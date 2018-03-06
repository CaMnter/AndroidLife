package com.camnter.hook.ams.f.activity.plugin.host.hook;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收集 apk 内的 activity 信息
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class ActivityInfoUtils {

    /**
     * 匹配 ActivityInfo 缓存
     *
     * @param pluginIntent 插件 Intent
     * @return 插件 intent 的 ActivityInfo
     */
    public static ActivityInfo selectPluginActivity(final Map<ComponentName, ActivityInfo> activityInfoMap, Intent pluginIntent) {
        for (ComponentName componentName : activityInfoMap.keySet()) {
            if (componentName.equals(pluginIntent.getComponent())) {
                return activityInfoMap.get(componentName);
            }
        }
        return null;
    }


    /**
     * 解析 Apk 文件中的 <activity>
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateActivityInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    public static Map<ComponentName, ActivityInfo> preLoadActivities(@NonNull final File apkFile, final Context context)
        throws Exception {

        final Map<ComponentName, ActivityInfo> activityInfoMap = new HashMap<>();

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
                "[ActivityInfoUtils]   the sdk version must >= 14 (4.0.0)");
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
                PackageManager.GET_ACTIVITIES
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
                PackageManager.GET_ACTIVITIES
            );
        }

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            /**
             * 反射调用 UserHandle # static @UserIdInt int getCallingUserId()
             * 获取到 userId
             *
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Class<?> userHandler = Class.forName("android.os.UserHandle");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class, packageUserStateClass, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0,
                    defaultUserState,
                    userId
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class, boolean.class, int.class, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
             * 解析出 Activity 对应的 ActivityInfo
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
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0,
                    false,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    userId
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateActivityInfo(Activity a, int flags)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags)
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        }

        return activityInfoMap;

    }

}
