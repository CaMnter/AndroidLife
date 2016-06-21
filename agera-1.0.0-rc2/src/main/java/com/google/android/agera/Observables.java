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
import java.util.ArrayList;
import java.util.List;

import static com.google.android.agera.Preconditions.checkNotNull;

/**
 * Utility methods for obtaining {@link Observable} instances.
 *
 * <p>Any {@link Observable} created by this class have to be created from a {@link Looper} thread
 * or they will throw an {@link IllegalStateException}
 *
 * <p>{@link UpdateDispatcher}s created by this class will for any injected
 * {@link ActivationHandler} call {@link ActivationHandler#observableActivated(UpdateDispatcher)}
 * and {@link ActivationHandler#observableDeactivated(UpdateDispatcher)} on the thread the
 * {@link UpdateDispatcher} was created on.
 *
 * Observable 的工具类
 * 可以获取 Observable
 */
public final class Observables {

    /**
     * Returns an {@link Observable} that notifies added {@link Updatable}s that any of the
     * {@code observables} have changed.
     *
     * 默认 最短的更新时间 = 0
     */
    @NonNull
    public static Observable compositeObservable(@NonNull final Observable... observables) {
        return compositeObservable(0, observables);
    }


    /**
     * 通过 一组 Observable
     *
     * 找到 这组 内 所有的 Observable
     * 因为这个组 内的 Observable 可能是 复合被观察者 CompositeObservable，所以又存在一个组的 Observable
     * 由于，要找到所有的 Observable，所以会 判断 是否 是 CompositeObservable，然后抽出 那个组的 Observable
     *
     * @param shortestUpdateWindowMillis 最短的更新时间
     * @param observables 被观察者组
     * @return 一个封装了 所有 被观察者的 复合被观察者 CompositeObservable
     */
    @NonNull
    static Observable compositeObservable(final int shortestUpdateWindowMillis,
                                          @NonNull final Observable... observables) {

        /*
         * 没有 被观察者数据
         * 实例化一个 没有被观察者数据 的 CompositeObservable
         */
        if (observables.length == 0) {
            return new CompositeObservable(0);
        }

        /*
         * 如果只有 一个被观察者
         */
        if (observables.length == 1) {
            final Observable singleObservable = observables[0];
            /*
             * 如果 是 复合被观察者 类型
             * 抽取去 内部的被观察者组
             * 然后用于实例化 一个 CompositeObservable 后返回
             */
            if (singleObservable instanceof CompositeObservable
                && ((CompositeObservable) singleObservable).shortestUpdateWindowMillis == 0) {
                return new CompositeObservable(shortestUpdateWindowMillis,
                    ((CompositeObservable) singleObservable).observables);
            } else {
                // 直接实例化 CompositeObservable
                return new CompositeObservable(shortestUpdateWindowMillis, singleObservable);
            }
        }

        final List<Observable> flattenedDedupedObservables = new ArrayList<>();
        /*
         * 找出所有的 Observable
         * 如果是 Observable 类型，直接判断是否存在后，进行添加
         * 如果是 CompositeObservable 类型，抽出其内 Observable 组。然后逐个判断是否存在后，进行添加
         */
        for (final Observable observable : observables) {
            if (observable instanceof CompositeObservable
                && ((CompositeObservable) observable).shortestUpdateWindowMillis == 0) {
                for (Observable subObservable : ((CompositeObservable) observable).observables) {
                    if (!flattenedDedupedObservables.contains(subObservable)) {
                        flattenedDedupedObservables.add(subObservable);
                    }
                }
            } else {
                if (!flattenedDedupedObservables.contains(observable)) {
                    flattenedDedupedObservables.add(observable);
                }
            }
        }
        /*
         * 拿到 找到后的 所有 Observable
         * 用于实例化一个 CompositeObservable
         */
        return new CompositeObservable(shortestUpdateWindowMillis,
            flattenedDedupedObservables.toArray(
                new Observable[flattenedDedupedObservables.size()]));
    }


    /**
     * Returns an {@link Observable} that notifies added {@link Updatable}s that any of the
     * {@code observables} have changed only if the {@code condition} applies.
     *
     * 构造一个 ConditionalObservable 实例
     */
    @NonNull
    public static Observable conditionalObservable(
        @NonNull final Condition condition, @NonNull final Observable... observables) {
        return new ConditionalObservable(compositeObservable(observables), condition);
    }


    /**
     * Returns an {@link Observable} that notifies added {@link Updatable}s that the
     * {@code observables} has changed, but never more often than every
     * {@code shortestUpdateWindowMillis}.
     *
     * 直接调用 compositeObservable(...)
     */
    @NonNull
    public static Observable perMillisecondObservable(
        final int shortestUpdateWindowMillis, @NonNull final Observable... observables) {
        return compositeObservable(shortestUpdateWindowMillis, observables);
    }


    /**
     * Returns an {@link Observable} that notifies added {@link Updatable}s that the
     * {@code observable} has changed, but never more often than once per {@link Looper} cycle.
     *
     * 最短的更新时间 = 0
     * 调用 compositeObservable(...)
     */
    @NonNull
    public static Observable perLoopObservable(@NonNull final Observable... observables) {
        return compositeObservable(observables);
    }


