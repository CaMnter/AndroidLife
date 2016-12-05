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
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.android.tools.fd.runtime.BootstrapApplication.LOG_TAG;

/**
 * A utility class which uses reflection hacks to replace the application instance and
 * the resource data for the current app.
 * This is based on the reflection parts of
 * com.google.devtools.build.android.incrementaldeployment.StubApplication,
 * plus changes to compile on JDK 6.
 * <p>
 * It now also has a lot of extra reflection machinery to do live resource swapping
 * in a running app (e.g. swiping through data structures, updating resource managers,
 * flushing cached theme entries, etc.)
 * <p>
 * The original is
 * https://github.com/google/bazel/blob/master/src/tools/android/java/com/google/devtools/build/android/incrementaldeployment/StubApplication.java
 * (May 11 revision, ca96e11)
 * <p>
 * (The code to handle resource loading etc is different; see FileManager.)
 * Furthermore, the resource patching was hacked on some more such that it can
 * handle live (activity-restart) changes, which allows us to for example patch
 * the theme and have existing activities have their themes updated!
 * <p>
 * Original comment for the StubApplication, which contained the reflection methods:
 * <p>
 * A stub application that patches the class loader, then replaces itself with the real application
 * by applying a liberal amount of reflection on Android internals.
 * <p>
 * <p>This is, of course, terribly error-prone. Most of this code was tested with API versions
 * 8, 10, 14, 15, 16, 17, 18, 19 and 21 on the Android emulator, a Nexus 5 running Lollipop LRX22C
 * and a Samsung GT-I5800 running Froyo XWJPE. The exception is {@code monkeyPatchAssetManagers},
 * which only works on Kitkat and Lollipop.
 * <p>
 * <p>Note that due to a bug in Dalvik, this only works on Kitkat if ART is the Java runtime.
 * <p>
 * <p>Unfortunately, if this does not work, we don't have a fallback mechanism: as soon as we
 * build the APK with this class as the Application, we are committed to going through with it.
 * <p>
 * <p>This class should use as few other classes as possible before the class loader is patched
 * because any class loaded before it cannot be incrementally deployed.
 *
 *
 * monkeyPatchApplication（ Hook BootstrapApplication ）:
 * -    1. Hook 掉 ActivityThread 内的所有 BootstrapApplication 为 RealApplication
 * -    2. Hook 掉 ActivityThread 内的所有 LoadedApk 内部的：
 *
 * monkeyPatchExistingResources（ 加载补丁资源，并 Hook 进 App 内  ）:
 * -    1. 反射调用 AssetManager.addAssetPath 方法加载 补丁资源
 * -    2. Hook Resource or ResourcesImpl 中的 mAssets，Hook 为 补丁资源
 * -    3. Hook Resource or ResourcesImpl 内 Theme or ThemeImpl 中的 mAssets，Hook 为 补丁资源
 * -    4. Hook Activity（ ContextThemeWrapper ）的 initializeTheme 方法去初始化 Theme
 * -    5. 如果 < 7.0， 先 Hook AssetManager 的 createTheme 方法去创建一个 补丁 Theme
 * -       然后 Hook Activity 的 Theme 的 mTheme Field 为 补丁 Theme
 * -    6. 调用 pruneResourceCaches(@NonNull Object resources) 方法去删除 资源缓存
 *
 * pruneResourceCache（ 由于 hook 进来了 newAssetManager，所以需要把原来运行 Activity 的资源缓存清空 ）:
 * -    1. 删除 Resource 内部的 TypedArrayPool 的资源缓存
 * -    2. 删除 Resource 图片、动画、颜色等资源缓存
 * -    3. 删除 ResourceImpl 图片、动画、颜色等资源缓存
 */
