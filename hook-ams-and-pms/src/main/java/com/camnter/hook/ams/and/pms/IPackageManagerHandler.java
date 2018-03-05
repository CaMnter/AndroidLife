package com.camnter.hook.ams.and.pms;

import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理 PMS
 *
 * @author CaMnter
 */

public class IPackageManagerHandler implements InvocationHandler {

    private static final String TAG = IPackageManagerHandler.class.getSimpleName();

    Object base;


    IPackageManagerHandler(Object base) {
        this.base = base;
    }


    @SuppressWarnings("DanglingJavadoc")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, "[IPackageManagerHandler]   before method invoke");
        final Object object = method.invoke(this.base, args);
        Log.d(TAG, "[IPackageManagerHandler]   after method invoke");
        return object;
    }

}
