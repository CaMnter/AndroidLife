package com.camnter.newlife.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description：ThreadPoolUtil
 * Created by：CaMnter
 * Time：2015-11-25 11:12
 */
public class ThreadPoolUtils {

    /**
     * Create a single thread thread pool.The thread pool is only one thread in
     * work, which is equivalent to a single thread serial performs all tasks.If
     * this is the only thread for abnormal end, then there will be a new thread
     * to replace it.The thread pool to ensure the executing order of tasks was
     * all submitted order according to the task.
     * 创建一个单线程的线程池。这个线程池只有一个线程在工作,也就是相当于单线程串行执行所有任务.
     * 如果这个唯一的线程因为异常结束,那么会有一个新的线程来替代它.此线程池保证所有任务的执行顺
     * 序按照任务的提交顺序执.
     *
     * @return ExecutorService
     */
    public static ExecutorService getSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }


    /**
     * To create a fixed-size pool.Every time to submit a task to create a
     * thread, thread until reach the maximum size of the thread pool.Once the
     * thread pool size maximum will remain the same, if a thread end because of
     * abnormal execution, so the thread pool will make up a new thread
     * 创建固定大小的线程池.每次提交一个任务就创建一个线程,直到线程达到线程池的最大大小.
     * 线程池的大小一旦达到最大值就会保持不变,如果某个线程因为执行异常而结束,那么线程池
     * 会补充一个新线程.
     *
     * @param count thread count
     * @return ExecutorService
     */
    public static ExecutorService getFixedThreadPool(int count) {
        return Executors.newFixedThreadPool(count);
    }


    /**
     * To create a cache of the thread pool.If the thread pool size than the
     * thread processing task need, Part will be recycling idle threads (60
     * seconds to perform a task), when the number of jobs increased and the
     * thread pool can be smart to add a new thread to handle the task.The
     * thread pool to the thread pool size do not limit, the thread pool size is
     * wholly dependent on the operating system (or the JVM) to create the
     * biggest thread size.
     * 创建一个可缓存的线程池.如果线程池的大小超过了处理任务所需要的线程,那么就会回收部分
     * 空闲（60秒不执行任务）的线程,当任务数增加时,此线程池又可以智能的添加新线程来处理任
     * 务.此线程池不会对线程池大小做限制,线程池大小完全依赖于操作系统（或者说JVM）能够创建
     * 的最大线程大小.
     *
     * @return ExecutorService
     */
    public static ExecutorService getCachedThreadPool() {
        return Executors.newCachedThreadPool();
    }


    /**
     * Create a limitless thread pool size.The thread pool support regular and
     * periodic mission requirements.
     * 创建一个大小无限的线程池.此线程池支持定时以及周期性执行任务的需求.
     *
     * @param corePoolSize corePoolSize
     * @return ExecutorService
     */
    public static ExecutorService getScheduledThreadPool(int corePoolSize) {
        return Executors.newScheduledThreadPool(corePoolSize);
    }
}
