package com.alibaba.android.arouter.thread;

import android.support.annotation.NonNull;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.android.arouter.utils.Consts;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工厂类
 *
 * @author zhilong <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 15/12/25 上午10:51
 */
public class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;


    /**
     * 获取当前线程的线程组
     * 初始化 线程名前缀
     */
    public DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "ARouter task pool No." + poolNumber.getAndIncrement() + ", thread No.";
    }


    /**
     * 初始化一个线程
     *
     * 守护线程是运行在后台的一种特殊进程，它独立于控制终端，并且周期性地执行某种任务或着等待处理某些发生的事件
     * 也就是在程序运行的时候在后台提供一种通用服务的线程，在没有用户线程客服务时会自动离开。
     *
     * 守护线程生命周期：守护线程并不是程序中不可缺少的部分。当所有的非守护线程即用户线程结束，程序也就终止了
     * 同时还会kill掉进程中的所有守护线程。
     *
     * 守护线程的优先级比较低，用于为系统中的其它对象和线程提供服务。
     *
     * 1. 初始化一个线程
     * 2. 判断线程是不是守护线程，是的话，设为非守护线程（ 用户线程 ）
     * 3. 设置线程优先级为 normal
     * 4. 设置线程捕获异常 Handler
     *
     * @param runnable runnable
     * @return Thread
     */
    public Thread newThread(@NonNull Runnable runnable) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        ARouter.logger.info(Consts.TAG, "Thread production, name is [" + threadName + "]");
        Thread thread = new Thread(group, runnable, threadName, 0);
        if (thread.isDaemon()) {   //设为非后台线程
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) { //优先级为normal
            thread.setPriority(Thread.NORM_PRIORITY);
        }

        // 捕获多线程处理中的异常
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ARouter.logger.info(Consts.TAG,
                    "Running task appeared exception! Thread [" + thread.getName() +
                        "], because [" + ex.getMessage() + "]");
            }
        });
        return thread;
    }
}