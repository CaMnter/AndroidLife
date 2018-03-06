package com.camnter.hook.ams.f.activity.plugin.host;

import android.app.Application;
import android.content.Context;
import com.camnter.hook.ams.f.activity.plugin.host.hook.AMSHooker;
import com.camnter.hook.ams.f.activity.plugin.host.hook.BaseDexClassLoaderHooker;
import java.io.File;

/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static Context BASE;


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
        BASE = base;
        try {
            // Hook AMS
            AMSHooker.hookActivityManagerNative();
            // Hook H
            AMSHooker.hookActivityThreadH();
            // assets 的 hook-ams-for-activity-plugin-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "hook-ams-for-activity-plugin-plugin.apk");
            final File apkFile = getFileStreamPath("hook-ams-for-activity-plugin-plugin.apk");
            final File odexFile = getFileStreamPath("hook-ams-for-activity-plugin-plugin.odex");
            // // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHooker.patchClassLoader(this.getClassLoader(), apkFile, odexFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Context getContext() {
        return BASE;
    }

}
