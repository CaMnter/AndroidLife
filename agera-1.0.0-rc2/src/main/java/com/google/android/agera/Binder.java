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
 * A receiver of two input objects.
 *
 * Agera 中抽象出来的 绑定 接口
 * 可用于执行两个 数据流的合并
 * 可以指定两个数据流的类型，默认都是 Object 类型
 */
public interface Binder<TFirst, TSecond> {

    /**
     * Accepts the given values {@code first} and {@code second}.
     * 绑定、合并数据
     */
    void bind(@NonNull TFirst first, @NonNull TSecond second);
}
