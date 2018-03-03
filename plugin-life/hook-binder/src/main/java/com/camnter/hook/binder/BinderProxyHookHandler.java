package com.camnter.hook.binder;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * ServiceManager # HashMap<String, IBinder> sCache
 * IBinder 的类型基本上都是 BinderProxy
 * 因为 ServiceManager 会将这些 BinderProxy 通过 asInterface 转为接口
 *
 * asInterface 会判断 BinderProxy 是否在本进程有 Binder 对象
 *
 * public static IBinder getService(String name) {
 * -    try {
 * -        IBinder service = sCache.get(name);
 * -        if (service != null) {
 * -            return service;
 * -        } else {
 * -            return getIServiceManager().getService(name);
 * -        }
 * -    } catch (RemoteException e) {
 * -        Log.e(TAG, "error in getService", e);
 * -    }
 * -    return null;
 * }
 *
 * public interface IBinder {
 *
 * -    ...
 *
 * -    public static android.content.IClipboard asInterface(android.os.IBinder obj) {
 * -        if ((obj == null)) {
 * -            return null;
 * -        }
 * -        android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
 * -        if (((iin != null) && (iin instanceof android.content.IClipboard))) {
 * -            return ((android.content.IClipboard) iin);
 * -        }
 * -        return new android.content.IClipboard.Stub.Proxy(obj);
 * -    }
 *
 * -    ...
 *
 * }
 *
 * BinderProxyHookHandler 的作用就是 hook queryLocalInterface
 * 将 public static android.content.IClipboard asInterface(android.os.IBinder obj) 方法
 * 的返回值变为 BinderHookHandler
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class BinderProxyHookHandler implements InvocationHandler {

    private static final String TAG = "BinderProxyHookHandler";

    private IBinder base;
    private Class<?> stub;
    private Class<?> iinterface;


    @SuppressLint("PrivateApi")
    public BinderProxyHookHandler(@NonNull final IBinder base) {
        this.base = base;
        try {
            this.stub = Class.forName("android.content.IClipboard$Stub");
            this.iinterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("queryLocalInterface".equals(method.getName())) {
            /**
             * android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
             * 将 iin 变为 BinderHookHandler
             *
             * BinderHookHandler 代理对象指定了接口类型 (android.content.IClipboard)
             *
             * 所以可以 return ((android.content.IClipboard) iin);
             *
             * 然后再一次动态代理
             */
            Log.d(TAG, "[BinderProxyHookHandler]   hook queryLocalInterface");
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                new Class[] { this.iinterface },
                new BinderHookHandler(this.base, this.stub));
        }
        return method.invoke(base, args);
    }

}
