/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.agera;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.util.Arrays;

import static android.os.SystemClock.elapsedRealtime;
import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Preconditions.checkState;
import static com.google.android.agera.WorkerHandler.MSG_LAST_REMOVED;
import static com.google.android.agera.WorkerHandler.MSG_UPDATE;
import static com.google.android.agera.WorkerHandler.workerHandler;

/**
 * A partial implementation of {@link Observable} that adheres to the threading contract between
 * {@link Observable}s and {@link Updatable}s. Subclasses can use {@link #observableActivated()}
 * and
 * {@link #observableDeactivated()} to control the activation and deactivation of this observable,
 * and to send out notifications to client updatables with {@link #dispatchUpdate()}.
 *
 * <p>For cases where subclassing {@link BaseObservable} is impossible, for example when the
 * potential class already has a base class, consider using {@link Observables#updateDispatcher()}
 * to help implement the {@link Observable} interface.
 */
public abstract class BaseObservable implements Observable {

    /*
     * 申请好一个 静态 长度为0 的对象数组
     */
    @NonNull
    private static final Object[] NO_UPDATABLES_OR_HANDLERS = new Object[0];

    /*
     * 保存一个 WorkerHandler
     */
    @NonNull
    private final WorkerHandler handler;

    /*
     * 创建一个 token 来标识 自身
     */
    @NonNull
    private final Object token = new Object();
    /*
     * 最小的更新时间间隔
     */
    final int shortestUpdateWindowMillis;
    /*
     * 用于存放 观察者 和 观察者对应的 Handler
     */
    @NonNull
    private Object[] updatablesAndHandlers;
    /*
     * 用于记录 行为
     * size++ 的话，是执行过了 add(...)，并且 在 updatablesAndHandlers 中， 存放了 观察者 和 观察者对应的 Handler
     * size-- 的话，是执行过了 remove(...)，并且 在 updatablesAndHandlers 中， 删除了 观察者 和 观察者对应的 Handler
     */
    private int size;
    /*
     * 记录 上次 更新的时候
     */
    private long lastUpdateTimestamp;
    /*
     * 相当于一个 过程锁
     * 在发送 通过 给 观察者时 会锁上（ dispatchUpdate() ）
     * 处理后，会打开
     */
    private boolean pendingUpdate = false;


    /**
     * 默认的
     * 最少的更新时间间隔 = 0
     */
    protected BaseObservable() {
        this(0);
    }


    /**
     * 构造方法
     * 需要 最少的更新时间间隔
     *
     * @param shortestUpdateWindowMillis 最少的更新时间间隔
     */
    BaseObservable(final int shortestUpdateWindowMillis) {
        checkState(Looper.myLooper() != null, "Can only be created on a Looper thread");
        this.shortestUpdateWindowMillis = shortestUpdateWindowMillis;
        this.handler = workerHandler();
        this.updatablesAndHandlers = NO_UPDATABLES_OR_HANDLERS;
        this.size = 0;
    }


    /**
     * 添加一个 观察者
     *
     * @param updatable 观察者
     */
    @Override
    public final void addUpdatable(@NonNull final Updatable updatable) {
        checkState(Looper.myLooper() != null, "Can only be added on a Looper thread");
        checkNotNull(updatable);
        boolean activateNow = false;
        synchronized (token) {
            // 观察者 和 其对应的 WorkerHandler
            add(updatable, workerHandler());
            // 如果 执行 add(...) 成功了
            if (size == 1) {
                /*
                 * 1.如果 执行 BaseObservable.observableDeactivated() 的消息
                 * 还在队列中，则删除
                 *
                 * 2. 判断是否在 主线程
                 * 然后  activateNow = true
                 *
                 * 3. 如果 1. 2. 都不是
                 * 发送 执行 BaseObservable.observableActivated() 的消息
                 */
                if (handler.hasMessages(MSG_LAST_REMOVED, this)) {
                    handler.removeMessages(MSG_LAST_REMOVED, this);
                } else if (Looper.myLooper() == handler.getLooper()) {
                    activateNow = true;
                } else {
                    handler.obtainMessage(WorkerHandler.MSG_FIRST_ADDED, this).sendToTarget();
                }
            }
        }
        /*
         * 如果在主线程
         * 回调 observableActivated()
         */
        if (activateNow) {
            observableActivated();
        }
    }


    /**
     * 删除一个 观察者
     *
     * @param updatable 观察者
     */
    @Override
    public final void removeUpdatable(@NonNull final Updatable updatable) {
        checkState(Looper.myLooper() != null, "Can only be removed on a Looper thread");
        checkNotNull(updatable);
        synchronized (token) {
            // 删除 观察者 和 其对应的 Handler
            remove(updatable);
            // 如果 执行 remove(...) 成功了
            if (size == 0) {
                // 发送 执行  BaseObservable.observableDeactivated() 的 消息
                handler.obtainMessage(MSG_LAST_REMOVED, this).sendToTarget();
                // 删除 执行 BaseObservable.sendUpdate() 的 消息
                handler.removeMessages(MSG_UPDATE, this);
                // 解 dispatchUpdate() 上的 锁
                pendingUpdate = false;
            }
        }
    }