public class MonkeyPatcher {
    /**
     * 1. Hook 掉 ActivityThread 内的所有 BootstrapApplication 为 RealApplication
     * 2. Hook 掉 ActivityThread 内的所有 LoadedApk 内部的：
     * BootstrapApplication 为 RealApplication
     * mResDir 为 externalResourceFile
     *
     * @param context context
     * @param bootstrap BootstrapApplication
     * @param realApplication realApplication
     * @param externalResourceFile 外部资源 path
     */
    @SuppressWarnings("unchecked")  // Lots of conversions with generic types
    public static void monkeyPatchApplication(@Nullable Context context,
                                              @Nullable Application bootstrap,
                                              @Nullable Application realApplication,
                                              @Nullable String externalResourceFile) {
        /*
        The code seems to perform this:
        Application realApplication = the newly instantiated (in attachBaseContext) user app

        currentActivityThread = ActivityThread.currentActivityThread;
        Application initialApplication = currentActivityThread.mInitialApplication;
        if (initialApplication == BootstrapApplication.this) {
            currentActivityThread.mInitialApplication = realApplication;

        // Replace all instance of the stub application in ActivityThread#mAllApplications with the
        // real one
        List<Application> allApplications = currentActivityThread.mAllApplications;
        for (int i = 0; i < allApplications.size(); i++) {
            if (allApplications.get(i) == BootstrapApplication.this) {
                allApplications.set(i, realApplication);
            }
        }

        // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
        // ActivityThread#mResourcePackages and do two things:
        //   - Replace the Application instance in its mApplication field with the real one
        //   - Replace mResDir to point to the external resource file instead of the .apk. This is
        //     used as the asset path for new Resources objects.
        //   - Set Application#mLoadedApk to the found LoadedApk instance

        ArrayMap<String, WeakReference<LoadedApk>> map1 = currentActivityThread.mPackages;
        for (Map.Entry<String, WeakReference<?>> entry : map1.entrySet()) {
            Object loadedApk = entry.getValue().get();
            if (loadedApk == null) {
                continue;
            }

            if (loadedApk.mApplication == BootstrapApplication.this) {
                loadedApk.mApplication = realApplication;
                if (externalResourceFile != null) {
                    loadedApk.mResDir = externalResourceFile;
                }
                realApplication.mLoadedApk = loadedApk;
            }
        }

        // Exactly the same as above, except done for mResourcePackages instead of mPackages
        ArrayMap<String, WeakReference<LoadedApk>> map2 = currentActivityThread.mResourcePackages;
        for (Map.Entry<String, WeakReference<?>> entry : map2.entrySet()) {
            Object loadedApk = entry.getValue().get();
            if (loadedApk == null) {
                continue;
            }

            if (loadedApk.mApplication == BootstrapApplication.this) {
                loadedApk.mApplication = realApplication;
                if (externalResourceFile != null) {
                    loadedApk.mResDir = externalResourceFile;
                }
                realApplication.mLoadedApk = loadedApk;
            }
        }
        */

        // BootstrapApplication is created by reflection in Application#handleBindApplication() ->
        // LoadedApk#makeApplication(), and its return value is used to set the Application field in all
        // sorts of Android internals.
        //
        // Fortunately, Application#onCreate() is called quite soon after, so what we do is monkey
        // patch in the real Application instance in BootstrapApplication#onCreate().
        //
        // A few places directly use the created Application instance (as opposed to the fields it is
        // eventually stored in). Fortunately, it's easy to forward those to the actual real
        // Application class.
        try {

            /**
             * Step 1
             * 各种反射寻找该进程的 ActivityThread
             */

            // Find the ActivityThread instance for the current thread
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = getActivityThread(context, activityThread);

            /**
             * Step 2
             *
             * Hook ActivityThread 内的 BootStrapApplication 数据为 RealApplication
             *
             * 1.通过 ActivityThread 去 Hook 替换内部的 mInitialApplication Field
             * 如果实例化的是 BootStrapApplication  就替换上 RealApplication（ 项目真正的 Application ）
             *
             * 2.通过 ActivityThread 去 Hook 修改内部的 mAllApplications Field
             * 如果有 BootStrapApplication  就替换上 RealApplication（ 项目真正的 Application ）
             */

            // Find the mInitialApplication field of the ActivityThread to the real application
            Field mInitialApplication = activityThread.getDeclaredField("mInitialApplication");
            mInitialApplication.setAccessible(true);
            Application initialApplication = (Application) mInitialApplication.get(
                currentActivityThread);
            if (realApplication != null && initialApplication == bootstrap) {
                mInitialApplication.set(currentActivityThread, realApplication);
            }

            // Replace all instance of the stub application in ActivityThread#mAllApplications with the
            // real one
            if (realApplication != null) {
                Field mAllApplications = activityThread.getDeclaredField("mAllApplications");
                mAllApplications.setAccessible(true);
                List<Application> allApplications = (List<Application>) mAllApplications
                    .get(currentActivityThread);
                for (int i = 0; i < allApplications.size(); i++) {
                    if (allApplications.get(i) == bootstrap) {
                        allApplications.set(i, realApplication);
                    }
                }
            }

            /**
             * Step 3
             *
             * 再次 Hook ActivityThread 内的 BootStrapApplication 数据为 RealApplication
             * 并且把资源 dir 和 RealApplication 中的 LoadedApk 也替换了
             *
             * 对 ActivityThread 的 ArrayMap<String, WeakReference<LoadedApk>> mPackages 和
             * ArrayMap<String, WeakReference<LoadedApk>> mResourcePackages 进行 hook。
             * 遍历两个 map，如果里面的 LoadedApk 的 mApplication == BootstrapApplication。
             * 1. 替换 mApplication 为 自定义 Application；
             * 2. 替换 LoadedApk 的 mResDir 为 externalResourceFile；
             * 3. 用修改后的 LoadedApk hook 掉 项目 Application 中的 mLoadedApk  。
             */

            // Figure out how loaded APKs are stored.

            // API version 8 has PackageInfo, 10 has LoadedApk. 9, I don't know.
            Class<?> loadedApkClass;
            try {
                loadedApkClass = Class.forName("android.app.LoadedApk");
            } catch (ClassNotFoundException e) {
                loadedApkClass = Class.forName("android.app.ActivityThread$PackageInfo");
            }

            Field mApplication = loadedApkClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            Field mResDir = loadedApkClass.getDeclaredField("mResDir");
            mResDir.setAccessible(true);

            // 10 doesn't have this field, 14 does. Fortunately, there are not many Honeycomb devices
            // floating around.
            Field mLoadedApk = null;
            try {
                mLoadedApk = Application.class.getDeclaredField("mLoadedApk");
            } catch (NoSuchFieldException e) {
                // According to testing, it's okay to ignore this.
            }

            // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
            // ActivityThread#mResourcePackages and do two things:
            //   - Replace the Application instance in its mApplication field with the real one
            //   - Replace mResDir to point to the external resource file instead of the .apk. This is
            //     used as the asset path for new Resources objects.
            //   - Set Application#mLoadedApk to the found LoadedApk instance
            for (String fieldName : new String[] { "mPackages", "mResourcePackages" }) {
                Field field = activityThread.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(currentActivityThread);

                for (Map.Entry<String, WeakReference<?>> entry :
                    ((Map<String, WeakReference<?>>) value).entrySet()) {
                    Object loadedApk = entry.getValue().get();
                    if (loadedApk == null) {
                        continue;
                    }

                    if (mApplication.get(loadedApk) == bootstrap) {
                        if (realApplication != null) {
                            mApplication.set(loadedApk, realApplication);
                        }
                        if (externalResourceFile != null) {
                            mResDir.set(loadedApk, externalResourceFile);
                        }

                        if (realApplication != null && mLoadedApk != null) {
                            mLoadedApk.set(realApplication, loadedApk);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * 反射获取该进程实例化的 ActivityThread
     * 1. 先从 ActivityThread 内找实例对象
     * 2. 再反射到 Application -> LoadedApk ，在 LoadedApk 内寻找实例对象
     *
     * @param context context
     * @param activityThread ActivityThread Class
     * @return ActivityThread 实例
     */
    @Nullable
    public static Object getActivityThread(@Nullable Context context,
                                           @Nullable Class<?> activityThread) {
        try {

            /**
             * 通过反射 ActivityThread 的 currentActivityThread 方法
             * 拿到 ActivityThread 实例化
             */

            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);

            /**
             * 这的 Context 一般都是 Application
             * 所以，反射 Application 的 mLoadedApk Field（ LoadedApk ）
             * mLoadedApk 属性是一个 LoadedApk 类型
             * 再反射 LoadedApk 的 mActivityThread Field（ ActivityThread ）
             * 拿到 返回 该 ActivityThread 对象
             */

            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }


    /**
     * 1. 反射调用 AssetManager.addAssetPath 方法加载 补丁资源
     * 2. Hook Resource or ResourcesImpl 中的 mAssets，Hook 为 补丁资源
     * 3. Hook Resource or ResourcesImpl 内 Theme or ThemeImpl 中的 mAssets，Hook 为 补丁资源
     * 4. Hook Activity（ ContextThemeWrapper ）的 initializeTheme 方法去初始化 Theme
     * 5. 如果 < 7.0， 先 Hook AssetManager 的 createTheme 方法去创建一个 补丁 Theme
     *    然后 Hook Activity 的 Theme 的 mTheme Field 为 补丁 Theme
     * 6. 调用 pruneResourceCaches(@NonNull Object resources) 方法去删除 资源缓存
     *
     * @param context context
     * @param externalResourceFile 外部资源 path
     * @param activities 运行 activity
     */
    public static void monkeyPatchExistingResources(@Nullable Context context,
                                                    @Nullable String externalResourceFile,
                                                    @Nullable Collection<Activity> activities) {
        if (externalResourceFile == null) {
            return;
        }

        /*
        (Note: the resource directory is *also* inserted into the loadedApk in
        monkeyPatchApplication)
        The code seems to perform this:
        File externalResourceFile = <path to resources.ap_ or extracted directory>

        AssetManager newAssetManager = new AssetManager();
        newAssetManager.addAssetPath(externalResourceFile)

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        newAssetManager.ensureStringBlocks();

        // Find the singleton instance of ResourcesManager
        ResourcesManager resourcesManager = ResourcesManager.getInstance();

        // Iterate over all known Resources objects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (WeakReference<Resources> wr : resourcesManager.mActiveResources.values()) {
                Resources resources = wr.get();
                // Set the AssetManager of the Resources instance to our brand new one
                resources.mAssets = newAssetManager;
                resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
            }
        }

        // Also, for each context, call getTheme() to get the current theme; null out its
        // mTheme field, then invoke initializeTheme() to force it to be recreated (with the
        // new asset manager!)

        */

        try {

            /**
             * Step 1
             *
             * 反射 AssetManager#addAssetPath 方法
             * 加载补丁资源的 AssetManager
             */

            // Create a new AssetManager instance and point it to the resources installed under
            // /sdcard
            AssetManager newAssetManager = AssetManager.class.getConstructor().newInstance();
            Method mAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath",
                String.class);
            mAddAssetPath.setAccessible(true);
            if (((Integer) mAddAssetPath.invoke(newAssetManager, externalResourceFile)) == 0) {
                throw new IllegalStateException("Could not create new AssetManager");
            }

            /**
             * Step 2
             *
             * 反射 Hook 补丁资源 AssetManager 的 ensureStringBlocks Field 为 true
             * 下面注释告诉，4.4 需要这么做
             */

            // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
            // in L, so we do it unconditionally.
            Method mEnsureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            mEnsureStringBlocks.setAccessible(true);
            mEnsureStringBlocks.invoke(newAssetManager);

            if (activities != null) {
                for (Activity activity : activities) {
                    Resources resources = activity.getResources();

                    /**
                     * Step 3
                     *
                     * 获取每个 Activity
                     * 然后 Hook 每个 Resource 的 mAssets Field
                     * 设置为 补丁 AssetManager
                     *
                     * 如果没有 mAssets Field 就
                     * 去找 mResourcesImpl （ ResourcesImpl ） Field
                     * 然后 Hook ResourcesImpl 的 mAssets Field
                     * 设置为 补丁 AssetManager
                     */

                    try {
                        Field mAssets = Resources.class.getDeclaredField("mAssets");
                        mAssets.setAccessible(true);
                        mAssets.set(resources, newAssetManager);
                    } catch (Throwable ignore) {
                        Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                        mResourcesImpl.setAccessible(true);
                        Object resourceImpl = mResourcesImpl.get(resources);
                        Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
                        implAssets.setAccessible(true);
                        implAssets.set(resourceImpl, newAssetManager);
                    }

                    Resources.Theme theme = activity.getTheme();
                    try {

                        /**
                         * Step 4
                         *
                         * 进一步 拿到 Resource 的 Theme
                         * 然后 Hook Theme 的 mAssets Field
                         * 设置为 补丁 AssetManager
                         *
                         * 如果没有 mAssets Field 就
                         * 去找 mThemeImpl （ ResourcesImpl.ThemeImpl ） Field
                         * 然后 Hook ThemeImpl 的 mAssets Field
                         * 设置为 补丁 AssetManager
                         */

                        try {
                            Field ma = Resources.Theme.class.getDeclaredField("mAssets");
                            ma.setAccessible(true);
                            ma.set(theme, newAssetManager);
                        } catch (NoSuchFieldException ignore) {
                            Field themeField = Resources.Theme.class.getDeclaredField("mThemeImpl");
                            themeField.setAccessible(true);
                            Object impl = themeField.get(theme);
                            Field ma = impl.getClass().getDeclaredField("mAssets");
                            ma.setAccessible(true);
                            ma.set(impl, newAssetManager);
                        }

                        /**
                         * Step 5
                         *
                         * Hook Activity（ ContextThemeWrapper ）的 mTheme Field 为 null
                         * Hook Activity（ ContextThemeWrapper ）的 initializeTheme 方法去初始化 Theme
                         */

                        Field mt = ContextThemeWrapper.class.getDeclaredField("mTheme");
                        mt.setAccessible(true);
                        mt.set(activity, null);
                        Method mtm = ContextThemeWrapper.class.getDeclaredMethod("initializeTheme");
                        mtm.setAccessible(true);
                        mtm.invoke(activity);

                        /**
                         * Step 6
                         *
                         * 如果 < 24
                         *
                         * 先 Hook AssetManager 的 createTheme 方法
                         * 去创建一个 补丁 Theme
                         *
                         * 然后 Hook Activity 的 Theme 的 mTheme Field 为 补丁 Theme
                         */

                        if (SDK_INT < 24) { // As of API 24, mTheme is gone (but updates work
                            // without these changes
                            Method mCreateTheme = AssetManager.class
                                .getDeclaredMethod("createTheme");
                            mCreateTheme.setAccessible(true);
                            Object internalTheme = mCreateTheme.invoke(newAssetManager);
                            Field mTheme = Resources.Theme.class.getDeclaredField("mTheme");
                            mTheme.setAccessible(true);
                            mTheme.set(theme, internalTheme);
                        }
                    } catch (Throwable e) {
                        Log.e(LOG_TAG, "Failed to update existing theme for activity " + activity,
                            e);
                    }

                    /**
                     * Step 7
                     *
                     * 调用 pruneResourceCaches(@NonNull Object resources) 方法去删除 资源缓存
                     */
                    pruneResourceCaches(resources);
                }
            }

            /**
             * Step 8
             *
             * 1. 如果 > 4.4，反射拿到 ResourcesManager 的 getInstance 方法。获取 ResourcesManager 的单例对象，
             *    有两种选择：
             *    1.1 反射获取 mActiveResources Field，强转为 ArrayMap<?, WeakReference<Resources>> 类型，赋值给 references
             *    1.2 反射获取 mResourceReferences Field，强转为 Collection<WeakReference<Resources>> 类型，赋值给 references
             *
             * 2. 如果 <= 4.4，通过 getActivityThread 方法去获取进程中的 ActivityThread 实例。 然后反射
             *    获取 ActivityThread 的 mActiveResources Field，强转为 HashMap<?, WeakReference<Resources>> 类型，赋值给 references
             *
             * 3. 将 1 or 2 环境中，保存下来的 references 进行遍历，拿到每一个 WeakReference<Resources>
             *    有两种选择：
             *    3.1 Hook Resource 的 mAssets Field 的值为 补丁 AssetManager
             *    3.2 如果 3.1 失败被 catch 了，反射拿到 Resource mResourcesImpl Field（ ResourcesImpl ）
             *        然后 Hook ResourcesImpl 的 mAssets Field 的值为 补丁 AssetManager
             *
             *
             */
            // Iterate over all known Resources objects
            Collection<WeakReference<Resources>> references;
            if (SDK_INT >= KITKAT) {
                // Find the singleton instance of ResourcesManager
                Class<?> resourcesManagerClass = Class.forName("android.app.ResourcesManager");
                Method mGetInstance = resourcesManagerClass.getDeclaredMethod("getInstance");
                mGetInstance.setAccessible(true);
                Object resourcesManager = mGetInstance.invoke(null);
                try {
                    Field fMActiveResources = resourcesManagerClass.getDeclaredField(
                        "mActiveResources");
                    fMActiveResources.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    ArrayMap<?, WeakReference<Resources>> arrayMap =
                        (ArrayMap<?, WeakReference<Resources>>) fMActiveResources.get(
                            resourcesManager);
                    references = arrayMap.values();
                } catch (NoSuchFieldException ignore) {
                    Field mResourceReferences = resourcesManagerClass.getDeclaredField(
                        "mResourceReferences");
                    mResourceReferences.setAccessible(true);
                    //noinspection unchecked
                    references = (Collection<WeakReference<Resources>>) mResourceReferences.get(
                        resourcesManager);
                }
            } else {
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                Field fMActiveResources = activityThread.getDeclaredField("mActiveResources");
                fMActiveResources.setAccessible(true);
                Object thread = getActivityThread(context, activityThread);
                @SuppressWarnings("unchecked")
                HashMap<?, WeakReference<Resources>> map =
                    (HashMap<?, WeakReference<Resources>>) fMActiveResources.get(thread);
                references = map.values();
            }
            for (WeakReference<Resources> wr : references) {
                Resources resources = wr.get();
                if (resources != null) {
                    // Set the AssetManager of the Resources instance to our brand new one
                    try {
                        Field mAssets = Resources.class.getDeclaredField("mAssets");
                        mAssets.setAccessible(true);
                        mAssets.set(resources, newAssetManager);
                    } catch (Throwable ignore) {
                        Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                        mResourcesImpl.setAccessible(true);
                        Object resourceImpl = mResourcesImpl.get(resources);
                        Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
                        implAssets.setAccessible(true);
                        implAssets.set(resourceImpl, newAssetManager);
                    }

                    resources.updateConfiguration(resources.getConfiguration(),
                        resources.getDisplayMetrics());
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * 删除 资源缓存
     *
     * 1. 删除 Resource 内部的 TypedArrayPool 的资源缓存
     * 2. 删除 Resource 图片、动画、颜色等资源缓存
     * 3. 删除 ResourceImpl 图片、动画、颜色等资源缓存
     *
     * @param resources resources
     */
    private static void pruneResourceCaches(@NonNull Object resources) {
        // Drain TypedArray instances from the typed array pool since these can hold on
        // to stale asset data

        /**
         * Step 1
         *
         * 如果 >= 5.0
         *
         * 反射获取 Resource 的 mTypedArrayPool Field
         * 去到该 Field 的 Class （ SynchronizedPool ）
         * 然后再反射获取 SynchronizedPool 的 acquire 方法
         *
         * 通过不断 反射调用 acquire 释放 TypedArray 数据（ 资源 ）
         */
        if (SDK_INT >= LOLLIPOP) {
            try {
                Field typedArrayPoolField =
                    Resources.class.getDeclaredField("mTypedArrayPool");
                typedArrayPoolField.setAccessible(true);
                Object pool = typedArrayPoolField.get(resources);
                Class<?> poolClass = pool.getClass();
                Method acquireMethod = poolClass.getDeclaredMethod("acquire");
                acquireMethod.setAccessible(true);
                while (true) {
                    Object typedArray = acquireMethod.invoke(pool);
                    if (typedArray == null) {
                        break;
                    }
                }
            } catch (Throwable ignore) {
            }
        }

        /**
         * Step 2
         *
         * 如果 >= 6.0
         *
         * 反射获取 Resource 的 mResourcesImpl Field
         * 然后通过传进来的 resource 对象去获取这个 Field 的对象 ResourcesImpl
         *
         * 在 >= 6.0 的版本，需要用 ResourcesImpl 去代替 Resource
         * 所以最后将传进来的 Resource 替换为 ResourcesImpl
         *
         * 所以以下的步骤分为两个分水岭
         * >= 6.0 的，resource 的对象类型为  ResourcesImpl
         * < 6.0 的，resource 的对象类型为  Resources
         */
        if (SDK_INT >= Build.VERSION_CODES.M) {
            // Really should only be N; fix this as soon as it has its own API level
            try {
                Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                mResourcesImpl.setAccessible(true);
                // For the remainder, use the ResourcesImpl instead, where all the fields
                // now live
                resources = mResourcesImpl.get(resources);
            } catch (Throwable ignore) {
            }
        }

        /**
         * Step 3
         *
         * 由于以上做了 resource 的修改
         *
         * 由于受到 Step 2 的影响，所以这里的话
         *
         * >= 4.3
         *      再 >= 6.0 ，会取到 ResourcesImpl 的 mAccessLock Field
         *      然后，然后获得该锁 （ Object mAccessLock ）
         *
         *      再 < 6.0 ，会取到 Resources 的 mAccessLock Field
         *      然后，然后获得该锁 （ Object mAccessLock ）
         *
         * < 4.3 ，直接获取 Resources 的 mTmpValue Field
         * 然后，然后获得该锁 （ Object mAccessLock ）
         *
         * 如果都没找到锁，就拿该类（ MonkeyPatcher.class ）作为锁
         */

        // Prune bitmap and color state lists etc caches
        Object lock = null;
        if (SDK_INT >= JELLY_BEAN_MR2) {
            try {
                Field field = resources.getClass().getDeclaredField("mAccessLock");
                field.setAccessible(true);
                lock = field.get(resources);
            } catch (Throwable ignore) {
            }
        } else {
            try {
                Field field = Resources.class.getDeclaredField("mTmpValue");
                field.setAccessible(true);
                lock = field.get(resources);
            } catch (Throwable ignore) {
            }
        }

        if (lock == null) {
            lock = MonkeyPatcher.class;
        }

        /**
         * Step 4
         *
         * 由于再次受到 Step 2 的影响
         *
         * 如果 >= 6.0 会删除
         * ResourcesImpl 内 mDrawableCache、mColorDrawableCache、mColorStateListCache、
         *                  mAnimatorCache、mStateListAnimatorCache 的资源缓存
         *
         * 如果 < 6.0 会删除
         * ResourcesImpl 内的   mDrawableCache、mColorDrawableCache、mColorStateListCache 资源缓存
         * 同时，如果还是 4.3 的话，会额外删除
         * Resources 内的   sPreloadedDrawables、sPreloadedColorDrawables、sPreloadedColorStateLists
         * 资源缓存
         */

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lock) {
            // Prune bitmap and color caches
            pruneResourceCache(resources, "mDrawableCache");
            pruneResourceCache(resources, "mColorDrawableCache");
            pruneResourceCache(resources, "mColorStateListCache");
            if (SDK_INT >= M) {
                pruneResourceCache(resources, "mAnimatorCache");
                pruneResourceCache(resources, "mStateListAnimatorCache");
            } else if (SDK_INT == KITKAT) {
                pruneResourceCache(resources, "sPreloadedDrawables");
                pruneResourceCache(resources, "sPreloadedColorDrawables");
                pruneResourceCache(resources, "sPreloadedColorStateLists");
            }
        }
    }


    /**
     * 如果 < 6.0 会删除
     * ResourcesImpl 内的   mDrawableCache、mColorDrawableCache、mColorStateListCache 资源缓存
     * 同时，如果还是 4.3 的话，会额外删除
     * Resources 内的   sPreloadedDrawables、sPreloadedColorDrawables、sPreloadedColorStateLists
     * 资源缓存
     *
     * @param resources ResourcesImpl or Resources
     * @param fieldName 以上 field name
     * @return 是否清空
     */
    private static boolean pruneResourceCache(@NonNull Object resources,
                                              @NonNull String fieldName) {
        try {

            /**
             * Step 1
             *
             * 从 ResourcesImpl or Resources 拿到对应的 fieldName 的 Field
             *
             * 当然，如果 ResourcesImpl 获取失败了，会 catch 。
             * 然后直接 Resources.class.getDeclaredField(fieldName) 去拿 Field
             */

            Class<?> resourcesClass = resources.getClass();
            Field cacheField;
            try {
                cacheField = resourcesClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignore) {
                cacheField = Resources.class.getDeclaredField(fieldName);
            }
            cacheField.setAccessible(true);
            Object cache = cacheField.get(resources);

            /**
             * Step 2
             *
             * 上面拿到的 Field
             * 然后判断 Field 的类型：
             *
             * 1 如果 < 4.1，有两种选择：
             *     1.1 如果属于 SparseArray 类型，直接 clear 清空，返回 true
             *     1.2 如果 => 4.0 并且 属于 LongSparseArray，也是直接 clear 清空，返回 true
             *
             * 2 如果 4.1 < x < 6.0，有四种选择：
             *     2.1 如果 Field 是 mColorStateListCache，并且属于 LongSparseArray，也是直接 clear 清空
             *     2.2 如果 Field 的类型实现了 ArrayMap 的超类（ Map ），然后反射调用 Resource clearDrawableCachesLocked 方法
             *     2.3 如果 Field 是类型实现了 LongSparseArray 的超类 （ Cloneable ？ ），然后反射调用 Resource clearDrawableCachesLocked 方法
             *         注： 2.2 与 2.3 的区别在于 clearDrawableCachesLocked 的参数不一样，一个是 ArrayMap，一个是 LongSparseArray
             *
             *     2.4 如果 Field 是类型实现是 数组类型，并且数组的 class 类型（ getComponentType ） 实现了 LongSparseArray 的超类 （ Cloneable ？ ）
             *         然后强转为 LongSparseArray[] 类型，一个一个拿出 LongSparseArray，然后 clear
             *
             * 3. 如果 >= 6.0 （ 主要针对 Marshmallow: DrawableCache class ）
             *    反射拿到该 Field 的 onConfigurationChange 的方法并且调用
             *      如果有就返回
             *      如果没有的话，继续拿到父类，继续反射拿到 onConfigurationChange 的方法并且调用。直到调用过一次 onConfigurationChange 为止
             *
             * 4. 如果 1-3 内又没一个选择的话，那么就是没有做任何 删除资源缓存操作 or 删除资源缓存失败了
             */

            // Find the class which defines the onConfigurationChange method
            Class<?> type = cacheField.getType();
            if (SDK_INT < JELLY_BEAN) {
                if (cache instanceof SparseArray) {
                    ((SparseArray) cache).clear();
                    return true;
                } else if (SDK_INT >= ICE_CREAM_SANDWICH && cache instanceof LongSparseArray) {
                    // LongSparseArray has API level 16 but was private (and available inside
                    // the framework) in 15 and is used for this cache.
                    //noinspection AndroidLintNewApi
                    ((LongSparseArray) cache).clear();
                    return true;
                }
            } else if (SDK_INT < M) {
                // JellyBean, KitKat, Lollipop
                if ("mColorStateListCache".equals(fieldName)) {
                    // For some reason framework doesn't call clearDrawableCachesLocked on
                    // this field
                    if (cache instanceof LongSparseArray) {
                        //noinspection AndroidLintNewApi
                        ((LongSparseArray) cache).clear();
                    }
                } else if (type.isAssignableFrom(ArrayMap.class)) {
                    Method clearArrayMap = Resources.class.getDeclaredMethod(
                        "clearDrawableCachesLocked", ArrayMap.class, Integer.TYPE);
                    clearArrayMap.setAccessible(true);
                    clearArrayMap.invoke(resources, cache, -1);
                    return true;
                } else if (type.isAssignableFrom(LongSparseArray.class)) {
                    try {
                        Method clearSparseMap = Resources.class.getDeclaredMethod(
                            "clearDrawableCachesLocked", LongSparseArray.class, Integer.TYPE);
                        clearSparseMap.setAccessible(true);
                        clearSparseMap.invoke(resources, cache, -1);
                        return true;
                    } catch (NoSuchMethodException e) {
                        if (cache instanceof LongSparseArray) {
                            //noinspection AndroidLintNewApi
                            ((LongSparseArray) cache).clear();
                            return true;
                        }
                    }
                } else if (type.isArray() &&
                    type.getComponentType().isAssignableFrom(LongSparseArray.class)) {
                    LongSparseArray[] arrays = (LongSparseArray[]) cache;
                    for (LongSparseArray array : arrays) {
                        if (array != null) {
                            //noinspection AndroidLintNewApi
                            array.clear();
                        }
                    }
                    return true;
                }
            } else {
                // Marshmallow: DrawableCache class
                while (type != null) {
                    try {
                        Method configChangeMethod = type.getDeclaredMethod(
                            "onConfigurationChange", Integer.TYPE);
                        configChangeMethod.setAccessible(true);
                        configChangeMethod.invoke(cache, -1);
                        return true;
                    } catch (Throwable ignore) {
                    }

                    type = type.getSuperclass();
                }
            }
        } catch (Throwable ignore) {
            // Not logging these; while there is some checking of SDK_INT here to avoid
            // doing a lot of unnecessary field lookups, it's not entirely accurate and
            // errs on the side of caution (since different devices may have picked up
            // different snapshots of the framework); therefore, it's normal for this
            // to attempt to look up a field for a cache that isn't there; only if it's
            // really there will it continue to flush that particular cache.
        }
        return false;
    }
}
