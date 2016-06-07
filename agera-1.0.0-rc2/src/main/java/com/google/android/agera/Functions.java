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
import android.support.annotation.Nullable;
import com.google.android.agera.Common.StaticProducer;
import com.google.android.agera.FunctionCompilerStates.FItem;
import com.google.android.agera.FunctionCompilerStates.FList;
import java.util.List;

import static com.google.android.agera.Common.FAILED_RESULT;
import static com.google.android.agera.Common.IDENTITY_FUNCTION;
import static com.google.android.agera.Preconditions.checkNotNull;

/**
 * Utility methods for obtaining {@link Function} instances.
 *
 * Function 的工具类
 * 可以获取 Function
 */
public final class Functions {

    /**
     * Returns a {@link Function} that returns {@code object} as the result of each
     * {@link Function#apply} function call.
     *
     * 构造一个 StaticProducer 实例 作为 Function
     * StaticProducer 实现了 Supplier、Function、Merger
     */
    @NonNull
    public static <F, T> Function<F, T> staticFunction(@NonNull final T object) {
        return new StaticProducer<>(object);
    }


    /**
     * Returns a {@link Function} that returns the result of {@code supplier} as the result of each
     * {@link Function#apply} function call.
     *
     * 构造一个 Supplier -> Function 转换器实例
     */
    @NonNull
    public static <F, T> Function<F, T> supplierAsFunction(
            @NonNull final Supplier<? extends T> supplier) {
        return new SupplierAsFunction<>(supplier);
    }


    /**
     * Returns a {@link Function} that passes on the {@link Function} input as output.
     *
     * 获取一个
     * 原始类型 = 目标类型
     * 的 Function 对象
     * 这里，使用了 一个通用的 Function 进行转换，是 Common.IDENTITY_FUNCTION
     */
    @NonNull
    public static <T> Function<T, T> identityFunction() {
        @SuppressWarnings("unchecked")
        final Function<T, T> identityFunction = (Function<T, T>) IDENTITY_FUNCTION;
        return identityFunction;
    }


    /**
     * Starts describing {@link Function} that starts with a single item.
     *
     * @return the next {@link FunctionCompilerStates} state
     *
     * 获取一个
     * 初始类型 = 结束类型
     * 的 FItem 对象
     * 这里，返回了 FunctionCompiler
     * 因为 FunctionCompiler 实现了 FList 和 FItem 接口
     */
    @NonNull
    @SuppressWarnings({ "unchecked", "UnusedParameters" })
    public static <F> FItem<F, F> functionFrom(@Nullable Class<F> from) {
        return new FunctionCompiler();
    }


    /**
     * Starts describing a {@link Function} that starts with a {@link List} of items.
     *
     * @return the next {@link FunctionCompilerStates} state
     *
     * 获取一个
     * 初始类型 = 初始集合类型 =结束类型
     * 的 FList 对象
     * 这里，返回了 FunctionCompiler
     * 因为 FunctionCompiler 实现了 FList 和 FItem 接口
     */
    @NonNull
    @SuppressWarnings({ "unchecked", "UnusedParameters" })
    public static <F> FList<F, List<F>, List<F>> functionFromListOf(
            @Nullable final Class<F> from) {
        return new FunctionCompiler();
    }


    /**
     * Returns a {@link Function} that wraps a {@link Throwable} in a
     * {@link Result#failure(Throwable)}).
     *
     * 获取一个 Throwable -> Result
     * 的 Function 对象
     * 这里，使用了 一个通用的 Function 进行转换，是 Common.FAILED_RESULT
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Function<Throwable, Result<T>> failedResult() {
        return (Function<Throwable, Result<T>>) FAILED_RESULT;
    }


    /**
     * SupplierAsFunction 作为
     * Supplier -> Function 的转换器
     * SupplierAsFunction 实现了 Function 接口
     * 实现了 apply(...) 方法，并且用 Supplier 的 get(...) 方法，将 Supplier 提供的数据作为
     * Function 的 目标数据
     *
     * @param <F> Function 原生类型
     * @param <T> Function 目标类型，也是 Supplier 目标类型
     */
    private static final class SupplierAsFunction<F, T> implements Function<F, T> {
        @NonNull
        private final Supplier<? extends T> supplier;


        SupplierAsFunction(@NonNull final Supplier<? extends T> supplier) {
            // Preconditions.checkNotNull(...) 方法检查数据
            this.supplier = checkNotNull(supplier);
        }


        /**
         * 用 Supplier 的 get(...) 方法，将 Supplier 提供的数据作为
         * Function 的 目标数据
         *
         * @param from Function 原始数据
         * @return Supplier 提供的数据
         */
        @NonNull
        @Override
        public T apply(@NonNull F from) {
            return supplier.get();
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Functions() {}
}
