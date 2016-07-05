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

import android.support.annotation.NonNull;

/**
 * Receives events of the {@link UpdateDispatcher} created with
 * {@link Observables#updateDispatcher(ActivationHandler)} when the first {@link Updatable} is
 * added
 * and the last {@link Updatable} is removed.
 *
 * <p>Typically an {@link Observable} service implemented using a {@link UpdateDispatcher} only
 * needs to be updated if it has clients of its own. By starting to listen to updates from its
 * clients on {@link #observableActivated} and stopping on {@link #observableDeactivated}, the
 * service of the service can implement an <i>active</i>/<i>inactive</i> lifecycle,
 * saving memory and execution time when not needed.
 *
 * Agera 中抽象出来的 激活处理 接口
 * 用于 执行 被观察者 的 激活 和 释放 功能
 * 这里的 被观察者 是 UpdateDispatcher
 * 因为 UpdateDispatcher 继承了 被观察者（ Observable ）接口 和 观察者（ Updatable ）接口
 */
public interface ActivationHandler {

    /**
     * Called when the the {@code caller} changes state from having no {@link Updatable}s to
     * having at least one {@link Updatable}.
     *
     * 被观察者 被 激活时
     */
    void observableActivated(@NonNull UpdateDispatcher caller);

    /**
     * Called when the the {@code caller} changes state from having {@link Updatable}s to
     * no longer having {@link Updatable}s.
     *
     * 被观察者 被 释放时
     */
    void observableDeactivated(@NonNull UpdateDispatcher caller);
}
