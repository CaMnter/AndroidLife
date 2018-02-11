package com.camnter.hook.ams.f.service.plugin.host;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static Context BASE;
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

        // TODO Hook AMS

        BASE = base;
        this.loadDex();
        this.injectAboveEqualApiLevel14();

        // TODO ProxyServiceManager.getInstance().preLoadServices(apkFile);
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

            // assets 的 hook-ams-for-service-plugin-plugin.apk 拷贝到 /storage/sdcard0/Android/data/[package name]/cache
            // 或者  /storage/sdcard0/Android/data/[package name]/files
            final File dexPath = new File(
                dir + File.separator + "hook-ams-for-service-plugin-plugin.apk");
            final String dexAbsolutePath = dexPath.getAbsolutePath();
            AssetsUtils.copyAssets(this, "hook-ams-for-service-plugin-plugin.apk",
                dexAbsolutePath);

            // /data/data/[package name]/app_hook-ams-for-service-plugin-plugin
            final File optimizedDirectory = this.getDir("hook-ams-for-service-plugin-plugin",
                MODE_PRIVATE);

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


    private synchronized void injectAboveEqualApiLevel14() {
        final PathClassLoader pathClassLoader
            = (PathClassLoader) SmartApplication.class.getClassLoader();
        try {
            // 插桩
            final Object dexElements = combineArray(
                getDexElements(getPathList(pathClassLoader)),
                getDexElements(getPathList(this.dexClassLoader))
            );
            final Object pathList = getPathList(pathClassLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static Object getPathList(@NonNull  final Object baseDexClassLoader)
        throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
               ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"),
            "pathList");
    }


    private static Object getDexElements(@NonNull  final Object paramObject)
        throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }


    private static Object getField(@NonNull final Object object,
                                   @NonNull final Class<?> classloader,
                                   @NonNull final String field)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field localField = classloader.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(object);
    }


    @SuppressWarnings("SameParameterValue")
    private static void setField(@NonNull final Object object,
                                 @NonNull  final Class<?> classloader,
                                 @NonNull final String field,
                                 final Object value)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field localField = classloader.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(object, value);
    }


    private static Object combineArray(@NonNull final Object arrayLhs,
                                       @NonNull final Object arrayRhs) {
        final Class<?> localClass = arrayLhs.getClass().getComponentType();
        final int i = Array.getLength(arrayLhs);
        final int j = i + Array.getLength(arrayRhs);
        final Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }


    public DexClassLoader getDexClassLoader() {
        return this.dexClassLoader;
    }


    public static Context getContext() {
        return BASE;
    }

}
