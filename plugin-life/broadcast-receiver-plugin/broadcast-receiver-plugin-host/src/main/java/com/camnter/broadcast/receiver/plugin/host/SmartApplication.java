package com.camnter.broadcast.receiver.plugin.host;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import com.camnter.broadcast.receiver.plugin.host.hook.BaseDexClassLoaderHooker;
import com.camnter.broadcast.receiver.plugin.host.hook.ReceiverInfoUtils;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static final Map<ActivityInfo, List<? extends IntentFilter>> receiverInfoMap
        = new HashMap<>();


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
            // assets 的 broadcast-receiver-plugin-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "broadcast-receiver-plugin-plugin.apk");
            final File apkFile = getFileStreamPath("broadcast-receiver-plugin-plugin.apk");
            final File odexFile = getFileStreamPath("broadcast-receiver-plugin-plugin.odex");
            // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHooker.patchClassLoader(this.getClassLoader(), apkFile, odexFile);
            receiverInfoMap.putAll(ReceiverInfoUtils.getReceiverInfos(apkFile, base));
            // 插件广播注册成 动态广播
            this.registerPluginBroadcast(base, receiverInfoMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 注册 插件 Broadcast
     *
     * @param context context
     * @param receiverInfoMap receiverInfoMap
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InstantiationException InstantiationException
     */
    private void registerPluginBroadcast(@NonNull final Context context,
                                         @NonNull final Map<ActivityInfo, List<? extends IntentFilter>> receiverInfoMap)
        throws ClassNotFoundException,
               IllegalAccessException,
               InstantiationException {
        for (Map.Entry<ActivityInfo, List<? extends IntentFilter>> entry : receiverInfoMap.entrySet()) {
            final ActivityInfo activityInfo = entry.getKey();
            final List<? extends IntentFilter> intentFilters = entry.getValue();

            final ClassLoader classLoader = this.getClassLoader();
            for (IntentFilter intentFilter : intentFilters) {
                final BroadcastReceiver receiver = (BroadcastReceiver) classLoader.loadClass(
                    activityInfo.name).newInstance();
                context.registerReceiver(receiver, intentFilter);
            }
        }
    }

}
