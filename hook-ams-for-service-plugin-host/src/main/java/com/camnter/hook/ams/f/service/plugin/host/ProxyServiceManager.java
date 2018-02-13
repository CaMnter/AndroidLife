package com.camnter.hook.ams.f.service.plugin.host;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import com.camnter.hook.ams.f.service.plugin.host.hook.AMSHooker;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
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

    // 存储插件的 Service 信息
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
     * @param proxyIntent proxyIntent
     * @param startId startId
     */
    public void onStart(@NonNull final Intent proxyIntent,
                        final int startId) {
        // 拿到 代理 service 收到的 intent 数据
        final Intent targetIntent = proxyIntent.getParcelableExtra(AMSHooker.EXTRA_TARGET_INTENT);
        // 查询是否有 该 intent 对应的 插件 ServiceInfo 缓存
        final ServiceInfo serviceInfo = this.selectPluginService(proxyIntent);

        if (serviceInfo == null) {
            Log.w(TAG, "[ProxyServiceManager]   [onStart]   can not found service : " +
                targetIntent.getComponent());
            return;
        }

        try {
            if (!serviceMap.containsKey(serviceInfo.name)) {
                // service 不存在
                this.proxyCreateService(serviceInfo);
            }

            final Service service = this.serviceMap.get(serviceInfo.name);
            service.onStart(targetIntent, startId);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        final Constructor<?> constructor = createServiceDataClass.getConstructor();
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

}
