package com.camnter.hook.loadedapk.classloader.hook.host.ams;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class AMSHooker {

    static final String EXTRA_TARGET_INTENT = "extra_target_intent";


    /**
     * Hook ActivityManagerNative 这个 Binder 中的 AMS
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    @SuppressLint("PrivateApi")
    public static void hookActivityManagerNative() throws ClassNotFoundException,
                                                          NoSuchMethodException,
                                                          InvocationTargetException,
                                                          IllegalAccessException,
                                                          NoSuchFieldException {

        /**
         * *****************************************************************************************
         *
         * ActivityManagerNative 部分源码
         *
         *
         * public abstract class ActivityManagerNative extends Binder implements IActivityManager{
         *
         *      ...
         *
         *      private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
         *          protected IActivityManager create() {
         *              IBinder b = ServiceManager.getService("activity");
         *              if (false) {
         *                  Log.v("ActivityManager", "default service binder = " + b);
         *              }
         *              IActivityManager am = asInterface(b);
         *              if (false) {
         *                  Log.v("ActivityManager", "default service = " + am);
         *              }
         *              return am;
         *          }
         *      };
         *
         *      ...
         *
         *
         * }
         *
         * *****************************************************************************************
         *
         * Singleton 的类结构
         *
         * package android.util;
         *
         *  **
         *  * Singleton helper class for lazily initialization.
         *  *
         *  * Modeled after frameworks/base/include/utils/Singleton.h
         *  *
         *  * @hide
         *  **
         *
         * public abstract class Singleton<T> {
         *      private T mInstance;
         *
         *      protected abstract T create();
         *
         *      public final T get() {
         *          synchronized (this) {
         *              if (mInstance == null) {
         *                  mInstance = create();
         *              }
         *                  return mInstance;
         *          }
         *      }
         * }
         *
         * *****************************************************************************************
         */

        final Field gDefaultField;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName(
                "android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);

        // IActivityManager
        final Object gDefault = gDefaultField.get(null);

        // 反射获取 IActivityManager # android.util.Singleton 的实例
        final Class<?> singletonClass = Class.forName("android.util.Singleton");
        final Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // 获取 ActivityManagerNative 中的 gDefault 对象里面原始的 IActivityManager 实例
        final Object rawIActivityManager = mInstanceField.get(gDefault);

        // 动态代理 创建一个 IActivityManager 代理类
        final Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        final Object proxy = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[] { iActivityManagerInterface },
            new IActivityManagerHandler(rawIActivityManager)
        );

        /**
         * 用 动态代理 创建的 IActivityManager 实现类
         * Hook 掉 IActivityManager # Singleton # mInstance
         */
        mInstanceField.set(gDefault, proxy);
    }


    /**
     * Hook ActivityThread 中 H 这个 Handler
     * 添加 mCallback
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    public static void hookActivityThreadH() throws ClassNotFoundException,
                                                    NoSuchMethodException,
                                                    InvocationTargetException,
                                                    IllegalAccessException,
                                                    NoSuchFieldException {
        /**
         * *****************************************************************************************
         *
         * ActivityThread 部分源码
         *
         * public final class ActivityThread {
         *
         *      ...
         *
         *      private class H extends Handler {
         *
         *          ...
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * Handler 部分源码
         *
         * public class Handler {
         *
         *     ...
         *
         *     final Callback mCallback;
         *
         *     public void dispatchMessage(Message msg) {
         *         if (msg.callback != null) {
         *             handleCallback(msg);
         *         } else {
         *            if (mCallback != null) {
         *                if (mCallback.handleMessage(msg)) {
         *                     return;
         *                }
         *            }
         *            handleMessage(msg);
         *         }
         *     }
         *
         *     ...
         *
         * }
         */

        /**
         * 获取 ActivityThread
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Field currentActivityThreadField = activityThreadClass.getDeclaredField(
            "sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        final Object currentActivityThread = currentActivityThreadField.get(null);

        /**
         * 获取 ActivityThread # final H mH = new H()
         */
        final Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        final Handler mH = (Handler) mHField.get(currentActivityThread);

        /**
         * Hook 掉 H # mCallback
         */
        final Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);
        mCallBackField.set(mH, new HCallback(mH));
    }

}
