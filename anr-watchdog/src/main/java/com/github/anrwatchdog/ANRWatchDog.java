package com.github.anrwatchdog;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Salomon BRYS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * A watchdog timer thread that detects when the UI thread has frozen.
 *
 * 自定义的检测 ANR 的 Thread
 *
 * {@link ANRWatchDog#run()}
 * 1. 如果线程没中断，一直循环
 * 2. 记录开始 tick，并且向主线程 post message
 * 3. 线程睡 5s
 * 4. 判断 tick 是不是没变
 * 5. 是的话，认为有 ANR，因为 主线程没处理刚才 post 的消息
 * 6. 然后生成 ANRError，ANRError 中出 dump 对应的 主线程 Thread stack 信息
 * 7. 调用 ANR 回调接口，回传 ANRError，跳出循环
 */
@SuppressWarnings("UnusedDeclaration")
public class ANRWatchDog extends Thread {

    /**
     * ANR 回调接口
     * 返回 ANRError
     */
    public interface ANRListener {
        public void onAppNotResponding(ANRError error);
    }


    /**
     * InterruptedException 回调接口
     */
    public interface InterruptionListener {
        public void onInterrupted(InterruptedException exception);
    }


    /**
     * post message 的间隔时间
     */
    private static final int DEFAULT_ANR_TIMEOUT = 5000;

    /**
     * 默认 ANR 回调接口
     */
    private static final ANRListener DEFAULT_ANR_LISTENER = new ANRListener() {
        @Override
        public void onAppNotResponding(ANRError error) {
            throw error;
        }
    };

    /**
     * 默认 InterruptedException 回调接口
     */
    private static final InterruptionListener DEFAULT_INTERRUPTION_LISTENER
        = new InterruptionListener() {
        @Override
        public void onInterrupted(InterruptedException exception) {
            Log.w("ANRWatchdog", "Interrupted: " + exception.getMessage());
        }
    };

    private ANRListener _anrListener = DEFAULT_ANR_LISTENER;
    private InterruptionListener _interruptionListener = DEFAULT_INTERRUPTION_LISTENER;

    /**
     * 主线程 Handler
     * 时间间隔
     */
    private final Handler _uiHandler = new Handler(Looper.getMainLooper());
    private final int _timeoutInterval;

    private String _namePrefix = "";
    private boolean _logThreadsWithoutStackTrace = false;
    private boolean _ignoreDebugger = false;

    /**
     * 计数器，向主线程 post 消息
     * 被处理的话，_tick 会 + 1
     */
    private volatile int _tick = 0;

    /**
     * 向主线程 post 消息
     */
    private final Runnable _ticker = new Runnable() {
        @Override
        public void run() {
            _tick = (_tick + 1) % Integer.MAX_VALUE;
        }
    };


    /**
     * Constructs a watchdog that checks the ui thread every {@value #DEFAULT_ANR_TIMEOUT}
     * milliseconds
     */
    public ANRWatchDog() {
        this(DEFAULT_ANR_TIMEOUT);
    }


    /**
     * Constructs a watchdog that checks the ui thread every given interval
     *
     * @param timeoutInterval The interval, in milliseconds, between to checks of the UI thread.
     * It is therefore the maximum time the UI may freeze before being reported as ANR.
     */
    public ANRWatchDog(int timeoutInterval) {
        super();
        _timeoutInterval = timeoutInterval;
    }


    /**
     * Sets an interface for when an ANR is detected.
     * If not set, the default behavior is to throw an error and crash the application.
     *
     * @param listener The new listener or null
     * @return itself for chaining.
     */
    public ANRWatchDog setANRListener(ANRListener listener) {
        if (listener == null) {
            _anrListener = DEFAULT_ANR_LISTENER;
        } else {
            _anrListener = listener;
        }
        return this;
    }


    /**
     * Sets an interface for when the watchdog thread is interrupted.
     * If not set, the default behavior is to just log the interruption message.
     *
     * @param listener The new listener or null.
     * @return itself for chaining.
     */
    public ANRWatchDog setInterruptionListener(InterruptionListener listener) {
        if (listener == null) {
            _interruptionListener = DEFAULT_INTERRUPTION_LISTENER;
        } else {
            _interruptionListener = listener;
        }
        return this;
    }


    /**
     * Set the prefix that a thread's name must have for the thread to be reported.
     * Note that the main thread is always reported.
     * Default "".
     *
     * @param prefix The thread name's prefix for a thread to be reported.
     * @return itself for chaining.
     */
    public ANRWatchDog setReportThreadNamePrefix(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        _namePrefix = prefix;
        return this;
    }


    /**
     * Set that only the main thread will be reported.
     *
     * @return itself for chaining.
     */
    public ANRWatchDog setReportMainThreadOnly() {
        _namePrefix = null;
        return this;
    }


    /**
     * Set that all running threads will be reported,
     * even those from which no stack trace could be extracted.
     * Default false.
     *
     * @param logThreadsWithoutStackTrace Whether or not all running threads should be reported
     * @return itself for chaining.
     */
    public ANRWatchDog setLogThreadsWithoutStackTrace(boolean logThreadsWithoutStackTrace) {
        _logThreadsWithoutStackTrace = logThreadsWithoutStackTrace;
        return this;
    }


    /**
     * Set whether to ignore the debugger when detecting ANRs.
     * When ignoring the debugger, ANRWatchdog will detect ANRs even if the debugger is connected.
     * By default, it does not, to avoid interpreting debugging pauses as ANRs.
     * Default false.
     *
     * @param ignoreDebugger Whether to ignore the debugger.
     * @return itself for chaining.
     */
    public ANRWatchDog setIgnoreDebugger(boolean ignoreDebugger) {
        _ignoreDebugger = ignoreDebugger;
        return this;
    }


    /**
     * 1. 如果线程没中断，一直循环
     * 2. 记录开始 tick，并且向主线程 post message
     * 3. 线程睡 5s
     * 4. 判断 tick 是不是没变
     * 5. 是的话，认为有 ANR，因为 主线程没处理刚才 post 的消息
     * 6. 然后生成 ANRError，ANRError 中出 dump 对应的 主线程 Thread stack 信息
     * 7. 调用 ANR 回调接口，回传 ANRError，跳出循环
     */
    @Override
    public void run() {
        setName("|ANR-WatchDog|");

        int lastTick;
        int lastIgnored = -1;
        while (!isInterrupted()) {
            lastTick = _tick;
            _uiHandler.post(_ticker);
            try {
                Thread.sleep(_timeoutInterval);
            } catch (InterruptedException e) {
                _interruptionListener.onInterrupted(e);
                return;
            }

            // If the main thread has not handled _ticker, it is blocked. ANR.
            if (_tick == lastTick) {
                if (!_ignoreDebugger && Debug.isDebuggerConnected()) {
                    if (_tick != lastIgnored) {
                        Log.w("ANRWatchdog",
                            "An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))");
                    }
                    lastIgnored = _tick;
                    continue;
                }

                ANRError error;
                if (_namePrefix != null) {
                    error = ANRError.New(_namePrefix, _logThreadsWithoutStackTrace);
                } else {
                    error = ANRError.NewMainOnly();
                }
                _anrListener.onAppNotResponding(error);
                return;
            }
        }
    }

}