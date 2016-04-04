package com.camnter.newlife.utils;

import android.os.Looper;

/**
 * Description：ThreadUtil
 * Created by：CaMnter
 * Time：2015-12-01 12:22
 */
public class ThreadUtils {

    private static final String MAIN_THREAD_MSG = "MainThread-%d: %s";
    private static final String CHILD_THREAD_MSG = "ChildThread-%d: %s";


    /**
     * Determine whether to the main thread
     * 判断是否为主线程
     *
     * @return true or false
     */
    public static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }


    /**
     * Formatting information, formatting and thread information
     * 格式化信息，格式化后有线程信息
     *
     * @param info info
     * @return msg
     */
    public static String getThreadMsg(String info) {
        if (ThreadUtils.isMainThread()) {
            return String.format(MAIN_THREAD_MSG, Thread.currentThread().getId(), info);
        } else {
            return String.format(CHILD_THREAD_MSG, Thread.currentThread().getId(), info);
        }
    }
}
