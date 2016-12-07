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

import android.content.Context;
import android.util.Log;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.android.tools.fd.runtime.AppInfo.applicationId;
import static com.android.tools.fd.runtime.BootstrapApplication.LOG_TAG;

/**
 * Class which handles locating existing code and resource files on the device,
 * as well as writing new versions of these.
 *
 * 最核心方法：
 *
 * 1. checkInbox: 复制资源文件 resources.ap_（ 主要用于 创建资源 ）
 * -    1.1 判断 /data/data/( applicationId )/files/instant-run/inbox/resources.ap_ 是否存在
 * -    1.2 存在的话，复制到 /data/data/.../files/instant-run/left(or right)/resources.ap_ 下
 * 2. getDexList: 获取 /data/data/( applicationId )/files/instant-run/dex 的 .dex 路径集合
 * -              ( 主要用于 HOOK BootstrapApplication 的 ClassLoader 的 类加载机制 )
 * -    2.1 获取 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹下，最近修改的.dex 文
 * -        件的更新时间，记录为 newestHotswapPatch
 * -    2.2 获取 File: /data/data/( applicationId )/files/instant-run/dex ，但不一定创建
 * -    2.3 校验 /data/data/( applicationId )/files/instant-run/dex 文件夹：
 * -        2.3.1 如果不存在，那么会创建该文件夹后，将 instant-run.zip 内的所有 .dex ，加上前缀
 * -              "slice-" 复制到 /data/data/( applicationId )/files/instant-run/dex 文件夹 中
 * -              最后，获取该文件夹内的所有文件，保存在 File[] dexFiles
 * -        2.3.2 如果直接存在，直接获取 /data/data/( applicationId )/files/instant-run/dex 文件
 * -              夹中的所有文件，保存在 File[] dexFiles
 * -    2.4 如果 2.3 内提取 instant-run.zip :
 * -        2.4.1 失败了。再次校验 /data/data/( applicationId )/files/instant-run/dex 文件夹。遍历所有
 * -              文件，如果有一个文件的修改时间小于 APK 的修改时间，证明存在旧的 dex。将 instant-run.zip
 * -              内的所有 .dex ，加上前缀"slice-" 复制到 /data/data/( applicationId )/files/instant-run/dex
 * -              文件夹 中。然后，清空不是提取复制过来的 dex（ 旧 dex ）。
 * -        2.4.2 成功了。判断 1. 中的 dex-temp 文件夹是否存在 dex。存在的话，清空 dex-temp 文件夹
 * -    2.5 最后判断 hotSwap 的时间是不是比 coldSwap 的时间新。实质上就是 dex-temp 文件夹内的 files 和
 * -        dex 文件夹内的 files，谁最新！如果 hotSwap 的时间比 coldSwap 的时间新，调用 Restarter.showToastWhenPossible
 * -        提示 the app is older
 * -    2.6 返回 /data/data/( applicationId )/files/instant-run/dex 的 .dex 路径集合
 * 3. extractSlices: 提取 instant-run.zip 的资源（ 主要用于获取所有 dex 集合，然后实例化一个补丁 ClassLoader
 * -                 进而 HOOK BootstrapApplication 的 ClassLoader 的 类加载机制 ）
 * -    3.1 Class.getResourceAsStream("/instant-run.zip") 去加载 instant-run.zip 的资源
 * -    3.2 提取出 instant-run.zip 内的资源（ 内部都是 .dex 文件 ）：
 * -        3.2.1 过滤掉 META-INF
 * -        3.2.2 过滤掉有 "/" 的 文件或文件夹
 * -        3.2.3 找出所有 .dex 文件，将其文件名加上前缀 "slice-" 保存在 Set<String> sliceNames
 * -        3.2.4 再将这些 .dex 文件，加载前缀 "slice-"，复制到 /data/data/( applicationId )/files/instant-run/dex
 * -              文件夹中
 * -        3.2.5 校验 /data/data/( applicationId )/files/instant-run/dex 文件夹中，是非存在不是 3.2.4
 * _              复制过来的文件。如果不是 2.4 复制过来的文件，证明是旧 "slice-" 文件，则删除
 * 4. getTempDexFile: 获取 dex-temp 文件夹下，下版本要创建的 File  ( 主要用于 热部署 )
 * -    4.1 获取 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹
 * -    4.2 校验 dex-temp 文件夹：
 * -        4.2.1 不存在，则创建
 * -        4.2.2 存在，则判断是否要清空。清空的话，则删除该文件夹下的所有 .dex 文件
 * -    4.3 然后遍历 dex-temp 文件夹下的文件:
 * -        4.3.1 截断 "reload" 和 ".dex" 之间的 十六进制版本号
 * -        4.3.2 找出版本号最大的 .dex 文件
 * -    4.4 根据 4.3.2 的找出的最大版本号的基础上，最大版本号+1，然后创建一个 "reload最大版本号.dex"
 * -        的 File 返回
 * 5. writeRawBytes: 二进制 生成 文件 （ 所有部署 ）。主要将 二进制数据 输出为 resources.ap_ or .dex
 * 6. extractZip:  提取出 instant-run.zip 流 内的 .dex 文件 ( 主要用于 温部署 )
 * -               注: 这提取出的 .dex ，不带 "slice-" 前缀。与 extractSlices 方法不同
 * -    6.1 过滤掉 META-INF
 * -    6.2 如果父路径文件夹不存在，则创建
 * 7. writeDexShard: 生成 dex（ 主要用于 冷部署  ）
 * -    7.1 校验目录：/data/data/( applicationId )/files/instant-run/dex。没有，则创建
 * -    7.2 通过调用 writeRawBytes 方法，在该目录下保存 dex 文件
 * 8. writeAaptResources: 生成资源文件 resources 或者 resources.ap_ ( 主要用于 温部署 )
 * -                      路径一般为 /data/data/( applicationId )/files/instant-run/left( right )
 * -                                /resources.ap_( resources )
 * -    8.1 拿到以上路径后，创建该路径的父文件夹
 * -    8.2 生成资源文件：
 * -        8.2.1 如果生成 resources.ap_：
 * -            8.2.1.1 如果 USE_EXTRACTED_RESOURCES = true，那么该流为 instant-run.zip 的数据，直接复
 * -                    制出内部的 dex 到 /data/data/( applicationId )/files/instant-run/left( right )
 * -                    目录下
 * -            8.2.1.2 如果 USE_EXTRACTED_RESOURCES = false，生成
 * -                    /data/data/( applicationId )/files/instant-run/left( right )/resources.ap_
 * -        8.2.2 如果生成 resources，那么直接写出
 * -              /data/data/( applicationId )/files/instant-run/left( right )/resources
 * 9. writeTempDexFile: 在 dex-temp 文件夹下 生成 dex ( 主要用于 热部署 )
 * 10. purgeTempDexFiles:  清空 dex-temp 下的 .dex 文件 ( 用于清空 热部署 产生的 dex-temp 文件夹中的 dex )
 * 11. readRawBytes: 读取 inbox/resources.ap_ ( 主要用于创建资源时，读取 inbox/resources.ap_ )
 * -    11.1 路径为: /data/data/( applicationId )/files/instant-run/inbox/resources.ap_
 * -    11.2 为了复制到 /data/data/.../files/instant-run/left(or right)/resources.ap_
 *
 * >>>>>>>
 *
 * 文件以及目录归类:
 *
 * 热部署：
 * /data/data/( applicationId )/files/instant-run/dex-temp
 *
 * 温部署：
 * /instant-run.zip
 * /data/data/( applicationId )/files/instant-run/left( right )
 * /data/data/( applicationId )/files/instant-run/inbox/resources.ap_
 * /data/data/( applicationId )/files/instant-run/left(or right)/resources.ap_
 *
 * 冷部署：
 * /data/data/( applicationId )/files/instant-run/dex
 */
