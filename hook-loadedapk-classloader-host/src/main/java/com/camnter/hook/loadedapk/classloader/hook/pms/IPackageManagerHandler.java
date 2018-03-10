package com.camnter.hook.loadedapk.classloader.hook.pms;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理 PMS
 *
 * @author CaMnter
 */

public class IPackageManagerHandler implements InvocationHandler {

    private Object base;


    IPackageManagerHandler(@NonNull final Object base) {
        this.base = base;
    }


    @SuppressWarnings("DanglingJavadoc")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /**
         * *****************************************************************************************
         *
         * LoadedApk 部分源码
         *
         * public final class LoadedApk {
         *
         *      ...
         *
         *      public Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation) {
         *
         *          ...
         *
         *          try {
         *              java.lang.ClassLoader cl = getClassLoader();
         *              if (!mPackageName.equals("android")) {
         *                  Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
         *                          "initializeJavaContextClassLoader");
         *                  initializeJavaContextClassLoader();
         *                  Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
         *              }
         *              ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
         *              app = mActivityThread.mInstrumentation.newApplication(
         *                      cl, appClass, appContext);
         *              appContext.setOuterContext(app);
         *          } catch (Exception e) {
         *              ...
         *          }
         *
         *          ...
         *
         *      }
         *
         *      ...
         *
         *      private void initializeJavaContextClassLoader() {
         *           IPackageManager pm = ActivityThread.getPackageManager();
         *           android.content.pm.PackageInfo pi;
         *           try {
         *               pi = pm.getPackageInfo(mPackageName, PackageManager.MATCH_DEBUG_TRIAGED_MISSING,
         *                       UserHandle.myUserId());
         *           } catch (RemoteException e) {
         *               throw e.rethrowFromSystemServer();
         *           }
         *           if (pi == null) {
         *               throw new IllegalStateException("Unable to get package info for "
         *                       + mPackageName + "; is package not installed?");
         *           }
         *
         *           ...
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         */

        /**
         * LoadedApk # initializeJavaContextClassLoader 中的 IPackageManager # getPackageInfo
         *
         * 如果是插件 LoadedApk
         * 那么是我们通过 hook 和 反射创建出来的，内部的 PMS 也是取 ActivityThread 中的 PMS
         * 就是宿主 PMS
         *
         * 这里开始就是加载非 android 开头的 package
         * 如果用宿主的 PMS 去加载自己的 PackageInfo 是 OK 的
         *
         * 但是加载 插件的 PackageInfo 是 null
         *
         * 为了欺骗 PMS，为了不抛出上述源码中的 "package not installed?" 异常
         *
         * 选择 检查 IPackageManager # getPackageInfo 的值
         * 如果为 null，暂且视为 宿主 PMS 加载插件 PackageInfo
         * 强行 new 一个 PackageInfo
         *
         * 骗过 PMS，跳过验证
         */

        if ("getPackageInfo".equals(method.getName())) {
            final Object tryToValue = method.invoke(this.base, args);
            return null == tryToValue ? new PackageInfo() : tryToValue;
        }
        return method.invoke(this.base, args);
    }

}
