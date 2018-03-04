# hook-binder

<br>
<br>

## 探索

<br>

```java
public final class ServiceManager {
    
    ...
    
    public static IBinder getService(String name) {
        try {
            IBinder service = sCache.get(name);
            if (service != null) {
                return service;
            } else {
                return getIServiceManager().getService(name);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
        }
        return null;
    }
    
    ...
    
}
```

发现 **IBinder** 都会缓存在 **sCache** 内。

<br>

在 **IClipboard.aidl** 的生成类 **IClipboard.java** 中

```java
public interface IClipboard extends android.os.IInterface {
    
    ...
    
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder
        implements android.app.IApplicationThread {
    
        public static android.content.IClipboard asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null; 
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR); 
            if (((iin != null) && (iin instanceof android.content.IClipboard))) {
                return ((android.content.IClipboard) iin);
            }
            return new android.content.IClipboard.Stub.Proxy(obj);
        }
    
    }
    
    ...
    
}
```

<br>

发现 **aidl** 生成的代码中，asInterface 先 `queryLocalInterface` 查询是否是当前进程的接口。  
不是的话，会返回远程代理对象，就是跨进程访问的代理对象。  

**打算**：**hook** `queryLocalInterface` 为 **永远不为 null 的动态代理对象**。
 
<br>
<br>

## Hook 点

**1.** `android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);` 让这里的 `queryLocalInterface` 永远返回 动态代理对象（ **BinderHookHandler** ）。   
**2.** 这样，永远走到 `if` 内，`asInterface(android.os.IBinder obj)` 方法永远返回 动态代理对象（ **BinderHookHandler** ）。   
**3.** 为了完成 **1** 和 **2**，需要在 `obj.queryLocalInterface(DESCRIPTOR)` 里，构造这个 `obj` 的 动态代理对象（ **BinderProxyHookHandler** ）。   
**4.** `obj.queryLocalInterface(DESCRIPTOR)` 中的 `obj` 是 **IBinder** 类型。即，**sCache** 内的缓存数据。   
**5.** 由于  **IBinder** 都会缓存在 **sCache** 内。所以，将 `obj` 的 动态代理对象（ **BinderProxyHookHandler** ）覆盖掉 `sCache` 内的对应缓存。   

<br>
<br>

# Hook

<br>

```java
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
```

<br>


```java
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
 * public interface IClipboard extends android.os.IInterface {
 *
 * -    ...
 * 
 * -    public static abstract class Stub extends android.os.Binder
 * -        implements android.app.IApplicationThread {
 *
 * -        public static android.content.IClipboard asInterface(android.os.IBinder obj) {
 * -            if ((obj == null)) {
 * -               return null;
 * -            }
 * -            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
 * -            if (((iin != null) && (iin instanceof android.content.IClipboard))) {
 * -                return ((android.content.IClipboard) iin);
 * -            }
 * -            return new android.content.IClipboard.Stub.Proxy(obj);
 * -        }
 * 
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
```

<br>

```java
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
```

<br>
<br>



