package com.camnter.hook.ams.and.pms;

import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理 AMS
 *
 * @author CaMnter
 */

public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = IActivityManagerHandler.class.getSimpleName();

    Object base;


    IActivityManagerHandler(Object base) {
        this.base = base;
    }


    @SuppressWarnings("DanglingJavadoc")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, "[IActivityManagerHandler]   before method invoke");
        final Object object = method.invoke(this.base, args);
        Log.d(TAG, "[IActivityManagerHandler]   after method invoke");
        return object;
    }

}