public class FileManager {
    /**
     * According to Dianne, using an extracted directory tree of resources rather than
     * in an archive was implemented before 1.0 and never used or tested... so we should
     * tread carefully here.
     *
     * 标识是否 使用提取资源
     */
    private static final boolean USE_EXTRACTED_RESOURCES = false;

    /**
     * 资源文件名 resources.ap_
     */
    /** Name of file to write resource data into, if not extracting resources */
    private static final String RESOURCE_FILE_NAME = Paths.RESOURCE_FILE_NAME;

    /**
     * 资源文件夹 resources
     */
    /** Name of folder to write extracted resource data into, if extracting resources */
    private static final String RESOURCE_FOLDER_NAME = "resources";

    /**
     * 用于指定文件话 left 还是 right 的 文件名 active
     */
    /** Name of the file which points to either the left or the right data directory */
    private static final String FILE_NAME_ACTIVE = "active";

    /**
     * left 文件夹
     */
    /** Name of the left directory */
    private static final String FOLDER_NAME_LEFT = "left";

    /**
     * right 文件夹
     */
    /** Name of the right directory */
    private static final String FOLDER_NAME_RIGHT = "right";

    /**
     * reload.dex 的前缀
     */
    /** Prefix for reload.dex files */
    private static final String RELOAD_DEX_PREFIX = "reload";

    /**
     * classes.dex 的扩展名
     */
    /** Suffix for classes.dex files */
    public static final String CLASSES_DEX_SUFFIX = ".dex";

    /**
     * 标识是否 清空 temp dex 文件
     */
    /** Whether we've purged temp dex files in this session */
    private static boolean sHavePurgedTempDexFolder;


    /**
     * The folder where resources and code are located. Within this folder we have two
     * alternatives: "left" and "right". One is in the foreground (in use), one is in the
     * background (to write to). These are named {@link #FOLDER_NAME_LEFT} and
     * {@link #FOLDER_NAME_RIGHT} and the current one is pointed to by
     * {@link #FILE_NAME_ACTIVE}.
     *
     * 获取数据目录：/data/data/( applicationId )/files/instant-run
     */
    private static File getDataFolder() {
        // TODO: Call Context#getFilesDir(), but since we don't have a context yet figure
        // out what to do
        // Keep in sync with ResourceDeltaManager in the IDE (which needs this path
        // in order to run an adb wipe command when reinstalling a freshly built app
        // to avoid using stale data)
        return new File(Paths.getDataDirectory(applicationId));
    }


    /**
     * 获取资源文件
     *
     * 1. 如果 使用提取资源，那么路径为 父路径/resource
     * 2. 如果 不使用提取资源，那么路径为 父路径/resources.ap_
     *
     * 父路径：
     * left: /data/data/( applicationId )/files/instant-run/left
     * 还是
     * right: /data/data/( applicationId )/files/instant-run/right
     *
     * @param base 父路径
     * @return 资源完整路径
     */
    @NonNull
    private static File getResourceFile(File base) {
        //noinspection ConstantConditions
        return new File(base, USE_EXTRACTED_RESOURCES ? RESOURCE_FOLDER_NAME : RESOURCE_FILE_NAME);
    }

    /**
     * 获取 dex 文件夹
     *
     * 去寻找 父路径/dex 文件夹
     * 一般都是 /data/data/( applicationId )/files/instant-run/dex
     * 根据 createIfNecessary 的值，考虑不存在的话，是否创建
     *
     * @param base 父路径
     * @param createIfNecessary 如果不存在，是否创建
     * @return File = 父路径/dex or null
     */
    /**
     * Returns the folder used for .dex files used during the next app start
     */
    @Nullable
    private static File getDexFileFolder(File base, boolean createIfNecessary) {
        File file = new File(base, Paths.DEX_DIRECTORY_NAME);
        if (createIfNecessary) {
            if (!file.isDirectory()) {
                boolean created = file.mkdirs();
                if (!created) {
                    Log.e(LOG_TAG, "Failed to create directory " + file);
                    return null;
                }
            }
        }

        return file;
    }

