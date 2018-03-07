# hook-ams-for-activity-plugin

<br>
<br>

## 进 system_server
 
<br>

**ContextImpl** 部分源码

```java
package android.app;

class ContextImpl extends Context {
    
    ...
    
    @Override
    public void startActivity(Intent intent, Bundle options) {
        warnIfCallingFromSystemProcess();
        // Calling start activity from outside an activity without FLAG_ACTIVITY_NEW_TASK is
        // generally not allowed, except if the caller specifies the task id the activity should
        // be launched in.
        if ((intent.getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) == 0
                && options != null && ActivityOptions.fromBundle(options).getLaunchTaskId() == -1) {
            throw new AndroidRuntimeException(
                    "Calling startActivity() from outside of an Activity "
                    + " context requires the FLAG_ACTIVITY_NEW_TASK flag."
                    + " Is this really what you want?");
        }
        mMainThread.getInstrumentation().execStartActivity(
                getOuterContext(), mMainThread.getApplicationThread(), null,
                (Activity) null, intent, -1, options);
    }
    
    ...
    
}

```

<br>

**Instrumentation** 部分源码

```java
package android.app;

public class Instrumentation {
    
    ...

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        
        ...
        
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManagerNative.getDefault()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
    
    ...

}
```

<br>

进 **system_server**，是从 `ContextImpl # startActivity` -> `Instrumentation # execStartActivity` -> `ActivityManagerNative # startActivity`。   

这样走到 **AMS** 的远程 **Binder Proxy** 对象，然后和 **AMS** 通信，进入 **system_server** 进程。   
在  **system_server** 进程中，一直辗转于 **ActivityStackSupervisor**, **ActivityStarter** 以及 **ActivityStack** 之间的方法调用。  
    
完成了：   

**1.** 校验启动的 **Activity** 的正确性 和 信息。比如，是否在 **AndroidManifest** 文件里。      

**2.** 创建 **ActivityRecord**。   
   
**3.** **Activity** 在 **AMS** 的栈管理。
  
**4.** 是否为该 **Activity** 启动新的进程 等等。   

<br>

**ActivityStarter** 部分源码

```java
package com.android.server.am;

class ActivityStarter {
    
    ...
    
    final int startActivityLocked(...){
        
        ...
        
        if (err == ActivityManager.START_SUCCESS && aInfo == null) {
            // We couldn't find the specific class specified in the Intent.
            // Also the end of the line.
            err = ActivityManager.START_CLASS_NOT_FOUND;
        }
        
        ...
        
    }
    
    ...
    
}
```

<br>

**Instrumentation** 部分源码

```java
package android.app;

public class Instrumentation {
    
    ...

    public static void checkStartActivityResult(int res, Object intent) {
        
        ...
        
        switch (res) {
            
            ...
            
            case ActivityManager.START_CLASS_NOT_FOUND:
                            if (intent instanceof Intent && ((Intent)intent).getComponent() != null)
                                throw new ActivityNotFoundException(
                                        "Unable to find explicit activity class "
                                        + ((Intent)intent).getComponent().toShortString()
                                        + "; have you declared this activity in your AndroidManifest.xml?");
                            throw new ActivityNotFoundException(
                                    "No Activity found to handle " + intent);
            
            ...
            
        }
        
        ...
        
    }
    
    ...

}
```

**上部分结论**：**AndroidManifest** 文件的校验，在 **AMS** 所在进程 **system_server**。所以，在这个校验过程中，
**AndroidManifest** 必须存在要校验的 **Activity**。如果不存在，就入上述源码一样。最终，回到 `Instrumentation # checkStartActivityResult` 中报错。

<br>
<br>

## 出 system_server

<br>

在 **ActivityStackSupervisor**, **ActivityStarter** 以及 **ActivityStack** 之间的方法调用 到最后。     
出 **system_server** ，是从 `ActivityStackSupervisor # realStartActivityLocked` 开始的。   

<br>

**ActivityStackSupervisor** 部分源码

```java
package com.android.server.am;

public final class ActivityStackSupervisor implements DisplayListener {
    
    ...
    
    final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app,
                boolean andResume, boolean checkConfig) throws RemoteException {
        
                ...
        
                app.forceProcessStateUpTo(mService.mTopProcessState);
                app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
                        System.identityHashCode(r), r.info, new Configuration(mService.mConfiguration),
                        new Configuration(task.mOverrideConfig), r.compat, r.launchedFromPackage,
                        task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, results,
                        newIntents, !andResume, mService.isNextTransitionForward(), profilerInfo);
                
                ...
                
        }
    
    ...
    
}
```

