package com.camnter.hook.ams.f.service.plugin.host;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Refer form http://weishu.me/2016/05/11/understand-plugin-framework-service/
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class ProxyServiceManager {

    private static final String TAG = ProxyServiceManager.class.getSimpleName();

    private static volatile ProxyServiceManager INSTANCE;

    // 缓存在创建好的 Service
    private Map<String, Service> serviceMap = new HashMap<>();

    // 解析 apk file，存储插件的 ServiceInfo
    private Map<ComponentName, ServiceInfo> serviceInfoMap = new HashMap<>();


    public synchronized static ProxyServiceManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProxyServiceManager();
        }
        return INSTANCE;
    }


    /**
     * 启动插件 Service
     * 如果 Service 没启动，会创建一个 new 插件 Service
     *
     * @param rawIntent proxyIntent
     * @param startId startId
     */
    void onStart(@NonNull final Intent rawIntent,
                 final int startId) {
        // 查询是否有 该 intent 对应的 插件 ServiceInfo 缓存
        final ServiceInfo serviceInfo = this.selectPluginService(rawIntent);

        if (serviceInfo == null) {
            Log.w(TAG, "[ProxyServiceManager]   [onStart]   can not found service : " +
                rawIntent.getComponent());
            return;
        }

        try {
            if (!serviceMap.containsKey(serviceInfo.name)) {
                // service 不存在
                this.proxyCreateService(serviceInfo);
            }

            final Service service = this.serviceMap.get(serviceInfo.name);
            service.onStart(rawIntent, startId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 停止插件 Service
     * 全部的插件 Service 都停止之后, ProxyService 也会停止
     *
     * @param rawIntent rawIntent
     * @return int
     */
    @SuppressWarnings("UnusedReturnValue")
    int onStop(@NonNull final Intent rawIntent) {
        // 获取 Intent 对应的 ServiceInfo 缓存
        final ServiceInfo serviceInfo = selectPluginService(rawIntent);
        if (serviceInfo == null) {
            Log.w(TAG, "[ProxyServiceManager]   [stopService]   can not found service: " +
                rawIntent.getComponent());
            return 0;
        }
        // 获取 ServiceInfo 对应的 Service 缓存
        final Service service = this.serviceMap.get(serviceInfo.name);
        if (service == null) {
            Log.w(TAG,
                "[ProxyServiceManager]   [stopService]   can not running, are you stopped it multi-times?");
            return 0;
        }
        service.onDestroy();
        // 删除 Service 缓存
        this.serviceMap.remove(serviceInfo.name);
        if (this.serviceMap.isEmpty()) {
            Log.d(TAG, "[ProxyServiceManager]   [stopService]   service all stopped, stop proxy");
            final Context appContext = SmartApplication.getContext();
            appContext.stopService(new Intent().setComponent(
                new ComponentName(appContext.getPackageName(), ProxyService.class.getName())));
        }
        return 1;
    }


    /**
     * 匹配 ServiceInfo 缓存
     *
     * @param pluginIntent 插件 Intent
     * @return 插件 intent 的 ServiceInfo
     */
    private ServiceInfo selectPluginService(Intent pluginIntent) {
        for (ComponentName componentName : this.serviceInfoMap.keySet()) {
            if (componentName.equals(pluginIntent.getComponent())) {
                return this.serviceInfoMap.get(componentName);
            }
        }
        return null;
    }


    /**
     * 反射 ActivityThread # handleCreateService 创建 Service
     *
     * @param serviceInfo 插件 service
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    private void proxyCreateService(@NonNull final ServiceInfo serviceInfo) throws Exception {
        IBinder token = new Binder();

        /**
         * 反射创建 ActivityThread # CreateServiceData 对象
         *
         * CreateServiceData 结构
         *
         * static final class CreateServiceData {
         *      IBinder token;
         *      ServiceInfo info;
         *      CompatibilityInfo compatInfo;
         *      Intent intent;
         *      public String toString() {
         *          return "CreateServiceData{token=" + token + " className=" + info.name + " packageName=" + info.packageName + " intent=" + intent + "}";
         *      }
         * }
         */
        final Class<?> createServiceDataClass = Class.forName(
            "android.app.ActivityThread$CreateServiceData");
        final Constructor<?> constructor = createServiceDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        final Object createServiceData = constructor.newInstance();

        /**
         * Hook CreateServiceData # IBinder token
         */
        final Field tokenField = createServiceDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(createServiceData, token);

        /**
         * Hook CreateServiceData # ServiceInfo info
         *
         * 因为 loadClass 的时候
         * LoadedApk 会是主程序的 ClassLoader
         * Hook BaseDexClassLoader 方式 加载插件
         */
        serviceInfo.applicationInfo.packageName = SmartApplication.getContext().getPackageName();
        final Field infoField = createServiceDataClass.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(createServiceData, serviceInfo);

        /**
         * Hook CreateServiceData # CompatibilityInfo compatInfo
         *
         * public class CompatibilityInfo implements Parcelable {
         *
         *     ***
         *
         *     public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo(){}
         *
         *     ***
         *
         * }
         *
         * 获取默认的 CompatibilityInfo 配置
         */
        final Class<?> compatibilityClass = Class.forName("android.content.res.CompatibilityInfo");
        final Field defaultCompatibilityField = compatibilityClass.getDeclaredField(
            "DEFAULT_COMPATIBILITY_INFO");
        final Object defaultCompatibility = defaultCompatibilityField.get(null);
        final Field compatInfoField = createServiceDataClass.getDeclaredField("compatInfo");
        compatInfoField.setAccessible(true);
        compatInfoField.set(createServiceData, defaultCompatibility);

        /**
         * 反射获取 ActivityThread
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        /**
         * 反射调用 ActivityThread # handleCreateService(CreateServiceData data)
         */
        final Method handleCreateServiceMethod = activityThreadClass.getDeclaredMethod(
            "handleCreateService", createServiceDataClass);
        handleCreateServiceMethod.setAccessible(true);
        handleCreateServiceMethod.invoke(currentActivityThread, createServiceData);

        /**
         * ActivityThread # handleCreateService(CreateServiceData data) 创造的 Service 会保存在
         * ActivityThread # mServices 字段里面
         *
         * 可以根据 CreateServiceData # IBinder token 获取该 Service
         */
        final Field mServicesField = activityThreadClass.getDeclaredField("mServices");
        mServicesField.setAccessible(true);
        final Map mServices = (Map) mServicesField.get(currentActivityThread);
        final Service service = (Service) mServices.get(token);

        /**
         * 获取到之后, 移除这个 Service
         *
         * 因为创建信息用的是插件 Service 去欺骗 AMS
         * 但是该 Service 未注册
         *
         * 只是 借花献佛
         */
        mServices.remove(token);

        /**
         * 将此 Service 存储起来
         */
        serviceMap.put(serviceInfo.name, service);
    }


    /**
     * 解析 Apk 文件中的 <service>
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateServiceInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    public void preLoadServices(@NonNull final File apkFile, final Context context)
        throws Exception {

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
                "[BaseDexClassLoaderHooker]   the sdk version must >= 14 (4.0.0)");
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

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateServiceInfo(Service s, int flags, PackageUserState state, int userId)
            /**
             * 读取 Package # ArrayList<Service> services
             * 通过 ArrayList<Service> services 获取 Service 对应的 ServiceInfo
             */
            final Field servicesField = packageObject.getClass().getDeclaredField("services");
            final List services = (List) servicesField.get(packageObject);

            /**
             * 反射调用 UserHandle # static @UserIdInt int getCallingUserId()
             * 获取到 userId
             *
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageParser$ServiceClass = Class.forName(
                "android.content.pm.PackageParser$Service");
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Class<?> userHandler = Class.forName("android.os.UserHandle");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateServiceInfo(Service s, int flags, PackageUserState state, int userId)
            Method generateReceiverInfo = packageParserClass.getDeclaredMethod(
                "generateServiceInfo",
                packageParser$ServiceClass, int.class, packageUserStateClass, int.class);

            /**
             * 反射调用 PackageParser # generateServiceInfo(Service s, int flags, PackageUserState state, int userId)
             * 解析出 Service 对应的 ServiceInfo
             *
             * 然后保存
             */
            for (Object service : services) {
                final ServiceInfo info = (ServiceInfo) generateReceiverInfo.invoke(packageParser,
                    service, 0,
                    defaultUserState, userId);
                this.serviceInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateServiceInfo(Service s, int flags, boolean stopped, int enabledState, int userId)
            /**
             * 读取 Package # ArrayList<Service> services
             * 通过 ArrayList<Service> services 获取 Service 对应的 ServiceInfo
             */
            final Field servicesField = packageObject.getClass().getDeclaredField("services");
            final List services = (List) servicesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateServiceInfo(Service s, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> packageParser$ServiceClass = Class.forName(
                "android.content.pm.PackageParser$Service");
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            Method generateReceiverInfo = packageParserClass.getDeclaredMethod(
                "generateServiceInfo",
                packageParser$ServiceClass, int.class, boolean.class, int.class, int.class);

            /**
             * 反射调用 PackageParser # generateServiceInfo(Service s, int flags, boolean stopped, int enabledState, int userId)
             * 解析出 Service 对应的 ServiceInfo
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
            for (Object service : services) {
                final ServiceInfo info = (ServiceInfo) generateReceiverInfo.invoke(packageParser,
                    service, 0, false, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, userId);
                this.serviceInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateServiceInfo(Service s, int flags)
            /**
             * 读取 Package # ArrayList<Service> services
             * 通过 ArrayList<Service> services 获取 Service 对应的 ServiceInfo
             */
            final Field servicesField = packageObject.getClass().getDeclaredField("services");
            final List services = (List) servicesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateServiceInfo(Service s, int flags)
            final Class<?> packageParser$ServiceClass = Class.forName(
                "android.content.pm.PackageParser$Service");
            Method generateReceiverInfo = packageParserClass.getDeclaredMethod(
                "generateServiceInfo",
                packageParser$ServiceClass, int.class);

            /**
             * 反射调用 PackageParser # generateServiceInfo(Activity a, int flags)
             * 解析出 Service 对应的 ServiceInfo
             *
             * 然后保存
             */
            for (Object service : services) {
                final ServiceInfo info = (ServiceInfo) generateReceiverInfo.invoke(packageParser,
                    service, 0);
                this.serviceInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        }

    }

}
