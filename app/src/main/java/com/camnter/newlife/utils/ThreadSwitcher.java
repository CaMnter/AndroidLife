/*
 * Copyright (c) 2015, 张涛.
 * Copyright (c) 2016, CaMnter.
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

package com.camnter.newlife.utils;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Description：ThreadSwitcher
 * Modified from：https://github.com/kymjs/Common/blob/master/Common/common/src/main/java/com/kymjs/common/function/ThreadSwitch.java
 * Created by：CaMnter
 */

public final class ThreadSwitcher extends Thread {

    private static final int MAX_SIZE = 16;
    private static final int GLOBAL_MAX_SIZE = 206;

    private final BlockingQueue<Runnable> runnableQueue;
    private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());
    private static final Break BREAK_TASK = new Break() {
        @Override public void run() {

        }
    };


    private static class GlobalThreadSwitcher {
        private static final ThreadSwitcher GLOBAL_THREAD_SWITCHER = new ThreadSwitcher(
            GLOBAL_MAX_SIZE);
    }


    private ThreadSwitcher() {
        this(MAX_SIZE);
    }


    private ThreadSwitcher(int maxSize) {
        this.start();
        // >= 5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.runnableQueue = new LinkedTransferQueue<>();
        } else {
            this.runnableQueue = new LinkedBlockingQueue<>(maxSize);
        }
    }


    public static ThreadSwitcher getGlobalThreadSwitcher() {
        return GlobalThreadSwitcher.GLOBAL_THREAD_SWITCHER;
    }


    public static ThreadSwitcher newInstance() {
        return new ThreadSwitcher();
    }


    public static ThreadSwitcher newInstance(final int maxSize) {
        return new ThreadSwitcher(maxSize <= 0 ? MAX_SIZE : maxSize);
    }


    public ThreadSwitcher io(final IO io) {
        this.runnableQueue.add(io);
        return this;
    }


    public ThreadSwitcher ui(final UI ui) {
        this.runnableQueue.add(ui);
        return this;
    }


    public ThreadSwitcher breakTask() {
        this.runnableQueue.add(BREAK_TASK);
        return this;
    }


    @Override public void run() {
        super.run();
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            try {
                final Runnable task = this.runnableQueue.take();
                if (task != null) {
                    if (task instanceof IO) {
                        task.run();
                    } else if (task instanceof UI) {
                        MAIN_THREAD_HANDLER.post(task);
                    } else if (task instanceof Break) {
                        this.runnableQueue.clear();
                        if (this != GlobalThreadSwitcher.GLOBAL_THREAD_SWITCHER) {
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }


    /**
     * UI task abstract interface
     */
    public interface UI extends Runnable {}


    /**
     * IO task abstract interface
     */
    public interface IO extends Runnable {}


    /**
     * Terminate the task abstract interface
     */
    public interface Break extends Runnable {}

}
