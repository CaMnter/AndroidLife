package com.camnter.newlife.utils.context;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.reflect.Method;

/**
 * https://github.com/kaedea/Feya/blob/master/Application/Feya/src/main/java/me/kaede/feya/context/AndroidHacks.java
 */
@SuppressWarnings("WeakerAccess")
public class AndroidHacks {

    private static final String TAG = "Applications";
    private static Object sActivityThread;


    @NonNull
    public static Object getActivityThread() {
        if (sActivityThread == null) {
            synchronized (AndroidHacks.class) {
                if (sActivityThread == null) {
                    sActivityThread = getActivityThreadFromUIThread();
                    if (sActivityThread != null) {
                        return sActivityThread;
                    }

                    if (Looper.getMainLooper() == Looper.myLooper()) {
                        sActivityThread = getActivityThreadFromUIThread();
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        synchronized (AndroidHacks.class) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    sActivityThread = getActivityThreadFromUIThread();
                                    synchronized (AndroidHacks.class) {
                                        AndroidHacks.class.notify();
                                    }
                                }
                            });
                            try {
                                AndroidHacks.class.wait();
                            } catch (InterruptedException e) {
                                Log.w(TAG, "Waiting notification from UI thread error.", e);
                            }
                        }
                    }
                }
            }
        }
        return sActivityThread;
    }


    private static Object getActivityThreadFromUIThread() {
        Object activityThread = null;
        try {
            Method method = Class.forName("android.app.ActivityThread")
                .getMethod("currentActivityThread");
            method.setAccessible(true);
            activityThread = method.invoke(null);
        } catch (final Exception e) {
            Log.w(TAG, "Failed to get ActivityThread from ActivityThread#currentActivityThread. " +
                "In some case, this method return null in worker thread.", e);
        }
        return activityThread;
    }

}