    /**
     * 获取临时 dex 文件夹
     *
     * 直接 new 一个 File = /data/data/( applicationId )/files/instant-run/dex-temp
     * 一般都是 /data/data/( applicationId )/files/instant-run/dex-temp
     *
     * @param base /data/data/( applicationId )/files/instant-run
     * @return File = /data/data/( applicationId )/files/instant-run/dex-temp
     */
    /**
     * Returns the folder used for temporary .dex files (e.g. classes loaded on the fly
     * and only needing to exist during the current app process
     */
    @NonNull
    private static File getTempDexFileFolder(File base) {
        return new File(base, "dex-temp");
    }


    /**
     * 获取本地 lib 文件夹
     *
     * 直接 new 一个 File = /data/data/( applicationId )/lib
     *
     * @return File = /data/data/( applicationId )/lib
     */
    public static File getNativeLibraryFolder() {
        return new File(Paths.getMainApkDataDirectory(applicationId), "lib");
    }

    /**
     * 获取 外部资源读取的 文件夹
     *
     * 根据 {@link FileManager#leftIsActive} 的结果，决定读取
     *
     * left: /data/data/( applicationId )/files/instant-run/left
     * 还是
     * right: /data/data/( applicationId )/files/instant-run/right
     *
     * 主要用于 {@link FileManager#getExternalResourceFile}
     *
     * @return left or right
     */
    /**
     * Returns the "foreground" folder: the location to read code and resources from.
     */
    @NonNull
    public static File getReadFolder() {
        String name = leftIsActive() ? FOLDER_NAME_LEFT : FOLDER_NAME_RIGHT;
        return new File(getDataFolder(), name);
    }

    /**
     * 反转文件夹
     *
     * 如果 leftIsActive() 表示 true，表示 left
     * 如果 leftIsActive() 表示 false，表示 right
     *
     * 但是 setLeftActive(!leftIsActive()) 之后，
     * 会清空之前的 active 的内容，leftIsActive() 表示 true，那么在 active 文件写入 right，
     * leftIsActive() 表示 false，那么在 active 文件写入 left
     * 达到反转文件夹的效果
     */
    /**
     * Swaps the read/write folders such that the next time somebody asks for the
     * read or write folders, they'll get the opposite.
     */
    public static void swapFolders() {
        setLeftActive(!leftIsActive());
    }

    /**
     * 获取 外部资源写入 的文件夹
     *
     * 根据 {@link FileManager#leftIsActive} 的结果，决定写入
     *
     * left: /data/data/( applicationId )/files/instant-run/left
     * 还是
     * right: /data/data/( applicationId )/files/instant-run/right
     *
     * 然后再根据 wipe ，来决定是否 一定！ 删除或者保留之前 left( or right ) 的文件夹
     *
     * @param wipe 是否清空之前的文件
     * @return 外部资源写入 的文件夹
     */
    /**
     * Returns the "background" folder: the location to write code and resources to.
     */
    @NonNull
    public static File getWriteFolder(boolean wipe) {
        String name = leftIsActive() ? FOLDER_NAME_RIGHT : FOLDER_NAME_LEFT;
        File folder = new File(getDataFolder(), name);
        if (wipe && folder.exists()) {
            delete(folder);
            boolean mkdirs = folder.mkdirs();
            if (!mkdirs) {
                Log.e(LOG_TAG, "Failed to create folder " + folder);
            }
        }
        return folder;
    }


