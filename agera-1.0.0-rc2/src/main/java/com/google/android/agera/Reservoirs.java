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

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Result.absentIfNull;

/**
 * Utility methods for creating {@link Reservoir} instances.
 *
 * <p>Any {@link Reservoir} created by this class has to be created from a {@link Looper} thread
 * or the method will throw an {@link IllegalStateException}.
 *
 * Reservoir 的工具类
 * 可以获取 Reservoir
 */
public final class Reservoirs {

    /**
     * Returns a {@link Reservoir} for the given value type.
     *
     * <p>The returned reservoir uses a standard unbounded FIFO queue as its backing storage. As a
     * result, all values are accepted and no duplication checks are used, and they are dequeued in
     * the same order.
     *
     * 构造一个 指定类型 T 的
     * 同步水库
     */
    @NonNull
    public static <T> Reservoir<T> reservoirOf(
        @SuppressWarnings("unused") @Nullable final Class<T> clazz) {
        return reservoir();
    }


    /**
     * Same as {@link #reservoirOf(Class)}. This variant is useful for when the value type is more
     * readily inferrable from the context, such as when used as a variable initializer or a return
     * value, so client code could simply write, for example,
     *
     * <pre>{@code Reservoir<String> stringReservoir = reservoir();}</pre>
     *
     * where this method is statically imported. This also helps in-line creation of a reservoir
     * whose
     * value type is generic, such as {@code List<String>}, so client code could write
     * {@code Reservoirs.<List<String>>reservoir()} instead of the less readable
     * {@code reservoirOf((Class<List<String>>) null)}.
     *
     * 实例化一个 队列为双端队列 的
     * 同步水库，没有数据
     */
    @NonNull
    public static <T> Reservoir<T> reservoir() {
        return reservoir(new ArrayDeque<T>());
    }


    /**
     * Returns a {@link Reservoir} that uses the given {@code queue} as the backing storage for
     * enqueuing and dequeuing values. It is up to the concrete {@link Queue#offer} implementation
     * of
     * the {@code queue} instance whether and how to accept each value to be enqueued.
     *
     * @param queue The backing storage of the reservoir. Any valid {@link Queue} implementation
     * can
     * be used, including non-FIFO queues such as {@link PriorityQueue}. Only these methods are
     * used: {@link Queue#offer} for attempting to enqueue a value, {@link Queue#poll} for
     * attempting to dequeue a value, and {@link Queue#isEmpty} for state check. All accesses are
     * synchronized on this {@code queue} instance; if the queue must also be accessed elsewhere,
     * those accesses must also be synchronized on this {@code queue} instance. Also note that
     * modifications to the queue outside the {@link Reservoir} interface will not update the
     * reservoir or its registered {@link Updatable}s.
     *
     * 根据一个 队列
     * 构造一个 同步水库
     */
    @NonNull
    public static <T> Reservoir<T> reservoir(@NonNull final Queue<T> queue) {
        return new SynchronizedReservoir<>(checkNotNull(queue));
    }


    /**
     * 同步水库
     *
     * @param <T> 水库数据类型
     */
    private static final class SynchronizedReservoir<T> extends BaseObservable
        implements Reservoir<T> {

        /**
         * 内置的一个队列
         * 用于存储 水库的 数据
         */
        @NonNull
        private final Queue<T> queue;


        private SynchronizedReservoir(@NonNull final Queue<T> queue) {
            this.queue = checkNotNull(queue);
        }


        /**
         * Reservoir 作为一个 Receiver 的特性
         * 可以接收 数据
         *
         * @param value 要接收的数据
         */
        @Override
        public void accept(@NonNull T value) {
            boolean shouldDispatchUpdate;
            // 锁住队列
            synchronized (queue) {
                // 队列内是否为没有数据
                boolean wasEmpty = queue.isEmpty();
                // 接收的数据 是否成功放入队列内
                boolean added = queue.offer(value);
                // 以上都符合的话，做一个标记
                shouldDispatchUpdate = wasEmpty && added;
            }
            /*
             * 如果 符合
             * 1. 队列内是否为没有数据
             * 2. 接收的数据 是否成功放入队列内
             * 通知观察者
             */
            if (shouldDispatchUpdate) {
                dispatchUpdate();
            }
        }


        /**
         * Reservoir 作为一个 Repository 的特性
         *
         * @return 存储的数据
         */
        @NonNull
        @Override
        public Result<T> get() {
            T nullableValue;
            boolean shouldDispatchUpdate;
            synchronized (queue) {
                nullableValue = queue.poll();
                // 队列是否还有数据
                shouldDispatchUpdate = !queue.isEmpty();
            }
            /*
             * 如果队列还有数据
             * 通知观察者
             */
            if (shouldDispatchUpdate) {
                dispatchUpdate();
            }
            /*
             * 判断 传入的结果 是否为 null
             * null 的话，返回 缺少参数的 错误 Result
             * 不为 null 的话，返回 成功 Result
             */
            return absentIfNull(nullableValue);
        }


        /**
         * 观察者被激活时
         * 队列内如果还有数据
         * 通知观察者
         */
        @Override
        protected void observableActivated() {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    return;
                }
            }
            dispatchUpdate();
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Reservoirs() {}
}
