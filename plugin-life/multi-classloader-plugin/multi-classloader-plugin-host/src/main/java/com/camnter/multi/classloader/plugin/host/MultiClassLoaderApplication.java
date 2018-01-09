package com.camnter.multi.classloader.plugin.host;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import java.lang.reflect.Field;

/**
 * @author CaMnter
 */

public class MultiClassLoaderApplication extends Application {

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
        this.hookPathClassLoaderParent(base);
    }


    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }


    private void hookPathClassLoaderParent(@NonNull final Context context) {
        final ClassLoader pathClassLoader = context.getClassLoader();
        final DispatchClassloader dispatchClassloader = new DispatchClassloader(context,
            pathClassLoader);
        try {
            final Class<?> clazz = ClassLoader.class;
            final Field parent = clazz.getDeclaredField("parent");
            parent.setAccessible(true);
            parent.set(pathClassLoader, dispatchClassloader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
