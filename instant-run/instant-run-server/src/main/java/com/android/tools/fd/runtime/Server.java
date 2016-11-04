/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.fd.runtime;

import android.app.Activity;
import android.app.Application;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.util.Log;
import com.android.annotations.NonNull;
import dalvik.system.DexClassLoader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_EOF;
import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_PATCHES;
import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_PATH_CHECKSUM;
import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_PATH_EXISTS;
import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_PING;
import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_RESTART_ACTIVITY;
import static com.android.tools.fd.common.ProtocolConstants.MESSAGE_SHOW_TOAST;
import static com.android.tools.fd.common.ProtocolConstants.PROTOCOL_IDENTIFIER;
import static com.android.tools.fd.common.ProtocolConstants.PROTOCOL_VERSION;
import static com.android.tools.fd.common.ProtocolConstants.UPDATE_MODE_COLD_SWAP;
import static com.android.tools.fd.common.ProtocolConstants.UPDATE_MODE_HOT_SWAP;
import static com.android.tools.fd.common.ProtocolConstants.UPDATE_MODE_NONE;
import static com.android.tools.fd.common.ProtocolConstants.UPDATE_MODE_WARM_SWAP;
import static com.android.tools.fd.runtime.BootstrapApplication.LOG_TAG;
import static com.android.tools.fd.runtime.FileManager.CLASSES_DEX_SUFFIX;
import static com.android.tools.fd.runtime.Paths.RELOAD_DEX_FILE_NAME;
import static com.android.tools.fd.runtime.Paths.RESOURCE_FILE_NAME;

/**
 * Server running in the app listening for messages from the IDE and updating the code and
 * resources
 * when provided
 */
public class Server {

    /**
     * If true, app restarts itself after receiving coldswap patches. If false,
     * it will just wait for the client to kill it remotely and restart via activity manager.
     * If we restart locally, there could be problems around: a) getting all the right intent
     * data to the restarted activity, and b) sometimes, the activity state is saved by the
     * system, and it could lead to conflicts with the new version of the app.
     * So this is currently turned off. See
     * https://code.google.com/p/android/issues/detail?id=200895#c9
     */
    private static final boolean RESTART_LOCALLY = false;

    /**
     * Temporary debugging: have the server emit a message to the log every 30 seconds to
     * indicate whether it's still alive
     */
    private static final boolean POST_ALIVE_STATUS = false;

    private LocalServerSocket mServerSocket;

    private final Application mApplication;

    private static int sWrongTokenCount;


    public static void create(@NonNull String packageName, @NonNull Application application) {
        //noinspection ResultOfObjectAllocationIgnored
        new Server(packageName, application);
    }


