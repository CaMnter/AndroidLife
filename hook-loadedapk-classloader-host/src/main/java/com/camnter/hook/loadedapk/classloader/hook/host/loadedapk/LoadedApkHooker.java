package com.camnter.hook.loadedapk.classloader.hook.host.loadedapk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import com.camnter.hook.loadedapk.classloader.hook.host.AssetsUtils;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Hook ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
 * 添加插件 LoadedApk
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class LoadedApkHooker {

    /**
     * 缓存插件 LoadedApk
     * 因为 ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages 是弱引用
     *
     * 所以，需要这个强引用去保存，防止回收
     */
    public static Map<String, Object> LOADEDAPK_MAP = new HashMap<>();


    /**
     * Hook ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
     *
     * @param apkFile apkFile
     * @param context context
     * @throws Exception Exception
     */
    @SuppressWarnings("unchecked")
    public static void hookLoadedApkForActivityThread(@NonNull final File apkFile,
                                                      @NonNull final Context context)
        throws Exception {

        /**
         * *****************************************************************************************
         *
         * ActivityThread 部分源码
         *
         * public final class ActivityThread {
         *
         *      ...
         *
         *      private static volatile ActivityThread sCurrentActivityThread;
         *
         *      ...
         *
         *      final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<String, WeakReference<LoadedApk>>();
         *
         *      ...
         *
         *      public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo){
         *
         *          return getPackageInfo(ai, compatInfo, null, false, true, false);
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * CompatibilityInfo 部分源码
         *
         * public class CompatibilityInfo implements Parcelable {
         *
         *     ...
         *
         *     public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {};
         *
         *     ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * public final class LoadedApk {
         *
         *     ...
         *
         *     private ClassLoader mClassLoader;
         *
         *     ...
         *
         * }
         *
         */

        /**
         * 获取 ActivityThread 实例
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        /**
         * 获取 ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
         */
        final Field mPackagesField = activityThreadClass.getDeclaredField("mPackages");
        mPackagesField.setAccessible(true);
        final Map mPackages = (Map) mPackagesField.get(currentActivityThread);

        /**
         * 获取 CompatibilityInfo # CompatibilityInfo DEFAULT_COMPATIBILITY_INFO
         */
        final Class<?> compatibilityInfoClass = Class.forName(
            "android.content.res.CompatibilityInfo");
        final Field defaultCompatibilityInfoField = compatibilityInfoClass.getDeclaredField(
            "DEFAULT_COMPATIBILITY_INFO");
        defaultCompatibilityInfoField.setAccessible(true);
        final Object defaultCompatibilityInfo = defaultCompatibilityInfoField.get(null);

        /**
         * 获取 ApplicationInfo
         */
        final ApplicationInfo applicationInfo = getApplicationInfo(apkFile, context);

        /**
         * 调用 ActivityThread # getPackageInfoNoCheck
         * 获取插件 LoadedApk
         */
        final Method getPackageInfoNoCheckMethod = activityThreadClass.getDeclaredMethod(
            "getPackageInfoNoCheck", ApplicationInfo.class, compatibilityInfoClass);
        final Object loadedApk = getPackageInfoNoCheckMethod.invoke(currentActivityThread,
            applicationInfo, defaultCompatibilityInfo);

        /**
         * 创建一个 Classloader
         */
        final String odexPath = AssetsUtils.getPluginOptDexDir(context, applicationInfo.packageName)
            .getPath();
        final String libDir = AssetsUtils.getPluginLibDir(context, applicationInfo.packageName)
            .getPath();
        final ClassLoader classLoader = new SmartClassloader(
            apkFile.getPath(),
            odexPath,
            libDir,
            ClassLoader.getSystemClassLoader()
        );

        /**
         * Hook LoadedApk # ClassLoader mClassLoader
         */
        final Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
        mClassLoaderField.setAccessible(true);
        mClassLoaderField.set(loadedApk, classLoader);

        /**
         * 强引用缓存一份 插件 LoadedApk
         */
        LOADEDAPK_MAP.put(applicationInfo.packageName, loadedApk);

        /**
         * Hook ActivityThread # ArrayMap<String, WeakReference<LoadedApk>> mPackages
         */
        final WeakReference<Object> weakReference = new WeakReference<>(loadedApk);
        mPackages.put(applicationInfo.packageName, weakReference);
    }


    /**
     * 解析 Apk 文件中的 application
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateApplicationInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    public static ApplicationInfo getApplicationInfo(@NonNull final File apkFile,
                                                     @NonNull final Context context)
        throws Exception {

        final ApplicationInfo applicationInfo;

        /**
         * 反射 获取 PackageParser # parsePackage(File packageFile, int flags)
         */
        final Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");

        /**
         * <= 4.0.0
         *
         * Don't deal with
         *
         * >= 4.0.0
         *
         * parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
         *
         * ---
         *
         * >= 5.0.0
         *
         * parsePackage(File packageFile, int flags)
         *
         */
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new RuntimeException(
                "[ApkUtils]   the sdk version must >= 14 (4.0.0)");
        }

        final Object packageParser;
        final Object packageObject;
        final Method parsePackageMethod;

        if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // >= 5.0.0
            // parsePackage(File packageFile, int flags)
            /**
             * 反射创建 PackageParser 对象，无参数构造
             *
             * 反射 调用 PackageParser # parsePackage(File packageFile, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            packageParser = packageParserClass.newInstance();

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                PackageManager.GET_SERVICES
            );
        } else {
            // >= 4.0.0
            // parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
            /**
             * 反射创建 PackageParser 对象，PackageParser(String archiveSourcePath)
             *
             * 反射 调用 PackageParser # parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            final String apkFileAbsolutePath = apkFile.getAbsolutePath();
            packageParser = packageParserClass.getConstructor(String.class)
                .newInstance(apkFileAbsolutePath);

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, String.class, DisplayMetrics.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                apkFile.getAbsolutePath(),
                context.getResources().getDisplayMetrics(),
                PackageManager.GET_SERVICES
            );
        }

        final Class<?> packageParser$Package = Class.forName(
            "android.content.pm.PackageParser$Package");

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateApplicationInfo(Package p, int flags, PackageUserState state)

            /**
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateApplicationInfo(Package p, int flags, PackageUserState state)
            final Method generateApplicationInfo = packageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageParser$Package, int.class, packageUserStateClass);

            applicationInfo = (ApplicationInfo) generateApplicationInfo.invoke(
                packageParser,
                packageObject,
                0 /*解析全部信息*/,
                defaultUserState
            );

        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateApplicationInfo(Package p, int flags, boolean stopped, int enabledState, int userId)

            // 需要调用 android.content.pm.PackageParser#generateApplicationInfo(Package p, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            Method generateApplicationInfo = packageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageParser$Package, int.class, boolean.class, int.class);

            /**
             * 反射调用 PackageParser # generateApplicationInfo(Package p, int flags, boolean stopped, int enabledState, int userId)
             *
             * 在之前版本的 4.0.0 中 存在着
             * public class PackageParser {
             *     public final static class Package {
             *         // User set enabled state.
             *         public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
             *
             *         // Whether the package has been stopped.
             *         public boolean mSetStopped = false;
             *     }
             * }
             *
             * 然后保存
             */
            applicationInfo = (ApplicationInfo) generateApplicationInfo.invoke(
                packageParser,
                packageObject,
                0 /*解析全部信息*/,
                false,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                userId
            );
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateApplicationInfo(Package p, int flags)

            // 需要调用 android.content.pm.PackageParser#generateApplicationInfo(Package p, int flags)
            Method generateApplicationInfo = packageParserClass.getDeclaredMethod(
                "generateApplicationInfo",
                packageParser$Package, int.class);

            /**
             * 反射调用 PackageParser # generateApplicationInfo(Package p, int flags)
             *
             * 然后保存
             */
            applicationInfo = (ApplicationInfo) generateApplicationInfo.invoke(
                packageParser,
                packageObject,
                0 /*解析全部信息*/
            );
        } else {
            applicationInfo = null;
        }

        return applicationInfo;

    }

}
