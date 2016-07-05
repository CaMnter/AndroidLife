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
 * Takes two inputs and merges them into one output.
 *
 * Agera 中抽象出来的 合并 接口
 * 用于将 两个输入值，合成一个值
 * 可以是不同类型之间的转换: A+B=C
 * 也可以是 O+O=O
 * 默认三个类型为：Object, Object, Object
 */
public interface Merger<TFirst, TSecond, TTo> {

    /**
     * Computes the return value merged from the two given input values.
     * 合并方法
     */
    @NonNull TTo merge(@NonNull TFirst first, @NonNull TSecond second);
}