    private Server(@NonNull String packageName, @NonNull Application application) {
        mApplication = application;
        try {
            mServerSocket = new LocalServerSocket(packageName);
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Starting server socket listening for package " + packageName
                    + " on " + mServerSocket.getLocalSocketAddress());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error creating local socket at " + packageName, e);
            return;
        }
        startServer();

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Started server for package " + packageName);
        }
    }


    private void startServer() {
        try {
            Thread socketServerThread = new Thread(new SocketServerThread());
            socketServerThread.start();
        } catch (Throwable e) {
            // Make sure an exception doesn't cause the rest of the user's
            // onCreate() method to be invoked
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Fatal error starting Instant Run server", e);
            }
        }
    }


    private class SocketServerThread extends Thread {
        @Override
        public void run() {
            if (POST_ALIVE_STATUS) {
                final Handler handler = new Handler();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(LOG_TAG, "Instant Run server still here...");
                            }
                        });
                    }
                };

                timer.schedule(task, 1, 30000L);
            }

            while (true) {
                try {
                    LocalServerSocket serverSocket = mServerSocket;
                    if (serverSocket == null) {
                        break; // stopped?
                    }
                    LocalSocket socket = serverSocket.accept();

                    if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                        Log.v(LOG_TAG, "Received connection from IDE: spawning connection thread");
                    }

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                        socket);
                    socketServerReplyThread.run();

                    if (sWrongTokenCount > 50) {
                        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "Stopping server: too many wrong token connections");
                        }
                        mServerSocket.close();
                        break;
                    }
                } catch (Throwable e) {
                    if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                        Log.v(LOG_TAG, "Fatal error accepting connection on local socket", e);
                    }
                }
            }
        }
    }


    private class SocketServerReplyThread extends Thread {

        private final LocalSocket mSocket;


        SocketServerReplyThread(LocalSocket socket) {
            mSocket = socket;
        }


        @Override
        public void run() {
            try {
                DataInputStream input = new DataInputStream(mSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(mSocket.getOutputStream());
                try {
                    handle(input, output);
                } finally {
                    try {
                        input.close();
                    } catch (IOException ignore) {
                    }
                    try {
                        output.close();
                    } catch (IOException ignore) {
                    }
                }
            } catch (IOException e) {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Fatal error receiving messages", e);
                }
            }
        }


        private void handle(DataInputStream input, DataOutputStream output) throws IOException {
            long magic = input.readLong();
            if (magic != PROTOCOL_IDENTIFIER) {
                Log.w(LOG_TAG, "Unrecognized header format "
                    + Long.toHexString(magic));
                return;
            }
            int version = input.readInt();

            // Send current protocol version to the IDE so it can decide what to do
            output.writeInt(PROTOCOL_VERSION);

            if (version != PROTOCOL_VERSION) {
                Log.w(LOG_TAG, "Mismatched protocol versions; app is "
                    + "using version " + PROTOCOL_VERSION + " and tool is using version "
                    + version);
                return;
            }

            while (true) {
                int message = input.readInt();
                switch (message) {
                    case MESSAGE_EOF: {
                        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "Received EOF from the IDE");
                        }
                        return;
                    }

                    case MESSAGE_PING: {
                        // Send an "ack" back to the IDE.
                        // The value of the boolean is true only when the app is in the
                        // foreground.
                        boolean active = Restarter.getForegroundActivity(mApplication) != null;
                        output.writeBoolean(active);
                        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "Received Ping message from the IDE; " +
                                "returned active = " + active);
                        }
                        continue;
                    }

                    case MESSAGE_PATH_EXISTS: {
                        String path = input.readUTF();
                        long size = FileManager.getFileSize(path);
                        output.writeLong(size);
                        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "Received path-exists(" + path + ") from the " +
                                "IDE; returned size=" + size);
                        }
                        continue;
                    }

                    case MESSAGE_PATH_CHECKSUM: {
                        long begin = System.currentTimeMillis();
                        String path = input.readUTF();
                        byte[] checksum = FileManager.getCheckSum(path);
                        if (checksum != null) {
                            output.writeInt(checksum.length);
                            output.write(checksum);
                            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                                long end = System.currentTimeMillis();
                                String hash = new BigInteger(1, checksum).toString(16);
                                Log.v(LOG_TAG, "Received checksum(" + path + ") from the " +
                                    "IDE: took " + (end - begin) + "ms to compute " + hash);
                            }
                        } else {
                            output.writeInt(0);
                            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                                Log.v(LOG_TAG, "Received checksum(" + path + ") from the " +
                                    "IDE: returning <null>");
                            }
                        }
                        continue;
                    }

                    case MESSAGE_RESTART_ACTIVITY: {
                        if (!authenticate(input)) {
                            return;
                        }

                        Activity activity = Restarter.getForegroundActivity(mApplication);
                        if (activity != null) {
                            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                                Log.v(LOG_TAG, "Restarting activity per user request");
                            }
                            Restarter.restartActivityOnUiThread(activity);
                        }
                        continue;
                    }

                    case MESSAGE_PATCHES: {
                        if (!authenticate(input)) {
                            return;
                        }

                        List<ApplicationPatch> changes = ApplicationPatch.read(input);
                        if (changes == null) {
                            continue;
                        }

                        boolean hasResources = hasResources(changes);
                        int updateMode = input.readInt();
                        updateMode = handlePatches(changes, hasResources, updateMode);

                        boolean showToast = input.readBoolean();

                        // Send an "ack" back to the IDE; this is used for timing purposes only
                        output.writeBoolean(true);

                        restart(updateMode, hasResources, showToast);
                        continue;
                    }

                    case MESSAGE_SHOW_TOAST: {
                        String text = input.readUTF();
                        Activity foreground = Restarter.getForegroundActivity(mApplication);
                        if (foreground != null) {
                            Restarter.showToast(foreground, text);
                        } else if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "Couldn't show toast (no activity) : " + text);
                        }
                        continue;
                    }

                    default: {
                        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                            Log.e(LOG_TAG, "Unexpected message type: " + message);
                        }
                        // If we hit unexpected message types we can't really continue
                        // the conversation: we can misinterpret data for the unexpected
                        // command as separate messages with different meanings than intended
                        return;
                    }
                }
            }
        }


        private boolean authenticate(@NonNull DataInputStream input) throws IOException {
            long token = input.readLong();
            if (token != AppInfo.token) {
                Log.w(LOG_TAG, "Mismatched identity token from client; received " + token
                    + " and expected " + AppInfo.token);
                sWrongTokenCount++;
                return false;
            }
            return true;
        }
    }


    private static boolean isResourcePath(String path) {
        return path.equals(RESOURCE_FILE_NAME) || path.startsWith("res/");
    }


    private static boolean hasResources(@NonNull List<ApplicationPatch> changes) {
        // Any non-code patch is a resource patch (normally resources.ap_ but could
        // also be individual resource files such as res/layout/activity_main.xml)
        for (ApplicationPatch change : changes) {
            String path = change.getPath();
            if (isResourcePath(path)) {
                return true;
            }

        }
        return false;
    }


    private int handlePatches(@NonNull List<ApplicationPatch> changes, boolean hasResources,
                              int updateMode) {
        if (hasResources) {
            FileManager.startUpdate();
        }

        for (ApplicationPatch change : changes) {
            String path = change.getPath();
            if (path.endsWith(CLASSES_DEX_SUFFIX)) {
                handleColdSwapPatch(change);

                // Gradle sometimes sends a restart dex even when there is a hotswap patch,
                // so don't take the presence of a restart dex as a conclusion that we must
                // do a coldswap. Check.
                boolean canHotSwap = false;
                for (ApplicationPatch c : changes) {
                    if (c.getPath().equals(RELOAD_DEX_FILE_NAME)) {
                        canHotSwap = true;
                        break;
                    }
                }

                if (!canHotSwap) {
                    updateMode = UPDATE_MODE_COLD_SWAP;
                }

            } else if (path.equals(RELOAD_DEX_FILE_NAME)) {
                updateMode = handleHotSwapPatch(updateMode, change);
            } else if (isResourcePath(path)) {
                updateMode = handleResourcePatch(updateMode, change, path);
            }
        }

        if (hasResources) {
            FileManager.finishUpdate(true);
        }

        return updateMode;
    }


    private static int handleResourcePatch(int updateMode, @NonNull ApplicationPatch patch,
                                           @NonNull String path) {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Received resource changes (" + path + ")");
        }
        FileManager.writeAaptResources(path, patch.getBytes());
        //noinspection ResourceType
        updateMode = Math.max(updateMode, UPDATE_MODE_WARM_SWAP);
        return updateMode;
    }


    private int handleHotSwapPatch(int updateMode, @NonNull ApplicationPatch patch) {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Received incremental code patch");
        }
        try {
            String dexFile = FileManager.writeTempDexFile(patch.getBytes());
            if (dexFile == null) {
                Log.e(LOG_TAG, "No file to write the code to");
                return updateMode;
            } else if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Reading live code from " + dexFile);
            }
            String nativeLibraryPath = FileManager.getNativeLibraryFolder().getPath();
            DexClassLoader dexClassLoader = new DexClassLoader(dexFile,
                mApplication.getCacheDir().getPath(), nativeLibraryPath,
                getClass().getClassLoader());

            // we should transform this process with an interface/impl
            Class<?> aClass = Class.forName(
                "com.android.tools.fd.runtime.AppPatchesLoaderImpl", true, dexClassLoader);
            try {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Got the patcher class " + aClass);
                }

                PatchesLoader loader = (PatchesLoader) aClass.newInstance();
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Got the patcher instance " + loader);
                }
                String[] getPatchedClasses = (String[]) aClass
                    .getDeclaredMethod("getPatchedClasses").invoke(loader);
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Got the list of classes ");
                    for (String getPatchedClass : getPatchedClasses) {
                        Log.v(LOG_TAG, "class " + getPatchedClass);
                    }
                }
                if (!loader.load()) {
                    updateMode = UPDATE_MODE_COLD_SWAP;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldn't apply code changes", e);
                e.printStackTrace();
                updateMode = UPDATE_MODE_COLD_SWAP;
            }
        } catch (Throwable e) {
            Log.e(LOG_TAG, "Couldn't apply code changes", e);
            updateMode = UPDATE_MODE_COLD_SWAP;
        }
        return updateMode;
    }


    private static void handleColdSwapPatch(@NonNull ApplicationPatch patch) {
        if (patch.path.startsWith(Paths.DEX_SLICE_PREFIX)) {
            File file = FileManager.writeDexShard(patch.getBytes(), patch.path);
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Received dex shard " + file);
            }
        }
    }


    private void restart(int updateMode, boolean incrementalResources, boolean toast) {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Finished loading changes; update mode =" + updateMode);
        }

        if (updateMode == UPDATE_MODE_NONE || updateMode == UPDATE_MODE_HOT_SWAP) {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Applying incremental code without restart");
            }

            if (toast) {
                Activity foreground = Restarter.getForegroundActivity(mApplication);
                if (foreground != null) {
                    Restarter.showToast(foreground, "Applied code changes without activity " +
                        "restart");
                } else if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Couldn't show toast: no activity found");
                }
            }
            return;
        }

        List<Activity> activities = Restarter.getActivities(mApplication, false);

        if (incrementalResources && updateMode == UPDATE_MODE_WARM_SWAP) {
            // Try to just replace the resources on the fly!
            File file = FileManager.getExternalResourceFile();

            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "About to update resource file=" + file +
                    ", activities=" + activities);
            }

            if (file != null) {
                String resources = file.getPath();
                MonkeyPatcher.monkeyPatchApplication(mApplication, null, null, resources);
                MonkeyPatcher.monkeyPatchExistingResources(mApplication, resources, activities);
            } else {
                Log.e(LOG_TAG, "No resource file found to apply");
                updateMode = UPDATE_MODE_COLD_SWAP;
            }
        }

        Activity activity = Restarter.getForegroundActivity(mApplication);
        if (updateMode == UPDATE_MODE_WARM_SWAP) {
            if (activity != null) {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Restarting activity only!");
                }

                boolean handledRestart = false;
                try {
                    // Allow methods to handle their own restart by implementing
                    //     public boolean onHandleCodeChange(long flags) { .... }
                    // and returning true if the change was handled manually
                    Method method = activity.getClass().getMethod("onHandleCodeChange", Long.TYPE);
                    Object result = method.invoke(activity, 0L);
                    if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                        Log.v(LOG_TAG, "Activity " + activity
                            + " provided manual restart method; return " + result);
                    }
                    if (Boolean.TRUE.equals(result)) {
                        handledRestart = true;
                        if (toast) {
                            Restarter.showToast(activity, "Applied changes");
                        }
                    }
                } catch (Throwable ignore) {
                }

                if (!handledRestart) {
                    if (toast) {
                        Restarter.showToast(activity, "Applied changes, restarted activity");
                    }
                    Restarter.restartActivityOnUiThread(activity);
                }
                return;
            }

            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "No activity found, falling through to do a full app restart");
            }
            updateMode = UPDATE_MODE_COLD_SWAP;
        }

        if (updateMode != UPDATE_MODE_COLD_SWAP) {
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Unexpected update mode: " + updateMode);
            }
            return;
        }

        if (RESTART_LOCALLY) {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Performing full app restart");
            }

            Restarter.restartApp(mApplication, activities, toast);
        } else {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Waiting for app to be killed and restarted by the IDE...");
            }
        }
    }
}
