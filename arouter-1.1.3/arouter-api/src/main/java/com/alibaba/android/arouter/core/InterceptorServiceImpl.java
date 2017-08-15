package com.alibaba.android.arouter.core;

import android.content.Context;
import com.alibaba.android.arouter.exception.HandlerException;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.service.InterceptorService;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.alibaba.android.arouter.thread.CancelableCountDownLatch;
import com.alibaba.android.arouter.utils.MapUtils;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.android.arouter.launcher.ARouter.logger;
import static com.alibaba.android.arouter.utils.Consts.TAG;

/**
 * All of interceptors
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/23 下午2:09
 *
 * 拦截器服务，实现了 InterceptorService 接口
 * 作为一个固定的 拦截器服务，固定地址 /arouter/service/interceptor
 */
@Route(path = "/arouter/service/interceptor")
public class InterceptorServiceImpl implements InterceptorService {
    private static boolean interceptorHasInit;
    private static final Object interceptorInitLock = new Object();


    /**
     * 检查是否有拦截器。有，继续下走。无，执行该 InterceptorService 的拦截器回调 onContinue
     * 检查拦截器初始化状态
     *
     * 线程池启动异步线程任务（实质上不会并发，并且在 UI 线程开启的子线程）：
     * 1. 定义一个跟拦截器数量一样的 count 的 CountDownLatch
     * 2. 处理每一个拦截器，并执行拦截器回调
     * 3. 然后，CountDownLatch.await 在该子线程阻塞，等待所有拦截器回调执行完（ 防止拦截器的处理过程有子线程在跑 ）
     * 4. 处理 CountDownLatch 的状态，执行该 InterceptorService 的拦截器回调
     *
     * @param postcard postcard
     * @param callback callback
     */
    @Override
    public void doInterceptions(final Postcard postcard, final InterceptorCallback callback) {
        if (null != Warehouse.interceptors && Warehouse.interceptors.size() > 0) {

            checkInterceptorsInitStatus();

            if (!interceptorHasInit) {
                callback.onInterrupt(
                    new HandlerException("Interceptors initialization takes too much time."));
                return;
            }

            LogisticsCenter.executor.execute(new Runnable() {
                @Override
                public void run() {
                    CancelableCountDownLatch interceptorCounter = new CancelableCountDownLatch(
                        Warehouse.interceptors.size());
                    try {
                        // 处理每一个拦截器，并执行拦截器回调
                        _excute(0, interceptorCounter, postcard);
                        // CountDownLatch.await 在该子线程阻塞，等待所有拦截器回调执行完（ 防止拦截器的处理过程有子线程在跑 ）
                        interceptorCounter.await(postcard.getTimeout(), TimeUnit.SECONDS);
                        if (interceptorCounter.getCount() >
                            0) {    // Cancel the navigation this time, if it hasn't return anythings.
                            callback.onInterrupt(
                                new HandlerException("The interceptor processing timed out."));
                        } else if (null !=
                            postcard.getTag()) {    // Maybe some exception in the tag.
                            callback.onInterrupt(
                                new HandlerException(postcard.getTag().toString()));
                        } else {
                            callback.onContinue(postcard);
                        }
                    } catch (Exception e) {
                        callback.onInterrupt(e);
                    }
                }
            });
        } else {
            callback.onContinue(postcard);
        }
    }


    /**
     * Excute interceptor
     *
     * @param index current interceptor index
     * @param counter interceptor counter
     * @param postcard routeMeta
     *
     * 1. 处理每个拦截器的 process 方法，直到执行完为止
     * 2. 在执行每个拦截器执行 process 时。没有中断的话，都会通知 CountDownLatch 完成
     * 3. 只要有一个拦截器执行 process 时，中断了，CountDownLatch 就会执行 while --count，直到 0 为止
     * 4. 强行解除 CountDownLatch 对上面子线程的阻塞
     */
    private static void _excute(final int index, final CancelableCountDownLatch counter, final Postcard postcard) {
        if (index < Warehouse.interceptors.size()) {
            IInterceptor iInterceptor = Warehouse.interceptors.get(index);
            iInterceptor.process(postcard, new InterceptorCallback() {
                @Override
                public void onContinue(Postcard postcard) {
                    // Last interceptor excute over with no exception.
                    counter.countDown();
                    _excute(index + 1, counter,
                        postcard);  // When counter is down, it will be execute continue ,but index bigger than interceptors size, then U know.
                }


                @Override
                public void onInterrupt(Throwable exception) {
                    // Last interceptor excute over with fatal exception.

                    postcard.setTag(null == exception
                                    ? new HandlerException("No message.")
                                    : exception.getMessage());    // save the exception message for backup.
                    counter.cancel();
                    // Be attention, maybe the thread in callback has been changed,
                    // then the catch block(L207) will be invalid.
                    // The worst is the thread changed to main thread, then the app will be crash, if you throw this exception!
                    //                    if (!Looper.getMainLooper().equals(Looper.myLooper())) {    // You shouldn't throw the exception if the thread is main thread.
                    //                        throw new HandlerException(exception.getMessage());
                    //                    }
                }
            });
        }
    }


    /**
     * 初始化方法
     *
     * 线程池启动异步线程任务
     * 检查是否有拦截器，然后拿出每个拦截器的 class
     * 反射实例化每个一个拦截器实例
     * 然后反射调用每个拦截器 init 方法初始化自身，然后将每个拦截器实例保存在 Warehouse 的 List 内
     *
     * 保存全部实例后，保存标记，用于标识所有拦截器的状态，然后开锁 interceptorInitLock
     * （ 如果在此之前执行了 checkInterceptorsInitStatus 的话，意味着之前执行过 doInterceptions，那个线程处于阻塞和等待，最大时间 10 s ）
     *
     * @param context ctx
     */
    @Override
    public void init(final Context context) {
        LogisticsCenter.executor.execute(new Runnable() {
            @Override
            public void run() {
                if (MapUtils.isNotEmpty(Warehouse.interceptorsIndex)) {
                    for (Map.Entry<Integer, Class<? extends IInterceptor>> entry : Warehouse.interceptorsIndex
                        .entrySet()) {
                        Class<? extends IInterceptor> interceptorClass = entry.getValue();
                        try {
                            IInterceptor iInterceptor = interceptorClass.getConstructor()
                                .newInstance();
                            iInterceptor.init(context);
                            Warehouse.interceptors.add(iInterceptor);
                        } catch (Exception ex) {
                            throw new HandlerException(
                                TAG + "ARouter init interceptor error! name = [" +
                                    interceptorClass.getName() + "], reason = [" + ex.getMessage() +
                                    "]");
                        }
                    }

                    interceptorHasInit = true;

                    logger.info(TAG, "ARouter interceptors init over.");

                    synchronized (interceptorInitLock) {
                        interceptorInitLock.notifyAll();
                    }
                }
            }
        });
    }


    /**
     * 检查是否初始化好了，然后会锁 10 s
     */
    private static void checkInterceptorsInitStatus() {
        synchronized (interceptorInitLock) {
            while (!interceptorHasInit) {
                try {
                    interceptorInitLock.wait(10 * 1000);
                } catch (InterruptedException e) {
                    throw new HandlerException(
                        TAG + "Interceptor init cost too much time error! reason = [" +
                            e.getMessage() + "]");
                }
            }
        }
    }
}
