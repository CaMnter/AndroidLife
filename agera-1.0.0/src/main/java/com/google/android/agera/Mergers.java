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
import com.google.android.agera.Common.StaticProducer;

/**
 * Utility methods for obtaining {@link Merger} instances.
 *
 * Merger 的工具类
 * 可以获取 Merger
 */
public final class Mergers {

    /**
     * 实例化一个 对象不相等 合并者
     */
    private static final ObjectsUnequalMerger OBJECTS_UNEQUAL_MERGER = new ObjectsUnequalMerger();


    /**
     * Returns a {@link Merger} that outputs the given {@code value} regardless of the input
     * values.
     *
     * 构造一个 StaticProducer 实例 作为 Merger
     * StaticProducer 实现了 Supplier、Function、Merger
     */
    @NonNull
    public static <TFirst, TSecond, TTo> Merger<TFirst, TSecond, TTo> staticMerger(
            @NonNull final TTo value) {
        return new StaticProducer<>(value);
    }


    /**
     * Returns a {@link Merger} that outputs the <i>negated</i> result of {@link Object#equals}
     * called
     * on the first input value, using the second input value as the argument of that call.
     *
     * 获取一个 对象不相等 合并者，用于比较
     */
    @NonNull
    public static Merger<Object, Object, Boolean> objectsUnequal() {
        return OBJECTS_UNEQUAL_MERGER;
    }


    /**
     * ObjectsUnequalMerger - 对象不相等 合并者
     * 一个很普通的 Merger 实现类，目标类型是 Boolean
     * 用的是 !Object.equals(...)  判断 TFirst 和 TSecond 是否 "不是一个对象"
     *
     * 这个 Merger 的作用是用于比较
     */
    private static final class ObjectsUnequalMerger implements Merger<Object, Object, Boolean> {
        @NonNull
        @Override
        public Boolean merge(@NonNull final Object oldValue, @NonNull final Object newValue) {
            return !oldValue.equals(newValue);
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Mergers() {}
}
