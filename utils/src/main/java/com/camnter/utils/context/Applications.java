package com.camnter.utils.context;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.annotation.NonNull;

/**
 * When the App is running, there must be an application context.
 *
 * @author Kaede
 * @see "https://github.com/oasisfeng/deagle/blob/master/library/src/main/java/com/oasisfeng/android/base/Applications.java"
 * @see "https://github.com/kaedea/Feya/blob/master/Application/Feya/src/main/java/me/kaede/feya/context/Applications.java"
 * @since 17/4/8
 */
@SuppressWarnings("WeakerAccess")
public class Applications {

    /**
     * Access a global {@link Application} context from anywhere, such as getting a context in a
     * Library
     * module without attaching it from App module.
     * <p>
     * Note that this method may return null in some cases, such as working with a hotfix framework
     * or access when the App is terminated.
     */
    @NonNull
    public static Application context() {
        return CURRENT;
    }


    @SuppressLint("StaticFieldLeak")
    private static final Application CURRENT;


    static {
        try {
            Object activityThread = AndroidHacks.getActivityThread();
            Object app = activityThread.getClass()
                .getMethod("getApplication")
                .invoke(activityThread);
            CURRENT = (Application) app;
        } catch (Throwable e) {
            throw new IllegalStateException(
                "Can not access Application context by magic code, boom!", e);
        }
    }

}