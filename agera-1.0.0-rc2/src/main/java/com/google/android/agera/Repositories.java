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
import com.google.android.agera.RepositoryCompilerStates.REventSource;

import static com.google.android.agera.Observables.updateDispatcher;
import static com.google.android.agera.Preconditions.checkNotNull;

/**
 * Utility methods for obtaining {@link Repository} instances.
 *
 * <p>Any {@link Repository} created by this class have to be created from a {@link Looper} thread
 * or they will throw an {@link IllegalStateException}
 *
 * Repository 的工具类
 * 可以获取 Repository，也可开始编译 Repository
 */
public final class Repositories {

    /**
     * Returns a static {@link Repository} of the given {@code object}.
     *
     * 构造一个 仓库 Repository
     * 实质上用的是 SimpleRepository
     */
    @NonNull
    public static <T> Repository<T> repository(@NonNull final T object) {
        return new SimpleRepository<>(object);
    }


    /**
     * Starts the declaration of a compiled repository. See more at {@link
     * RepositoryCompilerStates}.
     *
     * 构造一个 REventSource 事件源状态
     * 并且 设置上了 初始值
     */
    @NonNull
    public static <T> REventSource<T, T> repositoryWithInitialValue(@NonNull final T initialValue) {
        return RepositoryCompiler.repositoryWithInitialValue(initialValue);
    }


    /**
     * Returns a {@link MutableRepository} with the given {@code object} as the initial data.
     *
     * 构造一个 可变仓库 MutableRepository
     * 实质上用的是 SimpleRepository
     */
    @NonNull
    public static <T> MutableRepository<T> mutableRepository(@NonNull final T object) {
        return new SimpleRepository<>(object);
    }


    /**
     * SimpleRepository 简单仓库
     * 实现了 MutableRepository 接口
     * 拥有 Repository 和 Receiver 的特性
     *
     * @param <T> 仓库的数据类型
     */
    private static final class SimpleRepository<T> implements MutableRepository<T> {
        /*
         * 持有一个 既是 观察者 又是 被观察者 的引用（ UpdateDispatcher ）
         */
        @NonNull
        private final UpdateDispatcher updateDispatcher;

        // 存放 仓库数据
        @NonNull
        private T reference;


        SimpleRepository(@NonNull final T reference) {
            // 创建一个 AsyncUpdateDispatcher
            this.updateDispatcher = updateDispatcher();
            // 检查 实例化 仓库的时候 的 数据 是否 为 null
            this.reference = checkNotNull(reference);
        }


        @NonNull
        @Override
        public synchronized T get() {
            // 返回 在 仓库中存储 的 数据
            return reference;
        }


        /**
         * Receiver 的特性 - 接收数据
         *
         * @param reference 接收的数据
         */
        @Override
        public void accept(@NonNull final T reference) {
            // 这里可能涉及到 多线程
            synchronized (this) {
                // 如果 接收的数据 与 原来存储的数据 相同，则返回
                if (reference.equals(this.reference)) {
                    // Keep the old reference to have a slight performance edge if GC is generational.
                    return;
                }
                // 替换数据
                this.reference = reference;
            }
            // 通知 观察者更新
            updateDispatcher.update();
        }


        /**
         * 为 UpdateDispatcher 添加 观察者
         *
         * @param updatable 观察者
         */
        @Override
        public void addUpdatable(@NonNull final Updatable updatable) {
            updateDispatcher.addUpdatable(updatable);
        }


        /**
         * 为 UpdateDispatcher 删除 观察者
         *
         * @param updatable 观察者
         */
        @Override
        public void removeUpdatable(@NonNull final Updatable updatable) {
            updateDispatcher.removeUpdatable(updatable);
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Repositories() {}
}
