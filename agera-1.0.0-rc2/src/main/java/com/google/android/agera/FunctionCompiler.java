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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.android.agera.Common.IDENTITY_FUNCTION;
import static com.google.android.agera.Common.TRUE_CONDICATE;
import static com.google.android.agera.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

/**
 * Function 编译器
 */
@SuppressWarnings("unchecked")
final class FunctionCompiler implements FunctionCompilerStates.FList, FunctionCompilerStates.FItem {

    // 方法集合
    @NonNull
    private final List<Function> functions;


    FunctionCompiler() {
        this.functions = new ArrayList<>();
    }


    /**
     * 添加方法 到 方法集合里
     * 只要不是 Common.IDENTITY_FUNCTION 的 通用的 Function
     */
    private void addFunction(@NonNull final Function function) {
        if (function != IDENTITY_FUNCTION) {
            functions.add(function);
        }
    }


    /**
     * 传入 thenApply 需要的 Function
     * 没有做特殊处理就 将 Function
     * 放入方法集合里
     *
     * @return 根据方法集合构造一个 ChainFunction 链式方法
     */
    @NonNull
    @Override
    public Function thenApply(@NonNull final Function function) {
        addFunction(function);
        return createFunction();
    }


    /**
     * 如果方法集合不存在方法，返回 Common.IDENTITY_FUNCTION 这个通过的 Function
     *
     * 存在方法的话，将 方法集合 -> 方法数组
     * 用于构造一个 ChainFunction 链式方法
     *
     * @return ChainFunction or IdentityFunction
     */
    @NonNull
    private Function createFunction() {
        if (functions.isEmpty()) {
            return IDENTITY_FUNCTION;
        }
        return new ChainFunction(functions.toArray(new Function[functions.size()]));
    }


    /**
     * 传入 unpack 需要的 Function
     * 没有做特殊处理就 将 Function
     * 放入方法集合里
     *
     * @return FunctionCompilerStates.FList 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FList unpack(@NonNull final Function function) {
        addFunction(function);
        return this;
    }


    /**
     * 传入 apply 需要的 Function
     * 没有做特殊处理就 将 Function
     * 放入方法集合里
     *
     * @return FunctionCompilerStates.FItem 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FItem apply(@NonNull final Function function) {
        addFunction(function);
        return this;
    }


    /**
     * 传入 morph 需要的 Function
     * 没有做特殊处理就 将 Function
     * 放入方法集合里
     *
     * @return FunctionCompilerStates.FList 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FList morph(@NonNull Function function) {
        addFunction(function);
        return this;
    }


    /**
     * 传入 filter 需要的 Predicate
     * 通过 Predicate 去 构造一个 FilterFunction 后
     * 并放入方法集合里
     *
     * @return FunctionCompilerStates.FList 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FList filter(@NonNull final Predicate filter) {
        if (filter != TRUE_CONDICATE) {
            addFunction(new FilterFunction(filter));
        }
        return this;
    }


    /**
     * 传入 limit 需要的 limit
     * 通过 limit 去 构造一个 LimitFunction 后
     * 并放入方法集合里
     *
     * @return FunctionCompilerStates.FList 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FList limit(final int limit) {
        addFunction(new LimitFunction(limit));
        return this;
    }


    /**
     * 传入 sort 需要的 comparator
     * 通过 comparator 去 构造一个 SortFunction 后
     * 并放入方法集合里
     *
     * @return FunctionCompilerStates.FList 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FList sort(@NonNull final Comparator comparator) {
        addFunction(new SortFunction(comparator));
        return this;
    }


    /**
     * 传入 map 需要的 Function
     * 通过 Function 去 构造一个 MapFunction 后
     * 并放入方法集合里
     *
     * @return FunctionCompilerStates.FList 状态
     */
    @NonNull
    @Override
    public FunctionCompilerStates.FList map(@NonNull final Function function) {
        if (function != IDENTITY_FUNCTION) {
            addFunction(new MapFunction(function));
        }
        return this;
    }


    /**
     * 传入 thenMap 需要的 Function
     * 并放入方法集合里
     *
     * @return 根据方法集合构造一个 ChainFunction 链式方法
     */
    @NonNull
    @Override
    public Function thenMap(@NonNull final Function function) {
        map(function);
        return createFunction();
    }


    /**
     * 传入 thenFilter 需要的 Function
     * 并放入方法集合里
     *
     * @return 根据方法集合构造一个 ChainFunction 链式方法
     */
    @NonNull
    @Override
    public Function thenFilter(@NonNull final Predicate filter) {
        filter(filter);
        return createFunction();
    }


    /**
     * 传入 thenLimit 需要的 Function
     * 并放入方法集合里
     *
     * @return 根据方法集合构造一个 ChainFunction 链式方法
     */
    @NonNull
    @Override
    public Function thenLimit(final int limit) {
        limit(limit);
        return createFunction();
    }


