/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.didi.virtualapk.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Pair;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * 用于与 UI 线程通信的工具
 *
 * Created by renyugang on 16/11/10.
 */
public class RunUtil {

    private static final int MESSAGE_RUN_ON_UITHREAD = 0x1;

    /**
     * UI 线程 Handler
     */
    private static Handler sHandler;


    /**
     * execute a runnable on ui thread, then return immediately. see also {@link
     * #runOnUiThread(Runnable, boolean)}
     *
     * @param runnable the runnable prepared to run
     */
    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, false);
    }


    /**
     * execute a runnable on ui thread
     *
     * 1. 如果是主线程，直接 run 后返回
     * 2. 发送消息
     * 3. 判断 runnable 是否要按顺序完成，表示着是否要阻塞 runOnUiThread(...)
     * 4. 初始化一个 size = 1 的 CountDownLatch，await() 直接锁上 runOnUiThread(...)
     * 5. 等待 UI 线程 Handler 那边打开 CountDownLatch 的锁，才会不阻塞 runOnUiThread(...)
     *
     * @param runnable the runnable prepared to run
     * @param waitUtilDone if set true, the caller thread will wait until the specific runnable
     * finished.
     */
    public static void runOnUiThread(Runnable runnable, boolean waitUtilDone) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }

        CountDownLatch countDownLatch = null;
        if (waitUtilDone) {
            countDownLatch = new CountDownLatch(1);
        }
        Pair<Runnable, CountDownLatch> pair = new Pair<>(runnable, countDownLatch);
        getHandler().obtainMessage(MESSAGE_RUN_ON_UITHREAD, pair).sendToTarget();
        if (waitUtilDone) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 为了不自己实例化一个线程池
     *
     * 利用了 AsyncTask 共用的那个静态线程池 AsyncTask.THREAD_POOL_EXECUTOR
     * 但是这个线程池并不是阻塞的那个 AsyncTask.SERIAL_EXECUTOR
     *
     * 这个是非阻塞的
     *
     * @return Executor
     */
    public static Executor getThreadPool() {
        return AsyncTask.THREAD_POOL_EXECUTOR;
    }


    /**
     * 根据进程 id 获取进程 name
     *
     * @param context context
     * @param pid pid
     * @return 进程 name
     */
    public static String getProcessNameByPid(Context context, int pid) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
            Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessList
            = manager.getRunningAppProcesses();
        if (appProcessList != null) {
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
                if (pid == appProcessInfo.pid) {
                    return appProcessInfo.processName;
                }
            }
        }

        return null;
    }


    /**
     * 判断是否是主进程
     *
     * @param context context
     * @return boolean
     */
    public static boolean isMainProcess(Context context) {
        String processName = getProcessNameByPid(context, Process.myPid());
        if (context.getPackageName().equals(processName)) {
            return true;
        }

        return false;
    }


    /**
     * 获取 UI Handler 单例
     *
     * @return Handler
     */
    private static Handler getHandler() {
        synchronized (RunUtil.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler();
            }
            return sHandler;
        }
    }


    /**
     * 自定义 UI Handler
     *
     * 唯一不同的是
     * 需要操作 CountDownLatch
     */
    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }


        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_RUN_ON_UITHREAD) {
                Pair<Runnable, CountDownLatch> pair = (Pair<Runnable, CountDownLatch>) msg.obj;
                Runnable runnable = pair.first;
                runnable.run();
                if (pair.second != null) {
                    pair.second.countDown();
                }
            }
        }
    }

}
