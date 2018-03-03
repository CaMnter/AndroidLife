package com.camnter.hook.binder;

import android.content.ClipData;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * BinderHookHandler 的作用就是
 * hook android.content.IClipboard 的一些方法
 *
 * @author CaMnter
 */

public class BinderHookHandler implements InvocationHandler {

    private static final String TAG = "BinderHookHandler";

    // 原始 Service IInterface
    private Object base;


    public BinderHookHandler(@NonNull final IBinder base, @NonNull final Class<?> stubClass) {
        try {
            final Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface",
                IBinder.class);
            this.base = asInterfaceMethod.invoke(null, base);
        } catch (Exception e) {
            throw new RuntimeException("[BinderHookHandler]   hooked failed!");
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        final String methodName = method.getName();

        // 替换剪贴板内容
        if ("getPrimaryClip".equals(methodName)) {
            Log.d(TAG, "[BinderHookHandler]   hook getPrimaryClip");
            return ClipData.newPlainText(null, "hooked");
        }

        // 欺骗系统，使之认为剪贴板上一直有内容
        if ("hasPrimaryClip".equals(methodName)) {
            return true;
        }

        return method.invoke(this.base, args);
    }

}
