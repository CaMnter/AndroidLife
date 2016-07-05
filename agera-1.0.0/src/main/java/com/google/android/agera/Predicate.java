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
 * Determines a true or false value for a given input.
 *
 * <p>The {@link Predicates} class provides common predicates and related utilities.
 *
 * Agera 中抽象出来的 断定 接口
 * 用于 断定 给定的数据 是 正确的 还是 错误的
 * Predicates 中 提供常见的 断定实现类 和 相关工具
 */
public interface Predicate<T> {

    /**
     * Returns whether the predicate applies to the input {@code value}.
     * 通过给定值，根据相应的 断定逻辑 去断定这个值是 正确的 还是 错误的
     * true = 正确的
     * false = 错误的
     */
    boolean apply(@NonNull T value);
}
