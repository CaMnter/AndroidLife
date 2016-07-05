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

import static com.google.android.agera.Preconditions.checkNotNull;

/**
 * Utility methods for obtaining {@link Supplier} instances.
 *
 * Supplier 的工具类
 * 可以获取 Supplier
 */
public final class Suppliers {

    /**
     * Returns a {@link Supplier} that always supplies the given {@code object} when its
     * {@link Supplier#get()} is called.
     *
     * 构造一个 StaticProducer 实例 作为 Supplier
     * StaticProducer 实现了 Supplier、Function、Merger
     */
    @NonNull
    public static <T> Supplier<T> staticSupplier(@NonNull final T object) {
        return new StaticProducer<>(object);
    }


    /**
     * Returns a {@link Supplier} that always supplies the value returned from the given
     * {@link Function} {@code function} when called with {@code from}.
     *
     * 构造一个 Function -> Supplier 转换器实例
     */
    @NonNull
    public static <T, F> Supplier<T> functionAsSupplier(
            @NonNull final Function<F, T> function, @NonNull final F from) {
        return new FunctionToSupplierConverter<>(function, from);
    }


    /**
     * FunctionToSupplierConverter 作为
     * Function -> Supplier 的 转换器
     * FunctionToSupplierConverter 实现了 Supplier 接口
     * 实现了 get() 方法，并且用 Function 的 apply(...) 方法，将原生类型转为目标类型数据，然后目标数据
     * 作为 Supplier 提供的数据
     *
     * @param <T> Function 原始类型
     * @param <F> Function 目标类型，也是 Supplier 目标类型
     */
    private static final class FunctionToSupplierConverter<T, F> implements Supplier<T> {
        // 转换方法
        @NonNull
        private final Function<F, T> function;
        // 原始数据
        @NonNull
        private final F from;


        private FunctionToSupplierConverter(@NonNull final Function<F, T> function,
                                            @NonNull final F from) {
            // Preconditions.checkNotNull(...) 方法检查数据
            this.function = checkNotNull(function);
            // Preconditions.checkNotNull(...) 方法检查数据
            this.from = checkNotNull(from);
        }


        /**
         * 利用 Function 的 apply(...) 方法，将原生类型转为目标类型数据，然后目标数据
         * 作为 Supplier 提供的数据
         *
         * @return
         */
        @NonNull
        @Override
        public T get() {
            return function.apply(from);
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Suppliers() {}
}