    /**
     * 传入 thenSort 需要的 Function
     * 并放入方法集合里
     *
     * @return 根据方法集合构造一个 ChainFunction 链式方法
     */
    @NonNull
    @Override
    public Function thenSort(@NonNull final Comparator comparator) {
        sort(comparator);
        return createFunction();
    }


    /**
     * 限制方法
     * 1. 如果 集合数据 长度不超过 限制值，直接返回
     * 2. 如果 集合数据 长度<=0，返回 Collections.emptyList
     * 3. 如果 集合数据 长度超过限制值，截取 0 - 限制值 之间的数据，放入一个新的集合里，返回
     *
     * @param <T> 集合数据类型
     */
    private static final class LimitFunction<T> implements Function<List<T>, List<T>> {
        // 限制值
        private final int limit;


        LimitFunction(final int limit) {
            this.limit = limit;
        }


        /**
         * 1. 如果 集合数据 长度不超过 限制值，直接返回
         * 2. 如果 集合数据 长度<=0，返回 Collections.emptyList
         * 3. 如果 集合数据 长度超过限制值，截取 0 - 限制值 之间的数据，放入一个新的集合里，返回
         *
         * @param input 原始集合
         * @return 限制后的 集合数据
         */
        @NonNull
        @Override
        public List<T> apply(@NonNull final List<T> input) {
            if (input.size() < limit) {
                return input;
            }
            if (limit <= 0) {
                return emptyList();
            }
            return new ArrayList<>(input.subList(0, limit));
        }
    }


    /**
     * 集合转型方法
     * 创建长度和 原始集合 一样的 目标集合，然后通过 转型方法 将 原始集合 中的数据逐个转型后
     * 添加到 目标集合 中
     *
     * @param <F> 原始集合类型
     * @param <T> 目标集合类型
     */
    private static final class MapFunction<F, T> implements Function<List<F>, List<T>> {
        // 转型方法
        @NonNull
        private final Function<F, T> function;


        MapFunction(@NonNull final Function<F, T> function) {
            this.function = checkNotNull(function);
        }


        /**
         * 创建长度和 原始集合 一样的 目标集合，然后通过 转型方法 将 原始集合 中的数据逐个转型后
         * 添加到 目标集合 中
         *
         * @param input 原始集合数据
         * @return 目标集合数据
         */
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public List<T> apply(@NonNull final List<F> input) {
            if (input.isEmpty()) {
                return emptyList();
            }
            final List<T> result = new ArrayList(input.size());
            for (final F item : input) {
                result.add(function.apply(item));
            }
            return result;
        }
    }


    /**
     * 链式方法
     *
     * 链式方法 的 apply:
     * 初始值 input
     * 然后经过 Function[] 内， 所有 Function 的 apply 的转型操作，得到的最终结果是 链式方法的结果
     */
    private static final class ChainFunction implements Function {
        // 方法数组
        @NonNull
        private final Function[] functions;


        ChainFunction(@NonNull final Function[] functions) {
            this.functions = checkNotNull(functions);
        }


        /**
         * 然后经过 Function[] 内， 所有 Function 的 apply 的转型操作，得到的最终结果是 链式方法的结果
         *
         * @param input 初始值
         * @return 链式方法的结果值
         */
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public Object apply(@NonNull final Object input) {
            Object item = input;
            for (final Function function : functions) {
                item = function.apply(item);
            }
            return item;
        }
    }


    /**
     * 过滤方法
     * 利用 Predicate 断定数据是否要过滤
     */
    private static final class FilterFunction<T> implements Function<List<T>, List<T>> {
        // 断定接口
        @NonNull
        private final Predicate filter;


        FilterFunction(@NonNull final Predicate filter) {
            this.filter = checkNotNull(filter);
        }


        /**
         * 如果集合数据内，没有数据，返回 Collections.emptyList
         * 有数据的话，先创建一个一样大的集合，然后根据 断定接口 断定后，再逐个添加到新的集合内，最后返回
         *
         * @param input 要过滤的 集合数据
         * @return 过滤好的 集合数据
         */
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public List<T> apply(@NonNull final List<T> input) {
            if (input.isEmpty()) {
                return emptyList();
            }
            final List<T> result = new ArrayList(input.size());
            for (final T item : input) {
                if (filter.apply(item)) {
                    result.add(item);
                }
            }
            return result;
        }
    }


    /**
     * 排序方法
     * 利用传入进来的 Comparator 进行排序
     *
     * @param <T> 集合数据类型
     */
    private static final class SortFunction<T> implements Function<List<T>, List<T>> {
        @NonNull
        private final Comparator comparator;


        SortFunction(@NonNull final Comparator comparator) {
            this.comparator = checkNotNull(comparator);
        }


        /**
         * 利用传入进来的 Comparator 进行排序
         *
         * @param input 要排序的 集合数据
         * @return 排好序的 集合数据
         */
        @NonNull
        @Override
        public List<T> apply(@NonNull final List<T> input) {
            final List<T> output = new ArrayList<>(input);
            Collections.sort(output, comparator);
            return output;
        }
    }
}
