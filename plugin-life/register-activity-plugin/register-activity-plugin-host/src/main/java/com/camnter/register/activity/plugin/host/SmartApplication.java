package com.camnter.register.activity.plugin.host;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.text.TextUtils;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private DexClassLoader dexClassLoader;


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

        this.loadDex();
        this.hookInstrumentation();
    }


    private void loadDex() {
        try {
            String dir = null;
            final File cacheDir = this.getExternalCacheDir();
            final File filesDir = this.getExternalFilesDir("");
            if (cacheDir != null) {
                dir = cacheDir.getAbsolutePath();
            } else {
                if (filesDir != null) {
                    dir = filesDir.getAbsolutePath();
                }
            }
            if (TextUtils.isEmpty(dir)) return;

            // assets 的 register-activity-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
            // 或者  /storage/sdcard0/Android/data/[package name]/files
            final File dexPath = new File(dir + File.separator + "register-activity-plugin.apk");
            AssetsUtils.copyAssets(this, "register-activity-plugin.apk", dexPath.getAbsolutePath());

            // /data/data/[package name]/app_register-activity-plugin
            final File optimizedDirectory = this.getDir("register-activity-plugin", MODE_PRIVATE);

            this.dexClassLoader = new DexClassLoader(
                dexPath.getAbsolutePath(),
                optimizedDirectory.getAbsolutePath(),
                null,
                this.getClassLoader()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hookInstrumentation() {
        try {
            /*
             * 反射 ActivityThread
             * 通过 currentActivityThread 方法
             * 获取存放的 ActivityThread 实例
             */
            @SuppressLint("PrivateApi") final Class<?> activityThreadClass = Class.forName(
                "android.app.ActivityThread");
            final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
                "currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            /*
             * ActivityThread 实例获取 Field mInstrumentation
             */
            final Field mInstrumentationField = activityThreadClass.getDeclaredField(
                "mInstrumentation");
            mInstrumentationField.setAccessible(true);
            final Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(
                currentActivityThread);

            // 是否 hook 过
            if (!(mInstrumentation instanceof SmartInstrumentation)) {
                final SmartInstrumentation pluginInstrumentation = new SmartInstrumentation(
                    dexClassLoader, mInstrumentation);
                mInstrumentationField.set(currentActivityThread, pluginInstrumentation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public DexClassLoader getDexClassLoader() {
        return this.dexClassLoader;
    }

}