<br>

`app.thread` 是 **ActivityThread** 的内部类 **ApplicationThread**。  
   
**ActivityThread** 部分源码

```java
public final class ActivityThread {

    ...

    private class ApplicationThread extends ApplicationThreadNative {
    
        ...
        
        public final void scheduleLaunchActivity(...){
            
            ActivityClientRecord r = new ActivityClientRecord();
            
            ...
            
            sendMessage(H.LAUNCH_ACTIVITY, r);
        }
        
        ...
    
    }
    
    ...
    
    private class H extends Handler {
        
        ...
        
        public static final int LAUNCH_ACTIVITY         = 100;
        
        ...
        
        public void handleMessage(Message msg) {
            if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
            switch (msg.what) {
                case LAUNCH_ACTIVITY: {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
                    
                    r.packageInfo = getPackageInfoNoCheck(
                            r.activityInfo.applicationInfo, r.compatInfo);
                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                }
            } break;
            
            ...
            
        }
        
        ...
        
    }
    
    ...
    
    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        
        ...
        
        Activity a = performLaunchActivity(r, customIntent);
        
        ...
        
    }
    
    ...
    
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent){
        
        ...
        
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = r.packageInfo.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(activity, e)) {
                throw new RuntimeException(
                    "Unable to instantiate activity " + component
                    + ": " + e.toString(), e);
            }
        }
        
        try {
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
            
            ...
            
            activity.attach(appContext, this, getInstrumentation(), r.token,
                    r.ident, app, r.intent, r.activityInfo, title, r.parent,
                    r.embeddedID, r.lastNonConfigurationInstances, config,
                    r.referrer, r.voiceInteractor, window);
            
            ...
                
            if (r.isPersistable()) {
                mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
            } else {
                mInstrumentation.callActivityOnCreate(activity, r.state);
            }
            
            ...
                
            r.paused = true;
            mActivities.put(r.token, r);
        } catch (SuperNotCalledException e) {
            throw e;
        } catch (Exception e) {
            ...
        }
        return activity;
    }

}
```

<br>

出 **system_server**，是从 `ActivityStackSupervisor # realStartActivityLocked` -> `ApplicationThread # scheduleLaunchActivity`。  
   
这样，就调用了 **App 进程** 的远程 **Binder Proxy** 对象，**ApplicationThread**。从而，离开了 **AMS** 所在的进程 **system_server**。
       
<br>       
   
然后，在 **App 进程** 上，走了 `H # handleMessage` -> `ActivityThread # handleLaunchActivity` -> `ActivityThread # performLaunchActivity` -> `Instrumentation # callActivityOnCreate`    

这样的话，就回到了 **Instrumentation**，回到了 `startActivity` 的起点。
   
<br>

**Instrumentation** 部分源码   

```java
public class Instrumentation {
    
    ...
    
    /**
     * Perform calling of an activity's {@link Activity#onCreate}
     * method.  The default implementation simply calls through to that method.
     *
     * @param activity The activity being created.
     * @param icicle The previously frozen state (or null) to pass through to onCreate().
     */
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        prePerformCreate(activity);
        activity.performCreate(icicle);
        postPerformCreate(activity);
    }
    
    ...
    
}
```

**Activity** 部分源码   
   
```java
public class Activity extends ContextThemeWrapper
        implements LayoutInflater.Factory2,
        Window.Callback, KeyEvent.Callback,
        OnCreateContextMenuListener, ComponentCallbacks2,
        Window.OnWindowDismissedCallback, WindowControllerCallback {
        
    ...
    
    final void performCreate(Bundle icicle) {
        
        ...
        
        onCreate(icicle);
        
        ...
        
    }
    
    ...
    
}
```
   
<br>
   
可以看得出，再次回到 **Instrumentation** 的时候，就是回调了 **Activity** 的生命周期。
   
<br>   

**下部分结论**：从 **system_server** 进程回来后，可以再 `ActivityThread # H` 获取到 **LAUNCH_ACTIVITY** 消息。从而，可以获取到 **ActivityClientRecord**。
**最重要的是** `ActivityThread # H` 采用的消息分发是 **最低优先级** **覆写** `Handler # handleMessage`。