    /**
     * 通知所有观察者
     * Notifies all registered {@link Updatable}s.
     */
    protected final void dispatchUpdate() {
        synchronized (token) {
            // 检查 过程锁
            if (!pendingUpdate) {
                // 锁上
                pendingUpdate = true;
                // 发送 执行 BaseObservable.sendUpdate() 的消息
                handler.obtainMessage(MSG_UPDATE, this).sendToTarget();
            }
        }
    }


    /**
     * 添加 观察者 和 其对应的 WorkerHandler
     *
     * @param updatable 观察者
     * @param handler 观察者 的 WorkerHandler
     */
    private void add(@NonNull final Updatable updatable, @NonNull final Handler handler) {
        int indexToAdd = -1;
        for (int index = 0; index < updatablesAndHandlers.length; index += 2) {
            // 判断是否 添加过 这个 观察者
            if (updatablesAndHandlers[index] == updatable) {
                throw new IllegalStateException("Updatable already added, cannot add.");
            }
            // 存在空位，记录下来，用于添加
            if (updatablesAndHandlers[index] == null) {
                indexToAdd = index;
            }
        }

        // 如果数组满了，就扩容
        if (indexToAdd == -1) {
            indexToAdd = updatablesAndHandlers.length;
            updatablesAndHandlers = Arrays.copyOf(updatablesAndHandlers,
                indexToAdd < 2 ? 2 : indexToAdd * 2);
        }
        // 然后添加 观察者 和 其对应的 Handler
        updatablesAndHandlers[indexToAdd] = updatable;
        updatablesAndHandlers[indexToAdd + 1] = handler;
        // 记录为 添加成功
        size++;
    }


    /**
     * 删除 观察者 和 其对应的 Handler
     *
     * @param updatable 观察者
     */
    private void remove(@NonNull final Updatable updatable) {
        for (int index = 0; index < updatablesAndHandlers.length; index += 2) {
            // 找到 该 观察者
            if (updatablesAndHandlers[index] == updatable) {
                // 找到 该 观察者 对应的 Handler
                WorkerHandler handler = (WorkerHandler) updatablesAndHandlers[index + 1];
                // 在 该 Handler 情况 观察者的数据 和 token 信息
                handler.removeUpdatable(updatable, token);
                updatablesAndHandlers[index] = null;
                updatablesAndHandlers[index + 1] = null;
                // 记录为 删除成功
                size--;
                return;
            }
        }
        throw new IllegalStateException("Updatable not added, cannot remove.");
    }


    /**
     * 通知 该 被观察者上
     * 所有的 观察者
     */
    void sendUpdate() {
        synchronized (token) {
            // 只有 锁上（ true ）才能通知
            if (!pendingUpdate) {
                return;
            }
            /*
             * 如果有 最小的更新时间间隔
             */
            if (shortestUpdateWindowMillis > 0) {
                // 记录 此时
                final long elapsedRealtimeMillis = elapsedRealtime();
                // 记录 此时-上次更新时间 之间的间隔
                final long timeFromLastUpdate = elapsedRealtimeMillis - lastUpdateTimestamp;
                // 如果 时间间隔 小于 最小的更新时间间隔
                if (timeFromLastUpdate < shortestUpdateWindowMillis) {
                    /*
                     * 延迟发送，延迟时间 = 最小的更新时间间隔-时间间隔 的时间
                     * 重新 执行 BaseObservable.sendUpdate() 的消息
                     */
                    handler.sendMessageDelayed(
                        handler.obtainMessage(WorkerHandler.MSG_UPDATE, this),
                        shortestUpdateWindowMillis - timeFromLastUpdate);
                    return;
                }
                lastUpdateTimestamp = elapsedRealtimeMillis;
            }
            // 打开 锁
            pendingUpdate = false;
            /*
             * 逐个 找到 每个 观察者 后
             * 再找到 对应的 Handler
             * 用 Handler 去发送 执行 Updatable.update() 的消息
             */
            for (int index = 0; index < updatablesAndHandlers.length; index = index + 2) {
                final Updatable updatable = (Updatable) updatablesAndHandlers[index];
                final WorkerHandler handler =
                    (WorkerHandler) updatablesAndHandlers[index + 1];
                if (updatable != null) {
                    handler.update(updatable, token);
                }
            }
        }
    }


    /**
     * Called from the worker looper thread when this {@link Observable} is activated by
     * transitioning
     * from having no client {@link Updatable}s to having at least one client {@link Updatable}.
     *
     * 被观察者 被激活时（ 成功添加一个观察者后 ）
     */
    protected void observableActivated() {}


    /**
     * Called from the worker looper thread when this {@link Observable} is deactivated by
     * transitioning from having at least one client {@link Updatable} to having no client
     * {@link Updatable}s.
     *
     * 被观察者 被失效时（ 成功删除一个观察者后 ）
     */
    protected void observableDeactivated() {}
}
