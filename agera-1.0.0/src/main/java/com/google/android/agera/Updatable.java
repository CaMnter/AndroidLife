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

/**
 * Called when when an event has occurred. Can be added to {@link Observable}s to be notified
 * of {@link Observable} events.
 *
 * Agera 中抽象出来的 观察者 接口
 * 只有一个 唯一 的方法 update()
 */
public interface Updatable {

    /**
     * Called when an event has occurred.
     *
     * 观察者的 更新方法
     * 事件发生时调用
     */
    void update();
}
