package com.camnter.content.provider.plugin.host;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.support.annotation.NonNull;
import com.camnter.content.provider.plugin.host.hook.BaseDexClassLoaderHooker;
import com.camnter.content.provider.plugin.host.hook.ProviderInfoUtils;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    final Map<ComponentName, ProviderInfo> providerInfoMap = new HashMap<>();


    /**
     * Set the base context for this ContextWrapper.  All calls will then be
     * delegated to the base context.  Throws
     * IllegalStateException if a base context has already been set.
     *
     * @param base The new base context for this wrapper.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            // assets 的 content-provider-plugin-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "content-provider-plugin-plugin.apk");
            final File apkFile = getFileStreamPath("content-provider-plugin-plugin.apk");
            final File odexFile = getFileStreamPath("content-provider-plugin-plugin.odex");
            // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHooker.patchClassLoader(this.getClassLoader(), apkFile, odexFile);
            // 解析 provider
            providerInfoMap.putAll(ProviderInfoUtils.getProviderInfos(apkFile, base));
            // 该进程安装 ContentProvider
            this.installProviders(base, providerInfoMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 反射 调用 ActivityThread # installContentProviders(Context context, List<ProviderInfo> providers)
     * 安装 ContentProvider
     *
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    private void installProviders(@NonNull final Context context,
                                  @NonNull final Map<ComponentName, ProviderInfo> providerInfoMap)
        throws NoSuchMethodException,
               ClassNotFoundException,
               IllegalAccessException,
               InvocationTargetException {

        List<ProviderInfo> providerInfos = new ArrayList<>();

        // 修改 ProviderInfo # String packageName
        for (Map.Entry<ComponentName, ProviderInfo> entry : providerInfoMap.entrySet()) {
            final ProviderInfo providerInfo = entry.getValue();
            providerInfo.applicationInfo.packageName = context.getPackageName();
            providerInfos.add(providerInfo);
        }

        if (providerInfos.isEmpty()) {
            return;
        }

        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        final Method installProvidersMethod = activityThreadClass.getDeclaredMethod(
            "installContentProviders", Context.class, List.class);

        installProvidersMethod.setAccessible(true);
        installProvidersMethod.invoke(currentActivityThread, context, providerInfos);
    }

}