<br>

`ActivityThread # H` 采用的消息分发是 **最低优先级** ？   

   
**Handler** 部分源码

```java
public class Handler {
    
    ...
    
    final Callback mCallback;
    
    ...
    
    /**
     * Handle system messages here.
     */
    public void dispatchMessage(Message msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }
    
    ...
    
}
```

<br>

**第一优先级** `msg.callback`。   

**第二优先级** `mCallback`。   

**最低优先级** `handleMessage`。

<br>

`msg.callback` 和 `mCallback` 可以提前拦截到 **LAUNCH_ACTIVITY** 消息。  
但是，`msg.callback` 和 发出的 **Message** 有关，无从下手。   
所以，`ActivityThread # H` 中，可以添加 `mCallback`，完成拦截 **LAUNCH_ACTIVITY** 消息的工作。

<br>
<br>

# Hook AMS for activity plugin 思路

<br>

## 进 system_server

<br>

**1.** **AndroidManifest** 写入一个插桩 **Activity**。

**2.** 准备好 **IActivityManager** 的 动态代理类。  

**3.** **IActivityManager** 的 动态代理类，去完成将 **插件 Activity Intent** 替换为 **插桩 Activity Intent**。拿着 **插桩 Activity Intent** 去交给 **AMS**。  
  
**4.** **插件 Activity Intent** 作为 **插桩 Activity Intent** 额外参数，进行保存。

**5.** **Hook AMS**。

<br>

## 出 system_server

<br>

**6.** 准备好一个 **Handler.Callback** 对象。   
   
**7.** **Handler.Callback** 对象，内接收 **LAUNCH_ACTIVITY** 消息。抽出消息中的 **ActivityClientRecord**。

**8.** **ActivityClientRecord** 中拿出 **Intent**。再从 **Intent** 中拿出 **4.** 中保存的 **插件 Activity Intent**。

**9.** 替换 **ActivityClientRecord** 中拿出的 **Intent** 中的 **Component**。改为 **插件 Activity Component**。

**10.** 拿着 **Handler.Callback** 对象去 **Hook** `ActivityThread # H # mCallback`。   

<br>
<br>

# Hook AMS for activity plugin

<br>

**IActivityManagerHandler**  
 
```java
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
        /**
         * API 25:
         *
         * public interface IActivityManager extends IInterface {
         *
         *     ...
         *
         *     public int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
         *             String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
         *             ProfilerInfo profilerInfo, Bundle options) throws RemoteException;
         *
         *     ...
         *
         * }
         */
        if ("startActivity".equals(method.getName())) {
            final Pair<Integer, Intent> integerIntentPair = foundFirstIntentOfArgs(args);
            final Intent rawIntent = integerIntentPair.second;

            /**
             * 判断是否是插件 Activity
             */
            if (rawIntent != null) {
                final ActivityInfo activityInfo = ActivityInfoUtils.selectPluginActivity(
                    SmartApplication.getActivityInfoMap(), rawIntent);
                if (activityInfo != null) {
                    // 插件 Activity
                    // 代理 StubActivity 包名
                    final String proxyServicePackageName = SmartApplication.getContext()
                        .getPackageName();

                    // 启动的 Activity 替换为 StubActivity
                    final Intent intent = new Intent();
                    final ComponentName componentName = new ComponentName(proxyServicePackageName,
                        StubActivity.class.getName());
                    intent.setComponent(componentName);

                    // 保存原始 启动插件的 Activity Intent
                    intent.putExtra(AMSHooker.EXTRA_TARGET_INTENT, rawIntent);

                    // 替换掉 Intent, 欺骗 AMS
                    args[integerIntentPair.first] = intent;
                    Log.v(TAG,
                        "[IActivityManagerHandler]   [startActivity]   hook method startActivity success");
                }
            }
            return method.invoke(this.base, args);
        }
        return method.invoke(this.base, args);
    }


    /**
     * 寻找 参数里面的第一个Intent 对象
     *
     * @param args args
     * @return Pair
     */
    private Pair<Integer, Intent> foundFirstIntentOfArgs(Object... args) {
        int index = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Intent) {
                index = i;
                break;
            }
        }
        return Pair.create(index, (Intent) args[index]);
    }

}

```

<br>

**AMSHooker**  

