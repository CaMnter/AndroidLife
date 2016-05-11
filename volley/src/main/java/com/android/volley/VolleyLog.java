/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley;

import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Logging helper class.
 * <p/>
 * to see Volley logs call:<br/>
 * {@code <android-sdk>/platform-tools/adb shell setprop log.tag.Volley VERBOSE}
 */

/*
 * VolleyLog 是 Volley 的一个工具类
 */
public class VolleyLog {

    // VolleyLog 默认 tag
    public static String TAG = "Volley";

    /*
     * VolleyLog 开关
     * level >= INFO时 isLoggable 返回 true，反之则返回 false
     * 所以，改开关 默认 关闭
     *
     * public static final int VERBOSE = 2;
     * public static final int DEBUG = 3;
     * public static final int INFO = 4;
     * public static final int WARN = 5;
     * public static final int ERROR = 6;
     * public static final int ASSERT = 7;
     */
    public static boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);


    /**
     * Customize the log tag for your application, so that other apps
     * using Volley don't mix their logs with yours.
     * <br />
     * Enable the log property for your tag before starting your app:
     * <br />
     * {@code adb shell setprop log.tag.&lt;tag&gt;}
     */

    /*
     * 修改 VolleyLog 的 tag
     */
    public static void setTag(String tag) {
        d("Changing log tag to %s", tag);
        TAG = tag;

        // Reinitialize the DEBUG "constant"
        // 重新设置 VolleyLog 开关，关闭
        DEBUG = Log.isLoggable(TAG, Log.VERBOSE);
    }


    /*
     * Level 没达到 DEBUG
     * 是不打印 VERBOSE
     */
    public static void v(String format, Object... args) {
        if (DEBUG) {
            Log.v(TAG, buildMessage(format, args));
        }
    }


    public static void d(String format, Object... args) {
        Log.d(TAG, buildMessage(format, args));
    }


    public static void e(String format, Object... args) {
        Log.e(TAG, buildMessage(format, args));
    }


    public static void e(Throwable tr, String format, Object... args) {
        Log.e(TAG, buildMessage(format, args), tr);
    }


    public static void wtf(String format, Object... args) {
        Log.wtf(TAG, buildMessage(format, args));
    }


    public static void wtf(Throwable tr, String format, Object... args) {
        Log.wtf(TAG, buildMessage(format, args), tr);
    }


    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(String format, Object... args) {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);

        // 拿到 栈轨迹 数据
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of VolleyLog.
        // It will be at least two frames up, so start there.

        /*
         * 前两条是 VM 和 Thread 的方法所以跳过
         * 后来肯定是进入 某个类 然后调用了 VolleyLog.e(...) d(...) wft(...)
         * 然后进入到 buildMessage(...)
         * 所以 栈轨迹 内一定有 调用 VolleyLog 的类 和 该类的方法
         *
         * 以下循环是为了 寻找 调用类 和 调用方法
         */
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            /*
             * 非 VolleyLog 的类
             * 认定该类 为调用 VolleyLog 的所在类
             */
            if (!clazz.equals(VolleyLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                // 拿到 类名称 和 类方法
                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        // 将 类名称 和 类方法 用于格式化 Log 消息内容
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), caller, msg);
    }


    /**
     * A simple event log with records containing a name, thread ID, and timestamp.
     */

    /*
     * 提供的一个简单的静态内部 Log 类
     */
    static class MarkerLog {

        // 开关状态和 VolleyLog 一样
        public static final boolean ENABLED = VolleyLog.DEBUG;

        /** Minimum duration from first marker to last in an marker log to warrant logging. */
        /** 从第一个标志最短持续时间要持续在标记日志，以保证记录。 */
        private static final long MIN_DURATION_FOR_LOGGING_MS = 0;

        /*
         * 私有静态内部类
         *
         * 用于保存 tagName + 线程Id + 时间戳
         */
        private static class Marker {
            public final String name;
            public final long thread;
            public final long time;


            public Marker(String name, long thread, long time) {
                this.name = name;
                this.thread = thread;
                this.time = time;
            }
        }

        private final List<Marker> mMarkers = new ArrayList<Marker>();
        private boolean mFinished = false;


        /** Adds a marker to this log with the specified name. */
        /*
         * 添加一个 tag - 线程id  保存在一个 Marker 内
         * 并且添加到一个 List 内
         * 常用于 保存 Request.addMarker(tag) 和 Request.finish(tag)
         */
        public synchronized void add(String name, long threadId) {
            if (mFinished) {
                throw new IllegalStateException("Marker added to finished log");
            }

            // 保存一个 Marker
            mMarkers.add(new Marker(name, threadId, SystemClock.elapsedRealtime()));
        }


        /**
         * Closes the log, dumping it to logcat if the time difference between
         * the first and last markers is greater than {@link #MIN_DURATION_FOR_LOGGING_MS}.
         *
         * @param header Header string to print above the marker log.
         */
        /*
         * 关闭日志
         */
        public synchronized void finish(String header) {

            // 设置标记
            mFinished = true;
            // 计算 持续时间
            long duration = getTotalDuration();
            // 持续时间 小于 默认最小值 直接返回
            if (duration <= MIN_DURATION_FOR_LOGGING_MS) {
                return;
            }

            // 拿到 第一个 Marker 的时间戳
            long prevTime = mMarkers.get(0).time;
            d("(%-4d ms) %s", duration, header);

            /*
             * 遍历 Marker 集合
             * 拿到 每一个 Marker 计算这个 Marker 与 此前的一个 Marker 的时间差
             * 然后打印
             */
            for (Marker marker : mMarkers) {
                long thisTime = marker.time;
                d("(+%-4d) [%2d] %s", (thisTime - prevTime), marker.thread, marker.name);
                prevTime = thisTime;
            }
        }

        /*
         * 这个方法的设计是
         * 捕获了请求日志，但是没有输出，就会执行 finalize()，并且抛出异常
         */
        @Override protected void finalize() throws Throwable {
            // Catch requests that have been collected (and hence end-of-lifed)
            // but had no debugging output printed for them.
            if (!mFinished) {
                finish("Request on the loose");
                e("Marker log finalized without finish() - uncaught exit point for request");
            }
        }


        /** Returns the time difference between the first and last events in this log. */
        /*
         *  计算 最先加入的 Marker 和 最后加入的 Marker 的时间差
         */
        private long getTotalDuration() {
            if (mMarkers.size() == 0) {
                return 0;
            }

            long first = mMarkers.get(0).time;
            long last = mMarkers.get(mMarkers.size() - 1).time;
            return last - first;
        }
    }
}
