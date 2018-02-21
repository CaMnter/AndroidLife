package com.camnter.hook.ams.f.service.plugin.host.hook;

import android.annotation.SuppressLint;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Refer form http://weishu.me/2016/05/11/understand-plugin-framework-service/
 *
 * @author CaMnter
 */

public final class AMSHooker {

    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";
    public static final String EXTRA_TARGET_INTENT_ACTION = "extra_target_intent_action";

    public static final String INTENT_ACTION_STOP = "intent_action_stop";
    public static final String INTENT_ACTION_START = "intent_action_start";


    /**
     * Hook ActivityManagerNative 这个 Binder 中的 AMS
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    @SuppressWarnings("DanglingJavadoc")
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

        final Class<?> activityManagerNativeClass = Class.forName(
            "android.app.ActivityManagerNative"
        );

        // static public IActivityManager getDefault()
        final Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true);

        // IActivityManager
        final Object gDefault = gDefaultField.get(null);

        // 反射获取 IActivityManager # android.util.Singleton 的实例
        final Class<?> singletonClass = Class.forName("android.util.Singleton");
        final Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // 获取 ActivityManagerNative 中的 gDefault 对象里面原始的 IActivityManager 实例
        final Object rawIActivityManager = mInstanceField.get(gDefault);

        // 动态代理 创建一个 IActivityManager 实现类
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

}