```java
/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class AMSHooker {

    static final String EXTRA_TARGET_INTENT = "extra_target_intent";


    /**
     * Hook ActivityManagerNative 这个 Binder 中的 AMS
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
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

        final Field gDefaultField;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        } else {
            Class<?> activityManagerNativeClass = Class.forName(
                "android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
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


    /**
     * Hook ActivityThread 中 H 这个 Handler
     * 添加 mCallback
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    public static void hookActivityThreadH() throws ClassNotFoundException,
                                                    NoSuchMethodException,
                                                    InvocationTargetException,
                                                    IllegalAccessException,
                                                    NoSuchFieldException {
        /**
         * *****************************************************************************************
         *
         * ActivityThread 部分源码
         *
         * public final class ActivityThread {
         *
         *      ...
         *
         *      private class H extends Handler {
         *
         *          ...
         *
         *      }
         *
         *      ...
         *
         * }
         *
         * *****************************************************************************************
         *
         * Handler 部分源码
         *
         * public class Handler {
         *
         *     ...
         *
         *     final Callback mCallback;
         *
         *     public void dispatchMessage(Message msg) {
         *         if (msg.callback != null) {
         *             handleCallback(msg);
         *         } else {
         *            if (mCallback != null) {
         *                if (mCallback.handleMessage(msg)) {
         *                     return;
         *                }
         *            }
         *            handleMessage(msg);
         *         }
         *     }
         *
         *     ...
         *
         * }
         */

        /**
         * 获取 ActivityThread
         */
        final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        final Field currentActivityThreadField = activityThreadClass.getDeclaredField(
            "sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        final Object currentActivityThread = currentActivityThreadField.get(null);

        /**
         * 获取 ActivityThread # final H mH = new H()
         */
        final Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        final Handler mH = (Handler) mHField.get(currentActivityThread);

        /**
         * Hook 掉 H # mCallback
         */
        final Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);
        mCallBackField.set(mH, new HCallback(mH));
    }

}
```

<br>

**HCallback**  

```java
/**
 * 启动 Activity 时
 *
 * ContextImpl # execStartActivity -> Instrumentation # execStartActivity -> IActivityManager #
 * startActivity
 *
 * 进入了 AMS 所在的进程 system_server
 *
 * 然后，在该进程一直辗转与 ActivityStackSupervisor <-> ActivityStack
 *
 * 要回到 App 进程的时候，system_server 通过 ApplicationThread 这个 Binder proxy 对象回到 App 进程
 * 然后通过 H（ Handler ）类分发消息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * public final class ActivityThread {
 *
 * -    ...
 *
 * -    private class H extends Handler {
 *
 * -        ...
 *
 * -        public static final int LAUNCH_ACTIVITY         = 100;
 *
 * -        ...
 *
 * -        public void handleMessage(Message msg) {
 * -            switch (msg.what) {
 * -                case LAUNCH_ACTIVITY: {
 * -                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
 * -                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
 *
 * -                    r.packageInfo = getPackageInfoNoCheck(r.activityInfo.applicationInfo,
 * -                                                          r.compatInfo);
 * -                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
 * -                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
 * -            } break;
 *
 * -            ...
 *
 * -        }
 *
 * -        ...
 *
 * -    }
 *
 * -    ...
 *
 * }
 *
 * -------------------------------------------------------------------------------------------------
 *
 * 得在 system_server 的消息回来的时候，拦截这个 ApplicationThread 发出的  LAUNCH_ACTIVITY 消息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * public class Handler {
 *
 * -    ...
 *
 * -    public void dispatchMessage(Message msg) {
 * -        if (msg.callback != null) {
 * -            handleCallback(msg);
 * -        } else {
 * -            if (mCallback != null) {
 * -                if (mCallback.handleMessage(msg)) {
 * -                    return;
 * -                }
 * -            }
 * -            handleMessage(msg);
 * -        }
 * -    }
 *
 * -    ...
 *
 * }
 *
 * -------------------------------------------------------------------------------------------------
 *
 * 庆幸的是，H 是选择 handleMessage(msg) 这种最低优先级的消息分发策略
 * 所以可以选择，在 H 添加 mCallback。这样就比 handleMessage(msg) 优先级高
 * 可以拦截到 system_server 回到 App 进程后，ApplicationThread 发出的 LAUNCH_ACTIVITY 消息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * HCallback 就是为了填补 H # mCallback 而存在的
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class HCallback implements Handler.Callback {

    private static final int LAUNCH_ACTIVITY = 100;

    private Handler h;


    public HCallback(Handler h) {
        this.h = h;
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                this.handleLaunchActivity(msg);
                break;
        }

        this.h.handleMessage(msg);
        return true;
    }


    private void handleLaunchActivity(@NonNull final Message msg) {
        /**
         * public final class ActivityThread {
         *
         * -    ...
         *
         * -    private class H extends Handler {
         *
         * -        ...
         *
         * -        public static final int LAUNCH_ACTIVITY         = 100;
         *
         * -        ...
         *
         * -        public void handleMessage(Message msg) {
         * -            switch (msg.what) {
         * -                case LAUNCH_ACTIVITY: {
         * -                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
         * -                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
         *
         * -                    r.packageInfo = getPackageInfoNoCheck(r.activityInfo.applicationInfo, r.compatInfo);
         * -                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
         * -                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
         * -            } break;
         *
         * -            ...
         *
         * -        }
         *
         * -        ...
         *
         * -    }
         *
         * -    ...
         *
         * }
         */

        /**
         * final ActivityClientRecord r = (ActivityClientRecord) msg.obj
         */
        try {
            final Object r = msg.obj;
            final Field intentField = r.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            final Intent intent = (Intent) intentField.get(r);

            /**
             * 取出 AMS 动态代理对象放入的 启动插件的 Activity Intent
             */
            final Intent rawIntent = intent.getParcelableExtra(AMSHooker.EXTRA_TARGET_INTENT);

            /**
             * 判断是否是插件 Activity
             */
            if (rawIntent != null) {
                final ActivityInfo activityInfo = ActivityInfoUtils.selectPluginActivity(
                    SmartApplication.getActivityInfoMap(), rawIntent);
                if (activityInfo != null) {
                    /**
                     * 替换启动的插件 Activity
                     *
                     * system_server 那边只知道的是 StubActivity
                     * 回到 App 进程时，附带的信息也是 StubActivity
                     *
                     * 这里将 StubActivity 换为 插件 Activity
                     *
                     * 保证了 App 进程这边一直都是 插件 Activity
                     */
                    intent.setComponent(rawIntent.getComponent());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
```

