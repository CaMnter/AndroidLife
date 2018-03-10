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

        if ("getPackageInfo".equals(method.getName())) {
            // IPackageManager # getPackageInfo 永远不为 null
            return new PackageInfo();
        }
        return method.invoke(this.base, args);
    }

}
