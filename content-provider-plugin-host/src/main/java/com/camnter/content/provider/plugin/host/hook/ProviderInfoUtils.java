package com.camnter.content.provider.plugin.host.hook;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
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
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class ProviderInfoUtils {

    private static final String TAG = ProviderInfoUtils.class.getSimpleName();


    /**
     * 解析 Apk 文件中的 <provider>
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateProviderInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("PrivateApi")
    public static Map<ComponentName, ProviderInfo> getProviderInfos(@NonNull final File apkFile,
                                                                    @NonNull final Context context)
        throws Exception {

        final Map<ComponentName, ProviderInfo> providerInfoMap = new HashMap<>();

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
                "[" + TAG + "]   the sdk version must >= 14 (4.0.0)");
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
                PackageManager.GET_PROVIDERS
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
                PackageManager.GET_PROVIDERS
            );
        }

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateProviderInfo(Provider p, int flags, PackageUserState state, int userId)
            /**
             * 读取 Package # ArrayList<Provider> providers
             * 通过 ArrayList<Provider> providers 获取 Provider 对应的 ProviderInfo
             */
            final Field providersField = packageObject.getClass().getDeclaredField("providers");
            final List providers = (List) providersField.get(packageObject);

            /**
             * 反射调用 UserHandle # static @UserIdInt int getCallingUserId()
             * 获取到 userId
             *
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageParser$ProviderClass = Class.forName(
                "android.content.pm.PackageParser$Provider");
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Class<?> userHandler = Class.forName("android.os.UserHandle");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateProviderInfo(Provider p, int flags, PackageUserState state, int userId)
            final Method generateProviderInfo = packageParserClass.getDeclaredMethod(
                "generateProviderInfo",
                packageParser$ProviderClass, int.class, packageUserStateClass, int.class);

            /**
             * 反射调用 PackageParser # generateProviderInfo(Provider p, int flags, PackageUserState state, int userId)
             * 解析出 Provider 对应的 ProviderInfo
             *
             * 然后保存
             */
            for (Object provider : providers) {
                final ProviderInfo info = (ProviderInfo) generateProviderInfo.invoke(
                    packageParser,
                    provider,
                    0,
                    defaultUserState,
                    userId
                );
                providerInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateProviderInfo(Provider p, int flags, boolean stopped, int enabledState, int userId)
            /**
             * 读取 Package # ArrayList<Provider> providers
             * 通过 ArrayList<Provider> providers 获取 Provider 对应的 ProviderInfo
             */
            final Field providersField = packageObject.getClass().getDeclaredField("providers");
            final List providers = (List) providersField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateProviderInfo(Provider p, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> packageParser$ProviderClass = Class.forName(
                "android.content.pm.PackageParser$Provider");
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Method generateProviderInfo = packageParserClass.getDeclaredMethod(
                "generateProviderInfo",
                packageParser$ProviderClass, int.class, boolean.class, int.class, int.class);

            /**
             * 反射调用 PackageParser # generateProviderInfo(Provider p, int flags, boolean stopped, int enabledState, int userId)
             * 解析出 Provider 对应的 ProviderInfo
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
            for (Object provider : providers) {
                final ProviderInfo info = (ProviderInfo) generateProviderInfo.invoke(
                    packageParser,
                    provider,
                    0,
                    false,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    userId
                );
                providerInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateProviderInfo(Provider p, int flags)
            /**
             * 读取 Package # ArrayList<Provider> providers
             * 通过 ArrayList<Provider> providers 获取 Provider 对应的 ProviderInfo
             */
            final Field providersField = packageObject.getClass().getDeclaredField("providers");
            final List providers = (List) providersField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateProviderInfo(Provider p, int flags)
            final Class<?> packageParser$ProviderClass = Class.forName(
                "android.content.pm.PackageParser$Provider");
            final Method generateProviderInfo = packageParserClass.getDeclaredMethod(
                "generateProviderInfo",
                packageParser$ProviderClass, int.class);

            /**
             * 反射调用 PackageParser # generateProviderInfo(Provider p, int flags)
             * 解析出 Provider 对应的 ProviderInfo
             *
             * 然后保存
             */
            for (Object provider : providers) {
                final ProviderInfo info = (ProviderInfo) generateProviderInfo.invoke(
                    packageParser,
                    provider,
                    0
                );
                providerInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        }

        return providerInfoMap;

    }

}
