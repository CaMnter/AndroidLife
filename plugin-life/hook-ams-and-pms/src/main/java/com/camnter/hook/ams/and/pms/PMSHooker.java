package com.camnter.hook.ams.and.pms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author CaMnter
 */

public class PMSHooker {

    /**
     * 1. Hook ActivityThread 中的 sPackageManager
     * 2. Hook Context.getPackageManager() 后得到的 ApplicationPackageManager 中的 mPM
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    @SuppressWarnings("DanglingJavadoc")
    @SuppressLint("PrivateApi")
    public static void hookPackageManagerService(@NonNull final Context context)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InvocationTargetException,
               IllegalAccessException,
               NoSuchFieldException {

        /**
         * class ContextImpl extends Context {
         *
         * -    ...
         *
         * -    @Override
         * -    public PackageManager getPackageManager() {
         * -        if (mPackageManager != null) {
         * -            return mPackageManager;
         * -        }
         *
         * -        IPackageManager pm = ActivityThread.getPackageManager();
         * -        if (pm != null) {
         * -            // Doesn't matter if we make more than one instance.
         * -            return (mPackageManager = new ApplicationPackageManager(this, pm));
         * -        }
         *
         * -        return null;
         * -    }
         *
         * -    ...
         *
         * }
         *
         * public final class ActivityThread {
         *
         * -    ...
         *
         * -    static volatile IPackageManager sPackageManager;
         *
         * -    public static IPackageManager getPackageManager() {
         * -        if (sPackageManager != null) {
         * -            //Slog.v("PackageManager", "returning cur default = " + sPackageManager);
         * -            return sPackageManager;
         * -        }
         * -        IBinder b = ServiceManager.getService("package");
         * -        //Slog.v("PackageManager", "default service binder = " + b);
         * -        sPackageManager = IPackageManager.Stub.asInterface(b);
         * -        //Slog.v("PackageManager", "default service = " + sPackageManager);
         * -        return sPackageManager;
         * -    }
         *
         * -    ...
         *
         * }
         *
         * 1. 会先从 ActivityThread.getPackageManager() 获取 PMS。所以得 hook ActivityThread 内的 sPackageManager。
         * 2. 然后下面会进行 PMS 的包装，变为 ApplicationPackageManager。
         *
         * public class ApplicationPackageManager extends PackageManager {
         *
         * -    ...
         *
         * -    private final IPackageManager mPM;
         *
         * -    protected ApplicationPackageManager(ContextImpl context, IPackageManager pm) {
         * -        mContext = context;
         * -        mPM = pm;
         * -    }
         *
         * -    ...
         *
         * }
         *
         * 3. 可以看得出，还需要 hook ApplicationPackageManager 内的 mPM。
         */

        /**
         * 获取 ActivityThread # ActivityThread currentActivityThread
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod(
            "currentActivityThread");
        final Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        /**
         * 获取 ActivityThread # IPackageManager sPackageManager
         */
        final Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        final Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        /**
         * 动态代理 创建一个 IPackageManager 代理类
         */
        final Class<?> iPackageManagerInterface = Class.forName(
            "android.content.pm.IPackageManager");
        final Object proxy = Proxy.newProxyInstance(
            iPackageManagerInterface.getClassLoader(),
            new Class<?>[] { iPackageManagerInterface },
            new IPackageManagerHandler(sPackageManager)
        );

        /**
         * hook ActivityThread # IPackageManager sPackageManager
         */
        sPackageManagerField.set(currentActivityThread, proxy);

        /**
         * hook Context.getPackageManager() 后得到的 ApplicationPackageManager # IPackageManager mPM
         */
        final PackageManager packageManager = context.getPackageManager();
        final Field mPmField = packageManager.getClass().getDeclaredField("mPM");
        mPmField.setAccessible(true);
        mPmField.set(packageManager, proxy);
    }

}