<br>

**BaseDexClassLoaderHooker**

```java
/**
 * DexElements 插桩
 *
 * @author CaMnter
 */

public final class BaseDexClassLoaderHooker {

    @SuppressWarnings("DanglingJavadoc")
    public static void patchClassLoader(@NonNull final ClassLoader classLoader,
                                        @NonNull final File apkFile,
                                        @NonNull final File optDexFile)
        throws IllegalAccessException,
               NoSuchMethodException,
               IOException,
               InvocationTargetException,
               InstantiationException,
               NoSuchFieldException,
               ClassNotFoundException {

        // 获取 BaseDexClassLoader # DexPathList pathList
        final Field pathListField = DexClassLoader.class.getSuperclass()
            .getDeclaredField("pathList");
        pathListField.setAccessible(true);
        final Object pathList = pathListField.get(classLoader);

        // 获取 DexPathList # Element[] dexElements
        final Field dexElementArray = pathList.getClass().getDeclaredField("dexElements");
        dexElementArray.setAccessible(true);
        final Object[] dexElements = (Object[]) dexElementArray.get(pathList);

        // Element 类型
        final Class<?> elementClass = dexElements.getClass().getComponentType();

        // 用于替换 PathList # Element[] dexElements
        final Object[] newElements = (Object[]) Array.newInstance(elementClass,
            dexElements.length + 1);

        /**
         * <= 4.0.0
         *
         * no method
         *
         * >= 4.0.0
         *
         * Element(File file, ZipFile zipFile, DexFile dexFile)
         *
         * ---
         *
         * >= 5.0.0
         *
         * Element(File file, boolean isDirectory, File zip, DexFile dexFile)
         *
         * ---
         *
         * >= 8.0.0
         *
         * @Deprecated
         * Element(File dir, boolean isDirectory, File zip, DexFile dexFile)
         * Element(DexFile dexFile, File dexZipPath)
         *
         */
        final int sdkVersion = Build.VERSION.SDK_INT;

        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new RuntimeException(
                "[BaseDexClassLoaderHooker]   the sdk version must >= 14 (4.0.0)");
        }

        final Object element;
        final Constructor<?> constructor;

        if (sdkVersion >= Build.VERSION_CODES.O) {
            // >= 8.0.0
            // DexFile dexFile, File dexZipPath
            constructor = elementClass.getConstructor(
                DexFile.class,
                File.class
            );
            element = constructor.newInstance(
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0),
                apkFile
            );
        } else if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // >= 5.0.0
            // File file, boolean isDirectory, File zip, DexFile dexFile
            constructor = elementClass.getConstructor(
                File.class,
                boolean.class,
                File.class,
                DexFile.class
            );
            element = constructor.newInstance(
                apkFile,
                false,
                apkFile,
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0)
            );
        } else {
            // >= 4.0.0
            // File file, ZipFile zipFile, DexFile dexFile
            constructor = elementClass.getConstructor(
                File.class,
                ZipFile.class,
                DexFile.class
            );
            element = constructor.newInstance(
                apkFile,
                new ZipFile(apkFile),
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0)
            );
        }

        final Object[] toAddElementArray = new Object[] { element };
        // 把原始的 elements 复制进去
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);
        // 把插件的 element  复制进去
        System.arraycopy(toAddElementArray, 0, newElements, dexElements.length,
            toAddElementArray.length);

        // 替换
        dexElementArray.set(pathList, newElements);
    }

}
```