    /**
     * Returns an asynchronous {@link UpdateDispatcher}.
     *
     * <p>{@link UpdateDispatcher#update()} can be called from any thread
     * {@link UpdateDispatcher#addUpdatable(Updatable)} and
     * {@link UpdateDispatcher#removeUpdatable(Updatable)} can only be called from {@link Looper}
     * threads. Any added {@link Updatable} will be called on the thread they were added from.
     *
     * 构造一个 AsyncUpdateDispatcher 实例
     */
    @NonNull
    public static UpdateDispatcher updateDispatcher() {
        return new AsyncUpdateDispatcher(null);
    }


    /**
     * Returns an asynchronous {@link UpdateDispatcher}.
     *
     * <p>See {@link #updateDispatcher()}
     *
     * <p>{@code updatablesChanged} will be called on the same thread as the {@link
     * UpdateDispatcher}
     * was created from when the first {@link Updatable} was added / last {@link Updatable} was
     * removed.
     *
     * <p>This {@link UpdateDispatcher} is useful when implementing {@link Observable} services
     * with
     * an <i>active</i>/<i>inactive</i> lifecycle.
     *
     * 通过 UpdateDispatcher
     * 构造一个 AsyncUpdateDispatcher 实例
     */
    @NonNull
    public static UpdateDispatcher updateDispatcher(
        @NonNull final ActivationHandler activationHandler) {
        return new AsyncUpdateDispatcher(activationHandler);
    }


    /**
     * 复合被观察者
     *
     * 主要工作：
     * 1. 用于将 BaseObservable 中 回调回来的
     * observableActivated() 和 observableDeactivated() 逻辑
     * 转接到 每个 Observable 上
     *
     * 2. 用于将 UpdateDispatcher 中回调回来的
     * update()
     * 转接到 BaseObservable 上的 dispatchUpdate()
     */
    private static final class CompositeObservable extends BaseObservable implements Updatable {
        // 被观察者们
        @NonNull
        private final Observable[] observables;


        CompositeObservable(final int shortestUpdateWindowMillis,
                            @NonNull final Observable... observables) {
            super(shortestUpdateWindowMillis);
            this.observables = observables;
        }


        /**
         * BaseObservable.observableActivated() ->
         * N 次 Observable.addUpdatable(this)
         */
        @Override
        protected void observableActivated() {
            for (final Observable observable : observables) {
                observable.addUpdatable(this);
            }
        }


        /**
         * BaseObservable.observableDeactivated() ->
         * N 次 Observable.removeUpdatable(this)
         */
        @Override
        protected void observableDeactivated() {
            for (final Observable observable : observables) {
                observable.removeUpdatable(this);
            }
        }


        /**
         * UpdateDispatcher.update() -> BaseObservable.dispatchUpdate()
         */
        @Override
        public void update() {
            dispatchUpdate();
        }
    }


    /**
     * 有条件的被观察者
     *
     * 主要工作：
     * 1. 用于将 BaseObservable 中 回调回来的
     * observableActivated() 和 observableDeactivated() 逻辑
     * 转接到 Observable 上
     *
     * 2. 用于将 UpdateDispatcher 中回调回来的
     * update()，通过条件判断，决定是否
     * 转接到 BaseObservable 上的 dispatchUpdate()
     */
    private static final class ConditionalObservable extends BaseObservable implements Updatable {
        @NonNull
        private final Observable observable;
        @NonNull
        private final Condition condition;


        ConditionalObservable(@NonNull final Observable observable,
                              @NonNull final Condition condition) {
            this.observable = checkNotNull(observable);
            this.condition = checkNotNull(condition);
        }


        /**
         * BaseObservable.observableActivated() -> Observable.addUpdatable(this)
         */
        @Override
        protected void observableActivated() {
            observable.addUpdatable(this);
        }


        /**
         * BaseObservable.observableDeactivated() -> Observable.removeUpdatable(this)
         */
        @Override
        protected void observableDeactivated() {
            observable.removeUpdatable(this);
        }


        /**
         * UpdateDispatcher.update() -> BaseObservable.dispatchUpdate()
         */
        @Override
        public void update() {
            if (condition.applies()) {
                dispatchUpdate();
            }
        }
    }


    /**
     * 异步观察调度者
     *
     * 主要工作：
     * 1. 用于将 BaseObservable 中 回调回来的
     * observableActivated() 和 observableDeactivated() 逻辑
     * 转接到 ActivationHandler 上
     *
     * 2. 用于将 UpdateDispatcher 中回调回来的
     * update()
     * 转接到 BaseObservable 上的 dispatchUpdate()
     */
    private static final class AsyncUpdateDispatcher extends BaseObservable
        implements UpdateDispatcher {

        // 内置一个 激活处理 接口
        @Nullable
        private final ActivationHandler activationHandler;


        private AsyncUpdateDispatcher(@Nullable ActivationHandler activationHandler) {
            this.activationHandler = activationHandler;
        }


        /**
         * BaseObservable.observableActivated() -> ActivationHandler.observableActivated(this)
         */
        @Override
        protected void observableActivated() {
            if (activationHandler != null) {
                activationHandler.observableActivated(this);
            }
        }


        /**
         * BaseObservable.observableDeactivated() -> ActivationHandler.observableDeactivated(this)
         */
        @Override
        protected void observableDeactivated() {
            if (activationHandler != null) {
                activationHandler.observableDeactivated(this);
            }
        }


        /**
         * UpdateDispatcher.update() -> BaseObservable.dispatchUpdate()
         */
        @Override
        public void update() {
            dispatchUpdate();
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Observables() {}
}
