# hook-AMS-and-PMS

<br>
<br>

## 探索 Hook AMS

<br>

熟悉 **Android Activity** 或者 **Service** 启动流程的话，都知道需要和 **AMS** 通信。   
要和远程 **AMS** 通信的话，就需要本地的 **AMS Binder Proxy** 类。   
这，就是 **ActivityManagerNative**。

<br>

**ActivityManagerNative** 部分源码   
   
```java
package android.app;

public abstract class ActivityManagerNative extends Binder implements IActivityManager{
    
    ...
    
    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            IBinder b = ServiceManager.getService("activity");
            if (false) {
                Log.v("ActivityManager", "default service binder = " + b);
            }
            IActivityManager am = asInterface(b);
            if (false) {
                Log.v("ActivityManager", "default service = " + am);
            }
            return am;
        }
    };
    
    ...
    
}
```

<br>

发现这个类的实例化对象藏在 `Singleton<IActivityManager> gDefault` 里。

<br>

**Singleton** 源码

```java
package android.util;
/**
 * Singleton helper class for lazily initialization.
 *
 * Modeled after frameworks/base/include/utils/Singleton.h
 *
 * @hide
 */
public abstract class Singleton<T> {
    private T mInstance;
    
    protected abstract T create();
    
    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
    
}
```

<br>

已经可以确定 **IActivityManager** 在这个 `mInstance` 里。   
也是 **ActivityManagerNative** 的实例，因为 **ActivityManagerNative** 继承了 **IActivityManager**。

<br>
<br>

# Hook AMS 思路

<br>

**1.** 准备好 **IActivityManager** 的 动态代理类。  

**2.** 替换掉 **Singleton** 内缓存的 `IActivityManager mInstance`。

<br>
<br>

# Hook AMS

<br>

**IActivityManagerHandler**   

```java
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
```

<br>

**AMSHooker**   

```java
import android.annotation.SuppressLint;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * @author CaMnter
 */

public final class AMSHooker {

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

        // 动态代理 创建一个 IActivityManager 代理类
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
```

<br>
<br>

## 探索 Hook PMS

<br>

**ContextImpl** 部分源码   
   
```java
package android.app;

class ContextImpl extends Context {
    
    ...
    
    @Override
    public PackageManager getPackageManager() {
        if (mPackageManager != null) {
            return mPackageManager;
        }

        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm != null) {
            // Doesn't matter if we make more than one instance.
            return (mPackageManager = new ApplicationPackageManager(this, pm));
        }

        return null;
    }
    
    ...
    
}
```

<br>

可以发现，**IPackageManager** 先从 **ActivityThread** 获取。然后再包装成 **ApplicationPackageManager**。

<br>

**ActivityThread** 部分源码  

```java
package android.app;

public final class ActivityThread {
    
        ...
    
        public static IPackageManager getPackageManager() {
            if (sPackageManager != null) {
                //Slog.v("PackageManager", "returning cur default = " + sPackageManager);
                return sPackageManager;
            }
            IBinder b = ServiceManager.getService("package");
            //Slog.v("PackageManager", "default service binder = " + b);
            sPackageManager = IPackageManager.Stub.asInterface(b);
            //Slog.v("PackageManager", "default service = " + sPackageManager);
            return sPackageManager;
        }
        
        ...
    
}
```

<br>

在 **ActivityThread** 中，找到了 **Field** `static volatile IPackageManager sPackageManager`。

<br>

**ApplicationPackageManager** 部分源码

```java
package android.app;

public class ApplicationPackageManager extends PackageManager {
    
    ...
    
    private final IPackageManager mPM;
    
    protected ApplicationPackageManager(ContextImpl context,
                              IPackageManager pm) {
        mContext = context;
        mPM = pm;
    }
    
    ...
    
}
```

<br>

在 **ApplicationPackageManager** 中，也找到了 **Field** `private final IPackageManager mPM`。

<br>
<br>

# Hook AMS 思路

<br>

**1.** 准备好 **IPackageManager** 的 动态代理类。  

**2.** 替换掉 **ActivityThread** `static volatile IPackageManager sPackageManager`。   
   
**3.** 替换掉 **ApplicationPackageManager** `private final IPackageManager mPM`。   
   
<br>   
<br>
   
# Hook PMS
    
<br>

**IPackageManagerHandler**

```java
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
```

<br>

**PMSHooker**

```java
/**
 * @author CaMnter
 */

public final class PMSHooker {

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
```

<br>
<br>

# 参考资料

<br>

[Android 插件化原理解析——Hook机制之AMS&PMS](http://weishu.me/2016/03/07/understand-plugin-framework-ams-pms-hook/)  

<br>
<br>


    
    