<br>

**ActivityInfoUtils**

```java
/**
 * 收集 apk 内的 activity 信息
 *
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public final class ActivityInfoUtils {

    /**
     * 匹配 ActivityInfo 缓存
     *
     * @param pluginIntent 插件 Intent
     * @return 插件 intent 的 ActivityInfo
     */
    public static ActivityInfo selectPluginActivity(final Map<ComponentName, ActivityInfo> activityInfoMap, Intent pluginIntent) {
        for (ComponentName componentName : activityInfoMap.keySet()) {
            if (componentName.equals(pluginIntent.getComponent())) {
                return activityInfoMap.get(componentName);
            }
        }
        return null;
    }


    /**
     * 解析 Apk 文件中的 <activity>
     * 并缓存
     *
     * 主要 调用 PackageParser 类的 generateActivityInfo 方法
     *
     * @param apkFile apkFile
     * @throws Exception exception
     */
    @SuppressLint("PrivateApi")
    public static Map<ComponentName, ActivityInfo> preLoadActivities(@NonNull final File apkFile, final Context context)
        throws Exception {

        final Map<ComponentName, ActivityInfo> activityInfoMap = new HashMap<>();

        /**
         * 反射 获取 PackageParser # parsePackage(File packageFile, int flags)
         */
        final Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");

        /**
         * <= 4.0.0
         *
         * Don't deal with
         *
         * >= 4.0.0
         *
         * parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
         *
         * ---
         *
         * >= 5.0.0
         *
         * parsePackage(File packageFile, int flags)
         *
         */
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new RuntimeException(
                "[ActivityInfoUtils]   the sdk version must >= 14 (4.0.0)");
        }

        final Object packageParser;
        final Object packageObject;
        final Method parsePackageMethod;

        if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // >= 5.0.0
            // parsePackage(File packageFile, int flags)
            /**
             * 反射创建 PackageParser 对象，无参数构造
             *
             * 反射 调用 PackageParser # parsePackage(File packageFile, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            packageParser = packageParserClass.newInstance();

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                PackageManager.GET_ACTIVITIES
            );
        } else {
            // >= 4.0.0
            // parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
            /**
             * 反射创建 PackageParser 对象，PackageParser(String archiveSourcePath)
             *
             * 反射 调用 PackageParser # parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags)
             * 获取 apk 文件对应的 Package 对象
             */
            final String apkFileAbsolutePath = apkFile.getAbsolutePath();
            packageParser = packageParserClass.getConstructor(String.class)
                .newInstance(apkFileAbsolutePath);

            parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",
                File.class, String.class, DisplayMetrics.class, int.class);
            packageObject = parsePackageMethod.invoke(
                packageParser,
                apkFile,
                apkFile.getAbsolutePath(),
                context.getResources().getDisplayMetrics(),
                PackageManager.GET_ACTIVITIES
            );
        }

        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // >= 4.2.0
            // generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            /**
             * 反射调用 UserHandle # static @UserIdInt int getCallingUserId()
             * 获取到 userId
             *
             * 反射创建 PackageUserState 对象
             */
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Class<?> packageUserStateClass = Class.forName(
                "android.content.pm.PackageUserState");
            final Class<?> userHandler = Class.forName("android.os.UserHandle");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Object defaultUserState = packageUserStateClass.newInstance();

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class, packageUserStateClass, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags, PackageUserState state, int userId)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0,
                    defaultUserState,
                    userId
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // >= 4.1.0
            // generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Class<?> userHandler = Class.forName("android.os.UserId");
            final Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            final int userId = (Integer) getCallingUserIdMethod.invoke(null);
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class, boolean.class, int.class, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags, boolean stopped, int enabledState, int userId)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 在之前版本的 4.0.0 中 存在着
             * public class PackageParser {
             *     public final static class Package {
             *         // User set enabled state.
             *         public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
             *
             *         // Whether the package has been stopped.
             *         public boolean mSetStopped = false;
             *     }
             * }
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0,
                    false,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    userId
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        } else if (sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // >= 4.0.0
            // generateActivityInfo(Activity a, int flags)
            /**
             * 读取 Package # ArrayList<Activity> activities
             * 通过 ArrayList<Activity> activities 获取 Activity 对应的 ActivityInfo
             */
            final Field activitiesField = packageObject.getClass().getDeclaredField("activities");
            final List activities = (List) activitiesField.get(packageObject);

            // 需要调用 android.content.pm.PackageParser#generateActivityInfo(Activity a, int flags)
            final Class<?> packageParser$ActivityClass = Class.forName(
                "android.content.pm.PackageParser$Activity");
            final Method generateActivityInfo = packageParserClass.getDeclaredMethod(
                "generateActivityInfo",
                packageParser$ActivityClass, int.class);

            /**
             * 反射调用 PackageParser # generateActivityInfo(Activity a, int flags)
             * 解析出 Activity 对应的 ActivityInfo
             *
             * 然后保存
             */
            for (Object activity : activities) {
                final ActivityInfo info = (ActivityInfo) generateActivityInfo.invoke(
                    packageParser,
                    activity,
                    0
                );
                activityInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }
        }

        return activityInfoMap;

    }

}
```