    /**
     * 删除 文件夹 or 文件
     *
     * 文件：直接删除
     * 文件夹：删除文件以及文件夹
     *
     * @param file 要删除的文件
     */
    private static void delete(@NonNull File file) {
        if (file.isDirectory()) {
            // Delete the contents
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    delete(child);
                }
            }
        }

        //noinspection ResultOfMethodCallIgnored
        boolean deleted = file.delete();
        if (!deleted) {
            Log.e(LOG_TAG, "Failed to delete file " + file);
        }
    }


    /**
     * 校验 active 文件或内容
     *
     * 1. 先拿到 data 目录: /data/data/( applicationId )/files/instant-run
     * 2. 定义 File : /data/data/( applicationId )/files/instant-run/active
     * 3. 如果 active 文件不存在，则返回 true，断定为 left
     * 4. 尝试读取 active 的内容
     * -    4.1 如果读到 "left"，返回 true，断定为 left
     * -    4.2 如果读到 "right", 返回 false，断定为 right
     * -    4.3 如果什么都没读到，或者文件不存在等等问题，返回 true，默认断定为 left
     *
     * @return true，断定为 left 或者 false，断定为 right
     */
    private static boolean leftIsActive() {
        File folder = getDataFolder();
        File pointer = new File(folder, FILE_NAME_ACTIVE);
        if (!pointer.exists()) {
            return true;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pointer));
            try {
                String line = reader.readLine();
                return FOLDER_NAME_LEFT.equals(line);
            } finally {
                reader.close();
            }
        } catch (IOException ignore) {
            return true;
        }
    }


    /**
     * 创建 active 文件
     *
     * 创建 active 文件，并根据传入的 boolean，写入 "left" 还是 "right"
     *
     * 1. 先拿到 data 目录: /data/data/( applicationId )/files/instant-run
     * 2. 定义 File : /data/data/( applicationId )/files/instant-run/active
     * 3. 判断 active 是否存在
     * -    3.1 存在，则删除
     * -    3.2 不存在，并且其父路径也不存在，则创建父路径的文件夹
     * 4. 根据 active 值，开始创建 active 文件，并对其写入内容
     * -    4.1 如果 active = true，写入 "left"
     * -    4.2 如果 active = false，写入 "right"
     *
     * @param active active
     */
    private static void setLeftActive(boolean active) {
        File folder = getDataFolder();
        File pointer = new File(folder, FILE_NAME_ACTIVE);
        if (pointer.exists()) {
            boolean deleted = pointer.delete();
            if (!deleted) {
                Log.e(LOG_TAG, "Failed to delete file " + pointer);
            }
        } else if (!folder.exists()) {
            boolean create = folder.mkdirs();
            if (!create) {
                Log.e(LOG_TAG, "Failed to create directory " + folder);
            }
            return;
        }

        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pointer),
                "UTF-8"));
            try {
                writer.write(active ? FOLDER_NAME_LEFT : FOLDER_NAME_RIGHT);
            } finally {
                writer.close();
            }
        } catch (IOException ignore) {
        }
    }

    /**
     * 复制资源文件 resources.ap_（ 主要用于 创建资源 ）
     *
     * 1. 判断 /data/data/( applicationId )/files/instant-run/inbox/resources.ap_ 是否存在
     * 2. 存在的话，复制到 /data/data/.../files/instant-run/left(or right)/resources.ap_ 下
     *
     * 主要用于 {@link BootstrapApplication#createResources(long)}
     */
    /** Looks in the inbox for new changes sent while the app wasn't running and apply them */
    public static void checkInbox() {
        File inbox = new File(Paths.getInboxDirectory(applicationId));
        if (inbox.isDirectory()) {
            File resources = new File(inbox, RESOURCE_FILE_NAME);
            if (resources.isFile()) {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Processing resource file from inbox (" + resources + ")");
                }
                byte[] bytes = readRawBytes(resources);
                if (bytes != null) {
                    FileManager.startUpdate();
                    FileManager.writeAaptResources(RESOURCE_FILE_NAME, bytes);
                    FileManager.finishUpdate(true);
                    boolean deleted = resources.delete();
                    if (!deleted) {
                        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                            Log.e(LOG_TAG, "Couldn't remove inbox resource file: " + resources);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取 外部资源文件
     *
     * 根据 {@link FileManager#leftIsActive} 的结果，决定读取
     *
     * left: /data/data/( applicationId )/files/instant-run/left
     * 还是
     * right: /data/data/( applicationId )/files/instant-run/right
     *
     * 如果不存在则返回 null，存在则 返回 left or right
     *
     * @return null，left，right
     */
    /** Returns the current/active resource file, if it exists */
    @Nullable
    public static File getExternalResourceFile() {
        File file = getResourceFile(getReadFolder());
        if (!file.exists()) {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Cannot find external resources, not patching them in");
            }
            return null;
        }

        return file;
    }

    /**
     * 获取 /data/data/( applicationId )/files/instant-run/dex 的 .dex 路径集合
     * ( 主要用于 HOOK BootstrapApplication 的 ClassLoader 的 类加载机制 )
     *
     * 1. 获取 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹下，最近修改的.dex 文
     * -  件的更新时间，记录为 newestHotswapPatch
     * 2. 获取 File: /data/data/( applicationId )/files/instant-run/dex ，但不一定创建
     * 3. 校验 /data/data/( applicationId )/files/instant-run/dex 文件夹：
     * -    3.1 如果不存在，那么会创建该文件夹后，将 instant-run.zip 内的所有 .dex ，加上前缀
     *          "slice-" 复制到 /data/data/( applicationId )/files/instant-run/dex 文件夹 中
     *          最后，获取该文件夹内的所有文件，保存在 File[] dexFiles
     * -    3.2 如果直接存在，直接获取 /data/data/( applicationId )/files/instant-run/dex 文件
     * -        夹中的所有文件，保存在 File[] dexFiles
     * 4. 如果 3. 内提取 instant-run.zip :
     * -    4.1 失败了。再次校验 /data/data/( applicationId )/files/instant-run/dex 文件夹。遍历所有文
     * -        件，如果有一个文件的修改时间小于 APK 的修改时间，证明存在旧的 dex。将 instant-run.zip 内
     * -        的所有 .dex ，加上前缀"slice-" 复制到 /data/data/( applicationId )/files/instant-run/dex
     * -        文件夹 中。然后，清空不是提取复制过来的 dex（ 旧 dex ）。
     * -    4.2 成功了。判断 1. 中的 dex-temp 文件夹是否存在 dex。存在的话，清空 dex-temp 文件夹
     * 5. 最后判断 hotSwap 的时间是不是比 coldSwap 的时间新。实质上就是 dex-temp 文件夹内的 files 和 dex 文
     * -  件夹内的 files，谁最新！如果 hotSwap 的时间比 coldSwap 的时间新，调用 Restarter.showToastWhenPossible
     * -  提示 the app is older
     * 6. 返回 /data/data/( applicationId )/files/instant-run/dex 的 .dex 路径集合
     *
     * @param context context
     * @param apkModified the apk modified time
     * @return /data/data/( applicationId )/files/instant-run/dex 的 .dex 路径集合
     */
    /**
     * Returns the list of available .dex files to be loaded, possibly empty
     *
     * @param apkModified main apk installation time to purge old dex files from previous
     * installation.
     */
    @NonNull
    public static List<String> getDexList(Context context, long apkModified) {
        File dataFolder = getDataFolder();

        long newestHotswapPatch = FileManager.getMostRecentTempDexTime(dataFolder);

        // We don't need "double buffering" for dex files - we never rewrite files, so we
        // can accumulate in the same dir
        File dexFolder = getDexFileFolder(dataFolder, false);

        // Extract slices.
        //
        // Imagine this scenario -- you run your app (so the device dex folder is filled).
        // Then you do a clean build etc -- so Gradle doesn't know there is existing state
        // on the device. If we *only* extract slices when there are no slices there already,
        // then we'd end up here just running the old slices already on the device.
        // On the other hand, we can't just always extract slices, since then each time
        // you run we'll overwrite coldswap and freezeswap slices.
        //
        // So what this code does is pass the APK timestamp to the extractor, and in the
        // extractor, if the timestamp is positive, we check before writing each slice that
        // it doesn't already exist and is newer than the APK.
        boolean extractedSlices = false;
        File[] dexFiles;
        if (dexFolder == null || !dexFolder.isDirectory()) {
            // It's the first run of a freshly installed app, and we need to extract the
            // slices from within the APK into the dex folder
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "No local dex slice folder: First run since installation.");
            }
            dexFolder = getDexFileFolder(dataFolder, true);
            if (dexFolder == null) {
                // Failed to create dex folder.
                Log.wtf(LOG_TAG, "Couldn't create dex code folder");
                return Collections.emptyList(); // unreachable
            }
            dexFiles = extractSlices(dexFolder, null, -1); // -1: unconditionally extract all
            extractedSlices = dexFiles.length > 0;
        } else {
            dexFiles = dexFolder.listFiles();
        }
        if (dexFiles == null || dexFiles.length == 0) {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Cannot find dex classes, not patching them in");
            }
            return Collections.emptyList();
        }

        // See if any of the slices are older than the APK. This will only be the case
        // if it's not the first run, and the APK has been reinstalled while there are some
        // potentially stale dex files.
        //
        // Note that we're *also* computing the timestamp of the *newest* coldswap slice.
        // We'll use that below to post a toast if the app seems to be missing hotswap patches.
        long newestColdswapPatch = apkModified;
        if (!extractedSlices && dexFiles.length > 0) {
            long oldestColdSwapPatch = apkModified;
            for (File dex : dexFiles) {
                long dexModified = dex.lastModified();
                oldestColdSwapPatch = Math.min(dexModified, oldestColdSwapPatch);
                newestColdswapPatch = Math.max(dexModified, newestColdswapPatch);
            }
            if (oldestColdSwapPatch < apkModified) {
                // At least one slice is older than the APK: re-extract those that
                // need it
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG,
                        "One or more slices were older than APK: extracting newer slices");
                }
                dexFiles = extractSlices(dexFolder, dexFiles, apkModified);
            }
        } else if (newestHotswapPatch > 0L) {
            // If the code is newer than the hotswap patches, delete them such that we don't
            // have to keep iterating through them each successive startup
            purgeTempDexFiles(dataFolder);
        }

        if (newestHotswapPatch > newestColdswapPatch) {
            String message = "Your app does not have the latest code changes because it "
                + "was restarted manually. Please run from IDE instead.";

            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, message);
            }

            // We now want to show a toast to the user showing that the app is older.
            // However, it's too early to show it here:
            Restarter.showToastWhenPossible(context, message);
        }

        List<String> list = new ArrayList<String>(dexFiles.length);
        for (File dex : dexFiles) {
            if (dex.getName().endsWith(CLASSES_DEX_SUFFIX)) {
                list.add(dex.getPath());
            }
        }

        // Dex files should be sorted in reverse order such that the class loader finds
        // most recent updates first
        Collections.sort(list, Collections.reverseOrder());

        return list;
    }

    /**
     * 提取 instant-run.zip 的资源（ 主要用于获取所有 dex 集合，然后实例化一个补丁 ClassLoader，进而
     * HOOK BootstrapApplication 的 ClassLoader 的 类加载机制 ）
     *
     * 1. Class.getResourceAsStream("/instant-run.zip") 去加载 instant-run.zip 的资源
     * 2. 提取出 instant-run.zip 内的资源（ 内部都是 .dex 文件 ）：
     * -    2.1 过滤掉 META-INF
     * -    2.2 过滤掉有 "/" 的 文件或文件夹
     * -    2.3 找出所有 .dex 文件，将其文件名加上前缀 "slice-" 保存在 Set<String> sliceNames
     * -    2.4 再将这些 .dex 文件，加载前缀 "slice-"，复制到 /data/data/( applicationId )/files/instant-run/dex
     * -        文件夹中
     * -    2.5 校验 /data/data/( applicationId )/files/instant-run/dex 文件夹中，是非存在不是 2.4 复制过来的文件
     * -        如果不是 2.4 复制过来的文件，证明是旧 "slice-" 文件，则删除
     *
     * @param dexFolder /data/data/( applicationId )/files/instant-run/dex
     * @param dexFolderFiles    /data/data/( applicationId )/files/instant-run/dex 内原本存在的文件
     * @param apkModified   APK 的最后修改时间
     * @return instant-run.zip 的 .dex 加上前缀后的文件集合
     */
    /**
     * Extracts the slices found in the APK root directory (instant-run.zip) into the dex folder,
     * and skipping any files that already exist and are newer than apkModified (unless apkModified
     * <= 0). It <b>also</b> deletes any <b>unrecognized</b> slices. This is necessary
     * since there are scenarios (such as b.android.com/204341) where we end up with slice files
     * in the dex folder that should <b>not</b> be loaded.
     */
    private static File[] extractSlices(@NonNull File dexFolder, @Nullable File[] dexFolderFiles,
                                        long apkModified) {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Extracting slices into " + dexFolder);
        }
        InputStream stream = BootstrapApplication.class.getResourceAsStream("/instant-run.zip");
        if (stream == null) {
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Could not find slices in APK; aborting.");
            }
            return new File[0];
        }
        List<File> slices = new ArrayList<File>(30);
        Set<String> sliceNames = new HashSet<String>(30);
        try {
            ZipInputStream zipInputStream = new ZipInputStream(stream);
            try {
                byte[] buffer = new byte[2000];

                for (ZipEntry entry = zipInputStream.getNextEntry();
                     entry != null;
                     entry = zipInputStream.getNextEntry()) {
                    String name = entry.getName();
                    // Don't extract META-INF data
                    if (name.startsWith("META-INF")) {
                        continue;
                    }
                    if (!entry.isDirectory()
                        && name.indexOf('/') == -1 // only files in root directory
                        && name.endsWith(CLASSES_DEX_SUFFIX)) {
                        // Using / as separators in both .zip files and on Android, no need to convert
                        // to File.separator

                        // Map slice name to the scheme already used by the code to push slices
                        // via the embedded server as well as the code to push via adb:
                        //   slice-<slicedir>
                        String sliceName = Paths.DEX_SLICE_PREFIX + name;
                        sliceNames.add(sliceName);
                        File dest = new File(dexFolder, sliceName);
                        slices.add(dest);

                        if (apkModified > 0) {
                            long sliceModified = dest.lastModified();
                            if (sliceModified > apkModified) {
                                // Ignore this slice: disk copy more recent than APK copy
                                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                                    Log.v(LOG_TAG, "Ignoring slice " + name
                                        + ": newer on disk than in APK");
                                }
                                continue;
                            }
                        }
                        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "Extracting slice " + name + " into " + dest);
                        }
                        File parent = dest.getParentFile();
                        if (parent != null && !parent.exists()) {
                            boolean created = parent.mkdirs();
                            if (!created) {
                                Log.wtf(LOG_TAG, "Failed to create directory " + dest);
                                return new File[0];
                            }
                        }

                        OutputStream src = new BufferedOutputStream(new FileOutputStream(dest));
                        try {
                            int bytesRead;
                            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                                src.write(buffer, 0, bytesRead);
                            }
                        } finally {
                            src.close();
                        }
                        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                            Log.v(LOG_TAG, "File written at " + System.currentTimeMillis());
                            Log.v(LOG_TAG, "File last modified reported : " + dest.lastModified());
                        }
                    }
                }

                // Remove old slice names
                if (dexFolderFiles != null) {
                    for (File file : dexFolderFiles) {
                        if (!sliceNames.contains(file.getName())) {
                            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                                Log.v(LOG_TAG, "Removing old slice " + file);
                            }
                            boolean deleted = file.delete();
                            if (!deleted) {
                                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                                    Log.v(LOG_TAG, "Could not delete " + file);
                                }
                            }
                        }
                    }
                }

                return slices.toArray(new File[slices.size()]);
            } catch (IOException ioe) {
                Log.wtf(LOG_TAG, "Failed to extract slices into directory " + dexFolder, ioe);
                return new File[0];
            } finally {
                try {
                    zipInputStream.close();
                } catch (IOException ignore) {
                }
            }
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * 获取 dex-temp 文件夹下，下版本要创建的 File  ( 主要用于 热部署 )
     *
     * 1. 获取 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹
     * 2. 校验 dex-temp 文件夹：
     * -    2.1 不存在，则创建
     * -    2.2 存在，则判断是否要清空。清空的话，则删除该文件夹下的所有 .dex 文件
     * 3. 然后遍历 dex-temp 文件夹下的文件:
     * -    3.1 截断 "reload" 和 ".dex" 之间的 十六进制版本号
     * -    3.2 找出版本号最大的 .dex 文件
     * 4. 根据 3.2 的找出的最大版本号的基础上，最大版本号+1，然后创建一个 "reload最大版本号.dex"
     * -  的 File 返回
     *
     * @return dex-temp 文件夹下，下版本要创建的 File
     */
    /** Produces the next available dex file name */
    @Nullable
    public static File getTempDexFile() {
        // Find the file name of the next dex file to write
        File dataFolder = getDataFolder();
        File dexFolder = getTempDexFileFolder(dataFolder);
        if (!dexFolder.exists()) {
            boolean created = dexFolder.mkdirs();
            if (!created) {
                Log.e(LOG_TAG, "Failed to create directory " + dexFolder);
                return null;
            }

            // There was nothing to purge, but leave the folder be from now on.
            sHavePurgedTempDexFolder = true;
        } else {
            // The *first* time we write a reload dex file in the new process, we'll
            // delete previously stashes reload dex files. (We keep them around
            // such that we can (repeatedly) warn an app on startup if its hotswap patches
            // are more recent than the app itself, such that developers aren't confused
            // when the app is not reflecting the most recent changes
            if (!sHavePurgedTempDexFolder) {
                purgeTempDexFiles(dataFolder);
            }
        }

        File[] files = dexFolder.listFiles();
        int max = -1;

        // Pick highest available number + 1 - we want these to be sortable
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(RELOAD_DEX_PREFIX) && name.endsWith(CLASSES_DEX_SUFFIX)) {
                    String middle = name.substring(RELOAD_DEX_PREFIX.length(),
                        name.length() - CLASSES_DEX_SUFFIX.length());
                    try {
                        int version = Integer.decode(middle);
                        if (version > max) {
                            max = version;
                        }
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }

        String fileName = String.format("%s0x%04x%s", RELOAD_DEX_PREFIX, max + 1,
            CLASSES_DEX_SUFFIX);
        File file = new File(dexFolder, fileName);

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "Writing new dex file: " + file);
        }

        return file;
    }


    /**
     * 二进制 生成 文件 （ 所有部署 ）
     *
     * 主要将 二进制数据 输出为 resources.ap_ or .dex
     *
     * @param destination 路径
     * @param bytes 二进制数据
     * @return 是否成功
     */
    public static boolean writeRawBytes(@NonNull File destination, @NonNull byte[] bytes) {
        try {
            BufferedOutputStream output = new BufferedOutputStream(
                new FileOutputStream(destination));
            try {
                output.write(bytes);
                output.flush();
                return true;
            } finally {
                output.close();
            }
        } catch (IOException ioe) {
            Log.wtf(LOG_TAG, "Failed to write file, clean project and rebuild " + destination, ioe);
            throw new RuntimeException(
                String.format(
                    "InstantRun could not write file %1$s, clean project and rebuild ",
                    destination));
        }
    }


    public static boolean extractZip(@NonNull File destination, @NonNull byte[] zipBytes) {
        InputStream inputStream = new ByteArrayInputStream(zipBytes);
        return extractZip(destination, inputStream);
    }


    /**
     * 提取出 instant-run.zip 流 内的 .dex 文件 ( 主要用于 温部署 )
     *
     * 1. 过滤掉 META-INF
     * 2. 如果父路径文件夹不存在，则创建
     *
     * 注: 这提取出的 .dex ，不带 "slice-" 前缀
     *
     * 和 {@link FileManager#extractSlices(File, File[], long)} 不一样
     *
     * @param destDir 目标文件夹
     * @param inputStream 数据流
     * @return 是否提取成功
     */
    public static boolean extractZip(@NonNull File destDir, @NonNull InputStream inputStream) {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        try {
            byte[] buffer = new byte[2000];

            for (ZipEntry entry = zipInputStream.getNextEntry();
                 entry != null;
                 entry = zipInputStream.getNextEntry()) {
                String name = entry.getName();
                // Don't extract META-INF data
                if (name.startsWith("META-INF")) {
                    continue;
                }
                if (!entry.isDirectory()) {
                    // Using / as separators in both .zip files and on Android, no need to convert
                    // to File.separator
                    File dest = new File(destDir, name);
                    File parent = dest.getParentFile();
                    if (parent != null && !parent.exists()) {
                        boolean created = parent.mkdirs();
                        if (!created) {
                            Log.e(LOG_TAG, "Failed to create directory " + dest);
                            return false;
                        }
                    }

                    OutputStream src = new BufferedOutputStream(new FileOutputStream(dest));
                    try {
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            src.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        src.close();
                    }
                }
            }

            return true;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Failed to extract zip contents into directory " + destDir, ioe);
            return false;
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException ignore) {
            }
        }
    }


    /**
     * 创建 外部资源写入 的文件夹
     *
     * 1. 如果存在，则先删除该文件夹
     * 2. 然后再开始创建 left or right
     *
     * left: /data/data/( applicationId )/files/instant-run/left
     * 还是
     * right: /data/data/( applicationId )/files/instant-run/right
     */
    public static void startUpdate() {
        // Wipe the back-buffer, if already present
        getWriteFolder(true);
    }


    /**
     * 进行 反转 left or right 文件夹
     *
     * 目前，所有调用 finishUpdate(boolean wroteResources) 的
     * 地方，都传了 true。所以，一定会进行反转 left or right 文件夹
     *
     * @param wroteResources wroteResources
     */
    public static void finishUpdate(boolean wroteResources) {
        if (wroteResources) {
            swapFolders();
        }
    }


    /**
     * 生成 dex（ 主要用于 冷部署  ）
     *
     * 1. 校验目录：/data/data/( applicationId )/files/instant-run/dex。没有，则创建
     * 2. 通过调用 writeRawBytes 方法，在该目录下保存 dex 文件
     *
     * @param bytes 二进制数据
     * @param name dex 名
     * @return File
     */
    @Nullable
    public static File writeDexShard(@NonNull byte[] bytes, @NonNull String name) {
        File dexFolder = getDexFileFolder(getDataFolder(), true);
        if (dexFolder == null) {
            return null;
        }
        File file = new File(dexFolder, name);
        writeRawBytes(file, bytes);
        return file;
    }


    /**
     * 生成资源文件 resources 或者 resources.ap_ ( 主要用于 温部署 )
     *
     * 路径一般为 /data/data/( applicationId )/files/instant-run/left( right )/resources.ap_( resources
     * )
     *
     * 1. 拿到以上路径后，创建该路径的父文件夹
     * 2. 生成资源文件：
     * -    2.1 如果生成 resources.ap_：
     * -        2.1.1 如果 USE_EXTRACTED_RESOURCES = true，那么该流为 instant-run.zip 的数据，直接复制
     * -              出内部的 dex 到 /data/data/( applicationId )/files/instant-run/left( right ) 目
     * -              录下
     * -        2.1.2 如果 USE_EXTRACTED_RESOURCES = false，生成
     * -              /data/data/( applicationId )/files/instant-run/left( right )/resources.ap_
     * -    2.2 如果生成 resources，那么直接写出
     * -        /data/data/( applicationId )/files/instant-run/left( right )/resources
     *
     * @param relativePath 文件名
     * @param bytes instant-run.zip 流 or 资源流
     */
    public static void writeAaptResources(@NonNull String relativePath, @NonNull byte[] bytes) {
        // TODO: Take relativePath into account for the actual destination file
        File resourceFile = getResourceFile(getWriteFolder(false));
        File file = resourceFile;
        if (USE_EXTRACTED_RESOURCES) {
            file = new File(file, relativePath);
        }
        File folder = file.getParentFile();
        if (!folder.isDirectory()) {
            boolean created = folder.mkdirs();
            if (!created) {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Cannot create local resource file directory " + folder);
                }
                return;
            }
        }

        if (relativePath.equals(RESOURCE_FILE_NAME)) {
            //noinspection ConstantConditions
            if (USE_EXTRACTED_RESOURCES) {
                extractZip(resourceFile, bytes);
            } else {
                writeRawBytes(file, bytes);
            }
        } else {
            writeRawBytes(file, bytes);
        }
    }


    /**
     * 在 dex-temp 文件夹下 生成 dex ( 主要用于 热部署 )
     *
     * @param bytes 二进制数据
     * @return dex 文件的路径
     */
    @Nullable
    public static String writeTempDexFile(byte[] bytes) {
        File file = getTempDexFile();
        if (file != null) {
            writeRawBytes(file, bytes);
            return file.getPath();
        } else {
            Log.e(LOG_TAG, "No file to write temp dex content to");
        }
        return null;
    }

    /**
     * 获取 dex-temp 文件夹下，最近修改的.dex 文件的更新时间
     *
     * 获取 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹下，最近修改的
     * .dex 文件的更新时间
     * 如果 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹 不存在 或者
     * 文件夹内没有文件 return 0L
     *
     * @param dataFolder /data/data/( applicationId )/files/instant-run
     * @return 最新 .dex 的时间
     */
    /**
     * Returns the modification time of the newest hotswap (reload) dex file
     * or 0 if there are no hotswap dex files in the passed dataFolder
     */
    public static long getMostRecentTempDexTime(@NonNull File dataFolder) {
        File dexFolder = getTempDexFileFolder(dataFolder);
        if (!dexFolder.isDirectory()) {
            return 0L;
        }
        File[] files = dexFolder.listFiles();
        if (files == null) {
            return 0L;
        }

        long newest = 0L;
        for (File file : files) {
            if (file.getPath().endsWith(CLASSES_DEX_SUFFIX)) {
                newest = Math.max(newest, file.lastModified());
            }
        }

        return newest;
    }

    /**
     * 清空 dex-temp 下的 .dex 文件 ( 用于清空 热部署 产生的 dex-temp 文件夹中的 dex )
     *
     * 清空 /data/data/( applicationId )/files/instant-run/dex-temp 文件夹下的 .dex 文件
     * 但是，保留 dex-temp 文件夹
     *
     * 同时记录 sHavePurgedTempDexFolder = true
     *
     * @param dataFolder /data/data/( applicationId )/files/instant-run
     */
    /**
     * Removes .dex files from the temp dex file folder
     */
    public static void purgeTempDexFiles(@NonNull File dataFolder) {
        sHavePurgedTempDexFolder = true;

        File dexFolder = getTempDexFileFolder(dataFolder);
        if (!dexFolder.isDirectory()) {
            return;
        }
        File[] files = dexFolder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.getPath().endsWith(CLASSES_DEX_SUFFIX)) {
                boolean deleted = file.delete();
                if (!deleted) {
                    Log.e(LOG_TAG, "Could not delete temp dex file " + file);
                }
            }
        }
    }


    /**
     * 校验  resources.ap_ 的大小
     *
     * 前提：文件得是 resources.ap_
     *
     * @param path resources.ap_
     * @return 路径是 resources.ap_ 的话，返回 resources.ap_ 的大小
     * 不是的话，返回 -1
     */
    public static long getFileSize(@NonNull String path) {
        // Currently only handle this for resource files
        if (path.equals(RESOURCE_FILE_NAME)) {
            File file = getExternalResourceFile();
            if (file != null) {
                return file.length();
            }
        }

        return -1;
    }


    /**
     * 获取 resources.ap_ 文件的 MD5 值
     *
     * @param path resources.ap_
     * @return 路径是 resources.ap_ 的话，返回 resources.ap_ 的 MD5 值
     * 不是的话，返回 null
     */
    @Nullable
    public static byte[] getCheckSum(@NonNull String path) {
        // Currently only handle this for resource files
        if (path.equals(RESOURCE_FILE_NAME)) {
            File file = getExternalResourceFile();
            if (file != null) {
                return getCheckSum(file);
            }
        }

        return null;
    }


    /**
     * 获取 文件的 MD5 值
     * 主要获取 获取 resources.ap_ 文件的 MD5 值
     *
     * Computes a checksum of a file.
     *
     * @param file the file to compute the fingerprint for
     * @return a fingerprint
     */
    @Nullable
    public static byte[] getCheckSum(@NonNull File file) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4096];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            try {
                while (true) {
                    int read = input.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    digest.update(buffer, 0, read);
                }
                return digest.digest();
            } finally {
                input.close();
            }
        } catch (NoSuchAlgorithmException e) {
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Couldn't look up message digest", e);
            }
        } catch (IOException ioe) {
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Failed to read file " + file, ioe);
            }
        } catch (Throwable t) {
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Unexpected checksum exception", t);
            }
        }
        return null;
    }


    /**
     * 读取 inbox/resources.ap_ ( 主要用于创建资源时，读取 inbox/resources.ap_ )
     *
     * 1. 路径为: /data/data/( applicationId )/files/instant-run/inbox/resources.ap_
     * 2. 为了复制到 /data/data/.../files/instant-run/left(or right)/resources.ap_
     *
     * @param source /data/data/( applicationId )/files/instant-run/inbox/resources.ap_
     * @return 资源二进制数据
     */
    public static byte[] readRawBytes(@NonNull File source) {
        try {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG, "Reading the bytes for file " + source);
            }
            long length = source.length();
            if (length > Integer.MAX_VALUE) {
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "File too large (" + length + ")");
                }
                return null;
            }
            byte[] result = new byte[(int) length];

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(source));
            try {
                int index = 0;
                int remaining = result.length - index;
                while (remaining > 0) {
                    int numRead = input.read(result, index, remaining);
                    if (numRead == -1) {
                        break;
                    }
                    index += numRead;
                    remaining -= numRead;
                }
                if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                    Log.v(LOG_TAG, "Returning length " + result.length + " for file " + source);
                }
                return result;
            } finally {
                input.close();
            }
        } catch (IOException ioe) {
            if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                Log.e(LOG_TAG, "Failed to read file " + source, ioe);
            }
        }
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.v(LOG_TAG, "I/O error, no bytes returned for " + source);
        }
        return null;
    }

}
