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
 * Passes on updates from an {@link Updatable#update} call to any {@link Updatable} added using
 * {@link Observable#addUpdatable}. It should be possible to call {@link UpdateDispatcher#update}
 * from any thread.
 *
 * <p>This interface should typically not be implemented by client code; the standard
 * implementations obtainable from {@link Observables#updateDispatcher} help implement
 * {@link Observable}s adhering to the contract.
 *
 * Agera 中抽象出来的 观察者调度者 接口
 * 由于自身 继承了 被观察者（ Observable ）接口 和 观察者（ Updatable ）接口
 * 所以自身就能够拥有两者的功能
 * 使用场景有：
 * 一个 Activity 继承了 观察者（ Updatable ）接口
 * 一个 BroadcastReceiver 继承了 被观察者（ Observable ）接口
 * 这个 BroadcastReceiver 持有一个 UpdateDispatcher 的引用
 *
 * 这个 UpdateDispatcher 通过 观察者（ Updatable ）接口的功能 去添加 一个 观察者（ Updatable ） -> Activity
 *
 * 然后：
 * Activity 发送广播
 * BroadcastReceiver 的 onReceive(...) 接受到该广播
 * 调用 UpdateDispatcher 的通过自身的被观察者功能去 调用 已经添加的 观察者（ Updatable ）的 update
 *
 * 这个接口，一般不能不能由开发者去实现其功能，而是通过 Observables.updateDispatcher(...) 获取一个对象
 */
public interface UpdateDispatcher extends Observable, Updatable {}