<br>

**SmartApplication**

```java
/**
 * @author CaMnter
 */

public class SmartApplication extends Application {

    private static Context BASE;

    private static Map<ComponentName, ActivityInfo> activityInfoMap;


    /**
     * Set the base context for this ContextWrapper.  All calls will then be
     * delegated to the base context.  Throws
     * IllegalStateException if a base context has already been set.
     *
     * @param base The new base context for this wrapper.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BASE = base;
        try {
            // Hook AMS
            AMSHooker.hookActivityManagerNative();
            // Hook H
            AMSHooker.hookActivityThreadH();
            // assets 的 hook-ams-for-activity-plugin-plugin.apk 拷贝到 /data/data/files/[package name]
            AssetsUtils.extractAssets(base, "hook-ams-for-activity-plugin-plugin.apk");
            final File apkFile = getFileStreamPath("hook-ams-for-activity-plugin-plugin.apk");
            final File odexFile = getFileStreamPath("hook-ams-for-activity-plugin-plugin.odex");
            // // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHooker.patchClassLoader(this.getClassLoader(), apkFile, odexFile);
            activityInfoMap = ActivityInfoUtils.preLoadActivities(apkFile, base);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Context getContext() {
        return BASE;
    }


    public static Map<ComponentName, ActivityInfo> getActivityInfoMap() {
        return activityInfoMap;
    }

}
```

<br>
<br>

# 启动插件 Activity

<br>

```java
/**
 * Called when a view has been clicked.
 *
 * @param v The view that was clicked.
 */
@Override
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.start_first_text:
            this.startFirstText.setEnabled(false);
            try {
                this.startActivity(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.ams.f.activity.plugin.plugin",
                        "com.camnter.hook.ams.f.activity.plugin.plugin.FirstPluginActivity")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.startFirstText.setEnabled(true);
            break;
        case R.id.start_second_text:
            this.startSecondText.setEnabled(false);
            try {
                this.startActivity(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.ams.f.activity.plugin.plugin",
                        "com.camnter.hook.ams.f.activity.plugin.plugin.SecondPluginActivity")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.startSecondText.setEnabled(true);
            break;
    }
}
```

<br>
<br>

# 参考资料

<br>

[Android 插件化原理解析——Activity生命周期管理](http://weishu.me/2016/03/21/understand-plugin-framework-activity-management/)  

<br>
<br>

