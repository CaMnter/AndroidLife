package com.camnter.hook.loadedapk.classloader.host;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import com.camnter.hook.loadedapk.classloader.hook.ams.AMSHooker;
import com.camnter.hook.loadedapk.classloader.hook.loadedapk.LoadedApkHooker;
import com.camnter.hook.loadedapk.classloader.hook.pms.PMSHooker;
import java.io.File;
import java.util.Map;

/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static Context BASE;

    private static Map<ComponentName, ActivityInfo> activityInfoMap;


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
            // assets 的 hook-loadedapk-classloader-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "hook-loadedapk-classloader-plugin.apk");
            final File apkFile = getFileStreamPath("hook-loadedapk-classloader-plugin.apk");
            // Hook LoadedApk
            LoadedApkHooker.hookLoadedApkForActivityThread(apkFile, base);
            // Hook PMS
            PMSHooker.hookPackageManagerService(base);
            activityInfoMap = ActivityInfoUtils.preLoadActivities(apkFile, base);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Context getContext() {
        return BASE;
    }


    public static Map<ComponentName, ActivityInfo> getActivityInfoMap() {
        return activityInfoMap;
    }

}
