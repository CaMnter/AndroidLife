package com.camnter.hook.binder;

import android.os.IBinder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class BinderHookHelper {

    private static final String CLIPBOARD_SERVICE = "clipboard";


    @SuppressWarnings("unchecked")
    public static void hookClipboardService() throws Exception {

        // 反射获取 clipboard 的 IBinder
        final Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
        final Method getServiceMethod = serviceManagerClass.getDeclaredMethod("getService",
            String.class);
        final IBinder rawIBinder = (IBinder) getServiceMethod.invoke(null, CLIPBOARD_SERVICE);

        /**
         * 创建 clipboard 的 IBinder 的动态代理类
         * 作用就是 hook queryLocalInterface
         * 将 public static android.content.IClipboard asInterface(android.os.IBinder obj) 方法
         * 的返回值变为 BinderHookHandler
         */
        final IBinder hookedIBinder = (IBinder) Proxy.newProxyInstance(
            serviceManagerClass.getClassLoader(),
            new Class<?>[] { IBinder.class },
            new BinderProxyHookHandler(rawIBinder)
        );

        // 替换掉 ServiceManager 内的缓存
        final Field sCacheField = serviceManagerClass.getDeclaredField("sCache");
        sCacheField.setAccessible(true);
        Map<String, IBinder> cache = (Map<String, IBinder>) sCacheField.get(null);
        cache.put(CLIPBOARD_SERVICE, hookedIBinder);

    }

}
