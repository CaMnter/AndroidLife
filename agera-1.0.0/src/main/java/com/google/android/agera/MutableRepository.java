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
 * A {@link Repository} that can receive new data through {@link Receiver#accept(Object)}.
 *
 * <p>If the new data does not {@linkplain Object#equals equal} to the old data, the added
 * {@link Updatable}s will be notified. {@link MutableRepository#accept(Object)} can be called on
 * any thread.
 *
 * Agera 中抽象出来的 可变仓库 接口
 * 继承了 数据接受者（ Receiver ）和 仓库（ Repository ）
 * 然而，仓库（ Repository ）接口 又继承了 被观察者（ Observable ）和 数据供应者（ Supplier ）接口
 * 就同时具备了它们（ Receiver, Observable, Supplier  ）的基本功能：
 * 1.作为被观察者，去通知观察者（ Updatable ）去更新数据
 * 2.作为数据供应者，去提供数据
 * 3.作为数据接受者，去接受外部数据
 *
 * 与 Reservoir 几乎一模一样。但是，Reservoir 中的 Repository 指定了 类型 Result<T>
 */
public interface MutableRepository<T> extends Repository<T>, Receiver<T> {}
