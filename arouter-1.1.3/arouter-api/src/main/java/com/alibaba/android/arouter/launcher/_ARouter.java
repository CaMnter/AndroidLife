package com.alibaba.android.arouter.launcher;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.android.arouter.core.InstrumentationHook;
import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.exception.HandlerException;
import com.alibaba.android.arouter.exception.InitException;
import com.alibaba.android.arouter.exception.NoRouteFoundException;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.facade.service.AutowiredService;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.alibaba.android.arouter.facade.service.InterceptorService;
import com.alibaba.android.arouter.facade.service.PathReplaceService;
import com.alibaba.android.arouter.facade.template.ILogger;
import com.alibaba.android.arouter.thread.DefaultPoolExecutor;
import com.alibaba.android.arouter.utils.Consts;
import com.alibaba.android.arouter.utils.DefaultLogger;
import com.alibaba.android.arouter.utils.TextUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ARouter core (Facade patten)
 *
 * 外观模式 - 核心
 * 仅提供 ARouter 调用，
 * 相对的，ARouter 的对应方法都没有自己的实现，都是调用 _ARouter 的对应方法
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/16 14:39
 *
 * {@link _ARouter#init(Application)}
 * 1. 反射初始化所有 JavaPoet 生成 com.alibaba.android.arouter.routes 包下的 类
 * -  然后初始化缓存所有的 路由组，拦截器 和 service 的 RouteMeta
 *
 * 2. 设置  初始化标记
 *
 * {@link _ARouter#destroy()}
 * 销毁方法（ 只有在 debug 模式下有效 ）
 *
 * 1. 设置  初始化标记
 * 2. 清空所有缓存
 *
 * {@link _ARouter#attachBaseContext()}
 * Hook 当前进程中的 ActivityThread.mInstrumentation 为 InstrumentationHook（已废弃）
 *
 * InstrumentationHook 自定义了 Instrumentation 类，覆写了 newActivity 方法
 * 除了，执行原 newActivity 的 cl.loadClass(className).newInstance() 外
 *
 * 仅仅为了拿到该 Activity 的实例，然后反射 field，获取 intent 内传过来的规定结构的 String[]
 * 进行反射 field 赋值
 *
 * 即使是 private 的 field 也会被设置为 public，然后赋值
 *
 * 该类是一个 hook 类，之前版本被用来 hook 掉 ActivityThread 中的 field mInstrumentation
 * 然后，在每次 Activity 被打开的时候，会自动反射 field 赋值
 *
 * 老版本的自动注入方式（通过 hook Instrumentation，已废弃）
 *
 * {@link _ARouter#inject(Object)}
 * 自动注入
 *
 * 在 Activity onCreate 的时候调用
 *
 * 此方法的出现，才废弃了
 * {@link _ARouter#attachBaseContext()}
 * {@link InstrumentationHook}
 * {@link com.alibaba.android.arouter.core.AutowiredLifecycleCallback}
 *
 * 会获取到 JavaPoet 为该 Activity 生成的 ISyringe 类
 * 然后反射构造该 ISyringe 实例，调用 注入 方法
 *
 * {@link _ARouter#build(String)}
 * String path 构造一个 Postcard，同时提取出 group
 * 并且 会提前 调用 地址预处理 service
 *
 * {@link _ARouter#build(Uri)}
 * uri 构造一个 Postcard，同时提取出 group
 * 并且 会提前 调用 地址预处理 service
 *
 * {@link _ARouter#build(String, String)}
 * 真正的 构造 Postcard 方法
 * 并且 会提前 调用 地址预处理 service
 *
 * {@link _ARouter#extractGroup(String)}
 * 从 路径 中，提取出 group
 * group：第一个 / 与 第二个 / 之间的内容
 *
 * {@link _ARouter#afterInit()}
 * 初始化 拦截器 service
 *
 * {@link _ARouter#navigation(Class)}
 * 根据 service class 进行跳转 （ 通常用于获取一个 service ）
 *
 * 1. 通过一个 服务 name 获取一个 关系类
 * 2. 往 关系类 中，添加参数。反射实例化所需要的 IProvider，并初始化
 * 3. 在执行第二步的时候，已经将 IProvider 放入 关系类中。所以，这里从 关系类 中拿到 IProvider
 * 4. 强转 IProvider 为泛型 T （ 具体 service ）
 *
 * {@link _ARouter#navigation(Context, Postcard, int, NavigationCallback)}
 * 跳转 （ 通常用于打开 Activity ）
 *
 * 1. 往 关系类 中，添加参数。反射实例化所需要的 IProvider，并初始化
 * 2. 第一步失败了，失败回调，还有执行 降级 service，最后 return，不走以下
 *
 * 3. 回调成功
 * 4.1 判断是否是 绿色通道。不是，执行 拦截器 service。拦截器全部没拦截才 返回 Activity
 * 4.2 是 绿色通道。返回 Activity
 *
 * {@link _ARouter#_navigation(Context, Postcard, int, NavigationCallback)}
 * 真正的跳转 （ 通常用于打开 Activity ）
 *
 * 1. 拿到 context，如果当前传入的 Activity context 为 null，就拿 Application context
 * 2. 然后，根据 路由数据类型 分发逻辑：
 *
 * 2.1 Activity 类型，如果 context 不是 Activity，就添加 flag = FLAG_ACTIVITY_NEW_TASK
 * -   切到主线程，start Activity 和 设置 activity 转场动画
 * 2.2 Provider 类型，返回 IProvider 对象，然后转为 Object
 * 2.3 BoardCast 或 ContentProvider 或 Fragment 类型，会反射构造一个该类型的实例
 * -   如果是，Fragment 类型的话，会添加额外的数据到 bundle 里
 * 2.4 Method 或 Service 或 default，目前 没有处理，return null
 */
final class _ARouter {
    static ILogger logger = new DefaultLogger(Consts.TAG); // 日志工具
    private volatile static boolean monitorMode = false;
    private volatile static boolean debuggable = false;
    private volatile static boolean autoInject = false;
    private volatile static _ARouter instance = null;
    private volatile static boolean hasInit = false;
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    private static Context mContext;

    private static InterceptorService interceptorService;


    private _ARouter() {
    }


    /**
     * 初始化
     *
     * 调用 {@link LogisticsCenter#init}
     * 1. 反射初始化所有 JavaPoet 生成 com.alibaba.android.arouter.routes 包下的 类
     * -  然后初始化缓存所有的 路由组，拦截器 和 service 的 RouteMeta
     *
     * 2. 设置  初始化标记
     *
     * @param application application
     * @return boolean
     */
    protected static synchronized boolean init(Application application) {
        mContext = application;
        LogisticsCenter.init(mContext, executor);
        logger.info(Consts.TAG, "ARouter init success!");
        hasInit = true;

        // It's not a good idea.
        // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        //     application.registerActivityLifecycleCallbacks(new AutowiredLifecycleCallback());
        // }
        return true;
    }


    /**
     * Destroy arouter, it can be used only in debug mode.
     *
     * 销毁方法（ 只有在 debug 模式下有效 ）
     *
     * 1. 设置  初始化标记
     * 2. 清空所有缓存
     */
    static synchronized void destroy() {
        if (debuggable()) {
            hasInit = false;
            LogisticsCenter.suspend();
            logger.info(Consts.TAG, "ARouter destroy success!");
        } else {
            logger.error(Consts.TAG, "Destroy can be used in debug mode only!");
        }
    }


    /**
     * 单例方法
     *
     * @return _ARouter
     */
    protected static _ARouter getInstance() {
        if (!hasInit) {
            throw new InitException("ARouterCore::Init::Invoke init(context) first!");
        } else {
            if (instance == null) {
                synchronized (_ARouter.class) {
                    if (instance == null) {
                        instance = new _ARouter();
                    }
                }
            }
            return instance;
        }
    }


    /**
     * 打开 debug
     */
    static synchronized void openDebug() {
        debuggable = true;
        logger.info(Consts.TAG, "ARouter openDebug");
    }


    /**
     * 打开 log
     */
    static synchronized void openLog() {
        logger.showLog(true);
        logger.info(Consts.TAG, "ARouter openLog");
    }


    /**
     * 打开 自动注入（ 已废弃 ）
     */
    @Deprecated
    static synchronized void enableAutoInject() {
        autoInject = true;
    }


    /**
     * 获取 是否自动注入（ 已废弃 ）
     *
     * @return boolean
     */
    @Deprecated
    static boolean canAutoInject() {
        return autoInject;
    }


    /**
     * Hook 当前进程中的 ActivityThread.mInstrumentation 为 InstrumentationHook（已废弃）
     *
     * InstrumentationHook 自定义了 Instrumentation 类，覆写了 newActivity 方法
     * 除了，执行原 newActivity 的 cl.loadClass(className).newInstance() 外
     *
     * 仅仅为了拿到该 Activity 的实例，然后反射 field，获取 intent 内传过来的规定结构的 String[]
     * 进行反射 field 赋值
     *
     * 即使是 private 的 field 也会被设置为 public，然后赋值
     *
     * 该类是一个 hook 类，之前版本被用来 hook 掉 ActivityThread 中的 field mInstrumentation
     * 然后，在每次 Activity 被打开的时候，会自动反射 field 赋值
     *
     * 老版本的自动注入方式（通过 hook Instrumentation，已废弃）
     */
    @Deprecated
    static void attachBaseContext() {
        Log.i(Consts.TAG, "ARouter start attachBaseContext");
        try {
            Class<?> mMainThreadClass = Class.forName("android.app.ActivityThread");

            // Get current main thread.
            Method getMainThread = mMainThreadClass.getDeclaredMethod("currentActivityThread");
            getMainThread.setAccessible(true);
            Object currentActivityThread = getMainThread.invoke(null);

            // The field contain instrumentation.
            Field mInstrumentationField = mMainThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);

            // Hook current instrumentation
            mInstrumentationField.set(currentActivityThread, new InstrumentationHook());
            Log.i(Consts.TAG, "ARouter hook instrumentation success!");
        } catch (Exception ex) {
            Log.e(Consts.TAG, "ARouter hook instrumentation failed! [" + ex.getMessage() + "]");
        }
    }


    /**
     * log 打印更多信息的开关（ ThreadId, ThreadName, FileName, ClassName, MethodName 和 LineNumber）
     */
    static synchronized void printStackTrace() {
        logger.showStackTrace(true);
        logger.info(Consts.TAG, "ARouter printStackTrace");
    }


    /**
     * 设置 线程池，业务层可以替换 ARouter 内的 线程池
     *
     * @param tpe tpe
     */
    static synchronized void setExecutor(ThreadPoolExecutor tpe) {
        executor = tpe;
    }


    /**
     * 打开 监控模式
     */
    static synchronized void monitorMode() {
        monitorMode = true;
        logger.info(Consts.TAG, "ARouter monitorMode on");
    }


    /**
     * 是否是 监控模式
     *
     * @return boolean
     */
    static boolean isMonitorMode() {
        return monitorMode;
    }


    /**
     * 是否是 Debug 模式
     *
     * @return boolean
     */
    static boolean debuggable() {
        return debuggable;
    }


    /**
     * 设置 ILogger，业务层可以替换 ARouter 内的 ILogger
     *
     * @param userLogger ILogger
     */
    static void setLogger(ILogger userLogger) {
        if (null != userLogger) {
            logger = userLogger;
        }
    }


    /**
     * 自动注入
     *
     * 在 Activity onCreate 的时候调用
     *
     * 此方法的出现，才废弃了
     * {@link _ARouter#attachBaseContext()}
     * {@link InstrumentationHook}
     * {@link com.alibaba.android.arouter.core.AutowiredLifecycleCallback}
     *
     * 会获取到 JavaPoet 为该 Activity 生成的 ISyringe 类
     * 然后反射构造该 ISyringe 实例，调用 注入 方法
     *
     * @param thiz activity
     */
    static void inject(Object thiz) {
        AutowiredService autowiredService = ((AutowiredService) ARouter.getInstance()
            .build("/arouter/service/autowired")
            .navigation());
        if (null != autowiredService) {
            autowiredService.autowire(thiz);
        }
    }


    /**
     * Build postcard by path and default group
     *
     * String path 构造一个 Postcard，同时提取出 group
     * 并且 会提前 调用 地址预处理 service
     *
     * @param path path
     * @return Postcard
     */
    protected Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new HandlerException(Consts.TAG + "Parameter is invalid!");
        } else {
            PathReplaceService pService = ARouter.getInstance()
                .navigation(PathReplaceService.class);
            if (null != pService) {
                path = pService.forString(path);
            }
            return build(path, extractGroup(path));
        }
    }


    /**
     * Build postcard by uri
     *
     * uri 构造一个 Postcard，同时提取出 group
     * 并且 会提前 调用 地址预处理 service
     *
     * @param uri uri
     * @return Postcard
     */
    protected Postcard build(Uri uri) {
        if (null == uri || TextUtils.isEmpty(uri.toString())) {
            throw new HandlerException(Consts.TAG + "Parameter invalid!");
        } else {
            PathReplaceService pService = ARouter.getInstance()
                .navigation(PathReplaceService.class);
            if (null != pService) {
                uri = pService.forUri(uri);
            }
            return new Postcard(uri.getPath(), extractGroup(uri.getPath()), uri, null);
        }
    }


    /**
     * Build postcard by path and group
     *
     * 真正的 构造 Postcard 方法
     * 并且 会提前 调用 地址预处理 service
     *
     * @param path path
     * @param group group
     * @return Postcard
     */
    protected Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new HandlerException(Consts.TAG + "Parameter is invalid!");
        } else {
            PathReplaceService pService = ARouter.getInstance()
                .navigation(PathReplaceService.class);
            if (null != pService) {
                path = pService.forString(path);
            }
            return new Postcard(path, group);
        }
    }


    /**
     * Extract the default group from path.
     *
     * 从 路径 中，提取出 group
     *
     * group：第一个 / 与 第二个 / 之间的内容
     *
     * @param path path
     * @return group
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new HandlerException(Consts.TAG +
                "Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }

        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new HandlerException(Consts.TAG +
                    "Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            logger.warning(Consts.TAG, "Failed to extract default group! " + e.getMessage());
            return null;
        }
    }


    /**
     * 初始化 拦截器 service
     */
    static void afterInit() {
        // Trigger interceptor init, use byName.
        interceptorService = (InterceptorService) ARouter.getInstance()
            .build("/arouter/service/interceptor")
            .navigation();
    }


    /**
     * 根据 service class 进行跳转 （ 通常用于获取一个 service ）
     *
     * 1. 通过一个 服务 name 获取一个 关系类
     * 2. 往 关系类 中，添加参数。反射实例化所需要的 IProvider，并初始化
     * 3. 在执行第二步的时候，已经将 IProvider 放入 关系类中。所以，这里从 关系类 中拿到 IProvider
     * 4. 强转 IProvider 为泛型 T （ 具体 service ）
     *
     * @param service service
     * @param <T> Class<? extends T>
     * @return T T
     */
    protected <T> T navigation(Class<? extends T> service) {
        try {
            Postcard postcard = LogisticsCenter.buildProvider(service.getName());

            // Compatible 1.0.5 compiler sdk.
            if (null == postcard) { // No service, or this service in old version.
                postcard = LogisticsCenter.buildProvider(service.getSimpleName());
            }

            LogisticsCenter.completion(postcard);
            return (T) postcard.getProvider();
        } catch (NoRouteFoundException ex) {
            logger.warning(Consts.TAG, ex.getMessage());
            return null;
        }
    }


    /**
     * Use router navigation.
     *
     * 跳转 （ 通常用于打开 Activity ）
     *
     * 1. 往 关系类 中，添加参数。反射实例化所需要的 IProvider，并初始化
     * 2. 第一步失败了，失败回调，还有执行 降级 service，最后 return，不走以下
     *
     * 3. 回调成功
     * 4.1 判断是否是 绿色通道。不是，执行 拦截器 service。拦截器全部没拦截才 返回 Activity
     * 4.2 是 绿色通道。返回 Activity
     *
     * @param context Activity or null.
     * @param postcard Route metas
     * @param requestCode RequestCode
     * @param callback cb
     */
    protected Object navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        try {
            LogisticsCenter.completion(postcard);
        } catch (NoRouteFoundException ex) {
            logger.warning(Consts.TAG, ex.getMessage());

            if (debuggable()) { // Show friendly tips for user.
                Toast.makeText(mContext, "There's no route matched!\n" +
                    " Path = [" + postcard.getPath() + "]\n" +
                    " Group = [" + postcard.getGroup() + "]", Toast.LENGTH_LONG).show();
            }

            if (null != callback) {
                callback.onLost(postcard);
            } else {    // No callback for this invoke, then we use the global degrade service.
                DegradeService degradeService = ARouter.getInstance()
                    .navigation(DegradeService.class);
                if (null != degradeService) {
                    degradeService.onLost(context, postcard);
                }
            }

            return null;
        }

        if (null != callback) {
            callback.onFound(postcard);
        }

        if (!postcard.isGreenChannel()) {   // It must be run in async thread, maybe interceptor cost too mush time made ANR.
            interceptorService.doInterceptions(postcard, new InterceptorCallback() {
                /**
                 * Continue process
                 *
                 * @param postcard route meta
                 */
                @Override
                public void onContinue(Postcard postcard) {
                    _navigation(context, postcard, requestCode, callback);
                }


                /**
                 * Interrupt process, pipeline will be destory when this method called.
                 *
                 * @param exception Reson of interrupt.
                 */
                @Override
                public void onInterrupt(Throwable exception) {
                    if (null != callback) {
                        callback.onInterrupt(postcard);
                    }

                    logger.info(Consts.TAG, "Navigation failed, termination by interceptor : " +
                        exception.getMessage());
                }
            });
        } else {
            return _navigation(context, postcard, requestCode, callback);
        }

        return null;
    }


    /**
     * 真正的跳转 （ 通常用于打开 Activity ）
     *
     * 1. 拿到 context，如果当前传入的 Activity context 为 null，就拿 Application context
     * 2. 然后，根据 路由数据类型 分发逻辑：
     *
     * 2.1 Activity 类型，如果 context 不是 Activity，就添加 flag = FLAG_ACTIVITY_NEW_TASK
     * -   切到主线程，start Activity 和 设置 activity 转场动画
     * 2.2 Provider 类型，返回 IProvider 对象，然后转为 Object
     * 2.3 BoardCast 或 ContentProvider 或 Fragment 类型，会反射构造一个该类型的实例
     * -   如果是，Fragment 类型的话，会添加额外的数据到 bundle 里
     * 2.4 Method 或 Service 或 default，目前 没有处理，return null
     *
     * @param context context
     * @param postcard postcard
     * @param requestCode requestCode
     * @param callback callback
     * @return Activity
     */
    private Object _navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        final Context currentContext = null == context ? mContext : context;

        switch (postcard.getType()) {
            case ACTIVITY:
                // Build intent
                final Intent intent = new Intent(currentContext, postcard.getDestination());
                intent.putExtras(postcard.getExtras());

                // Set flags.
                int flags = postcard.getFlags();
                if (-1 != flags) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {    // Non activity, need less one flag.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                // Navigation in main looper.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (requestCode > 0) {  // Need start for result
                            ActivityCompat.startActivityForResult((Activity) currentContext, intent,
                                requestCode, postcard.getOptionsBundle());
                        } else {
                            ActivityCompat.startActivity(currentContext, intent,
                                postcard.getOptionsBundle());
                        }

                        if ((0 != postcard.getEnterAnim() || 0 != postcard.getExitAnim()) &&
                            currentContext instanceof Activity) {    // Old version.
                            ((Activity) currentContext).overridePendingTransition(
                                postcard.getEnterAnim(), postcard.getExitAnim());
                        }

                        if (null != callback) { // Navigation over.
                            callback.onArrival(postcard);
                        }
                    }
                });

                break;
            case PROVIDER:
                return postcard.getProvider();
            case BOARDCAST:
            case CONTENT_PROVIDER:
            case FRAGMENT:
                Class fragmentMeta = postcard.getDestination();
                try {
                    Object instance = fragmentMeta.getConstructor().newInstance();
                    if (instance instanceof Fragment) {
                        ((Fragment) instance).setArguments(postcard.getExtras());
                    } else if (instance instanceof android.support.v4.app.Fragment) {
                        ((android.support.v4.app.Fragment) instance).setArguments(
                            postcard.getExtras());
                    }

                    return instance;
                } catch (Exception ex) {
                    logger.error(Consts.TAG, "Fetch fragment instance error, " +
                        TextUtils.formatStackTrace(ex.getStackTrace()));
                }
            case METHOD:
            case SERVICE:
            default:
                return null;
        }

        return null;
    }
}
