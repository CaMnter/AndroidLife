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
import java.util.Comparator;
import java.util.List;

/**
 * Container of the compiler state interfaces supporting the declaration of {@link Function}s
 * using the type-safe declarative language.
 *
 * Agera 中抽象出来的 方法编译状态 接口
 * 由 FunctionCompilerStates 编译（ create ）出 一个 Function
 * 并且规定了方法状态的执行顺序：
 * 1. FBase: 定义了 基本的 转换方法（ apply(...), thenApply(...) ），可以用 thenApply(...) 跳出流程，并完成编译
 * 2. FItem，继承了 FBase，拥有 FBase 的基本方法，可以用 thenApply(...) 跳出流程，并完成编译
 * 3. FList，也继承了 FBase，也拥有 FBase 的基本方法
 *           可以用 自带的 thenXxx(...) 方法跳出流程，并完成编译，包括 thenApply(...)
 *
 * FItem 中自身虽然没有 thenXxx 方法，去完成编译，但是 可以调用 FBase.thenApply(...) 去完成编译
 * FList 也是，可以调用 FBase.thenApply(...) 去完成编译
 */
public interface FunctionCompilerStates {

    /**
     * Methods allowed in both the {@link FItem} and {@link FList} compiler states.
     *
     * FBase 状态
     * TPrev 类型 为 FBase 的 初始类型
     * TFrom 类型 为 FBase 的 结束类型
     */
    interface FBase<TPrev, TFrom> {

        /**
         * Adds a {@link Function} to the behavior chain to be applied to the item.
         *
         * @param function the function to apply to the item
         *
         * FBase 状态下的 单类型转换方法
         * 需要传入一个 Function
         * 用于将 FBase 初始类型 TPrev 转换为 目标类型 TTo 后
         *
         * 返回一个 FItem 状态对象
         *
         * 然后 进入 FItem 状态：
         * TTo 类型 作为 FItem 的 初始类型 TPrev
         * TFrom 类型 作为 FItem 的 结束类型 TFrom
         */
        @NonNull <TTo> FItem<TTo, TFrom> apply(@NonNull Function<? super TPrev, TTo> function);

        /**
         * Adds a {@link Function} to the end of the behavior chain to be applied to the item.
         *
         * @param function the function to apply to the data
         *
         * FBase 状态下的 单类型转换方法
         * 需要传入一个 Function
         * 用于将 FBase 初始类型 TPrev 转换为 目标类型 TTo 后
         *
         * 返回一个 Function （ Function 的编译结束 ）
         *
         * 功能上与 apply(...) 相同， 唯一不同于 apply(...) 的是：
         * 这里 执行完 对应 的 转换功能 后，就结束 Function 的编译了 （ 返回 Function ）
         */
        @NonNull <TTo> Function<TFrom, TTo> thenApply(
                @NonNull Function<? super TPrev, TTo> function);
    }

    /**
     * Compiler state allowing to specify how the {@link Function} should modify single items.
     *
     * FItem 状态
     * TPrev 类型 为 FItem 的 初始类型
     * TFrom 类型 为 FItem 的 结束类型
     *
     * 同时，FItem 状态 继承了 FBase 状态：意味着，还能执行 apply(...) 或者 thenApply(...)
     */
    interface FItem<TPrev, TFrom> extends FBase<TPrev, TFrom> {
        /**
         * Adds a {@link Function} to the behavior chain to unpack an item into a {@link List},
         * allowing
         * list behaviors to be used from this point on.
         *
         * @param function the unpack function
         *
         * FItem 状态下的 包装方法
         * 需要传入一个 Function
         * 用于将 FItem 初始类型 TPrev 转换为 目标类型 的集合 List<TTo> 后
         *
         * 返回一个 FList 状态对象
         *
         * 然后 进入 FList 状态：
         * TTo 类型 作为 FList 的 初始类型 TPrev
         * List<TTo> 类型 作为 FList 的 初始类型 TPrevList
         * TFrom 类型 作为 FList 的 结束类型 TFrom
         */
        @NonNull <TTo> FList<TTo, List<TTo>, TFrom> unpack(
                @NonNull Function<? super TPrev, List<TTo>> function);
    }

    /**
     * Compiler state allowing to specify how the {@link Function} should modify {@link List}s.
     *
     * FList 状态
     * TPrev 类型 为 FItem 的 初始类型
     * TPrevList 类型 为 FItem 的 初始集合类型
     * TFrom 类型 为 FItem 的 结束类型
     *
     * 同时，FItem 状态 继承了 FBase<TPrevList, TFrom> 状态：意味着，还能执行 apply(...) 或者 thenApply(...)
     * 但是，执行 apply(...) 或者 thenApply(...) 的时候
     * 初始类型 为 TPrevList
     * 结束类型 为 TFrom
     */
    interface FList<TPrev, TPrevList, TFrom> extends FBase<TPrevList, TFrom> {

        /**
         * Adds a {@link Function} to the behavior chain to change the entire list to a new list.
         *
         * <p>The {@code morph} directive is functionally equivalent to {@code apply}, which treats
         * the
         * input list as a single item. But {@code morph} is aware of the list-typed output and
         * allows
         * list behaviors to follow immediately. Since the only difference between {@link #apply}
         * and
         * {@code morph} is the next state of the compiler, {@code thenMorph} does not exist since
         * {@link #thenApply} can be used in its place.
         *
         * @param function the function to apply to the list
         *
         * FList 状态下的 集合转换方法
         * 需要传入一个 Function
         * 用于将 FList 初始集合类型 List<TPrev> 转换为 目标集合类型 List<TTo> 后
         *
         * 返回一个 FList 状态对象
         *
         * 然后 还在 FList 状态：
         * TTo 类型 作为 FList 的 初始类型 TPrev
         * List<TTo> 类型 作为 FList 的 初始类型 TPrevList
         * TFrom 类型 作为 FList 的 结束类型 TFrom
         */
        @NonNull <TTo> FList<TTo, List<TTo>, TFrom> morph(
                @NonNull Function<List<TPrev>, List<TTo>> function);

        /**
         * Adds a {@link Function} to the behavior chain to map each item into a new type.
         *
         * @param function the function to apply to each item to create a new list
         *
         * FList 状态下的 单类型转换方法
         * 需要传入一个 Function
         * 用于将 FList 初始类型 TPrev 转换为 目标类型 TTo 后
         *
         * 返回一个 FList 状态对象
         *
         * 然后 还在 FList 状态：
         * TTo 类型 作为 FList 的 初始类型 TPrev
         * List<TTo> 类型 作为 FList 的 初始类型 TPrevList
         * TFrom 类型 作为 FList 的 结束类型 TFrom
         */
        @NonNull <TTo> FList<TTo, List<TTo>, TFrom> map(@NonNull Function<TPrev, TTo> function);

        /**
         * Adds a {@link Function} to the end of the behavior chain to map each item into a new
         * type.
         *
         * @param function the function to apply to each item to create a new list
         *
         * FList 状态下的 单类型转换方法
         * 需要传入一个 Function
         * 用于将 FList 初始类型 TPrev 转换为 目标类型 TTo 后
         *
         * 返回一个 Function （ Function 的编译结束 ）
         *
         * 功能上与 map(...) 相同， 唯一不同于 map(...) 的是：
         * 这里 执行完 对应 的 转换功能 后，就结束 Function 的编译了 （ 返回 Function ）
         */
        @NonNull <TTo> Function<TFrom, List<TTo>> thenMap(
                @NonNull Function<? super TPrev, TTo> function);

        /**
         * Adds a {@link Predicate} to the behavior chain to filter out items.
         *
         * @param filter the predicate to filter by
         *
         * FList 状态下的 过滤方法
         * 需要传入一个 Predicate
         * 判断 FList 初始类型 TPrev 的正确性（ 就是集合单个数据的判断过滤 ）
         *
         * 然后 还在 FList 状态：
         * 返回 初始状态、初始集合状态 以及 结束状态 于原来一样的 FList
         */
        @NonNull FList<TPrev, TPrevList, TFrom> filter(@NonNull Predicate<? super TPrev> filter);

        /**
         * Adds a max number of item limit to the behavior chain.
         *
         * @param limit the max number of items the list is limited to
         *
         * FList 状态下的 数量限制方法
         * 需要传入一个 limit 值
         * 执行 对应的 限制方法
         *
         * 然后 还在 FList 状态：
         * 返回 初始状态、初始集合状态 以及 结束状态 于原来一样的 FList
         */
        @NonNull FList<TPrev, TPrevList, TFrom> limit(int limit);

        /**
         * Adds a {@link Comparator} to the behavior chain to sort the items.
         *
         * @param comparator the comparator to sort the items
         *
         * FList 状态下的 排序方法
         * 需要传入一个 Comparator
         * 执行 对应的 Comparator 排序方法
         *
         * 然后 还在 FList 状态：
         * 返回 初始状态、初始集合状态 以及 结束状态 于原来一样的 FList
         */
        @NonNull FList<TPrev, TPrevList, TFrom> sort(@NonNull Comparator<TPrev> comparator);

        /**
         * Adds a {@link Predicate} to the end of the behavior chain to filter out items.
         *
         * @param filter the predicate to filter by
         *
         * FList 状态下的 过滤方法
         * 需要传入一个 Predicate
         * 判断 FList 初始类型 TPrev 的正确性（ 就是集合单个数据的判断过滤 ）
         *
         * 返回一个 Function （ Function 的编译结束 ）
         *
         * 功能上与 filter(...) 相同， 唯一不同于 filter(...) 的是：
         * 这里 执行完 对应 的 转换功能 后，就结束 Function 的编译了 （ 返回 Function ）
         */
        @NonNull Function<TFrom, TPrevList> thenFilter(@NonNull Predicate<? super TPrev> filter);

        /**
         * Adds a max number of item limit to the end of the behavior chain.
         *
         * @param limit the max number of items the list is limited to
         *
         * FList 状态下的 数量限制方法
         * 需要传入一个 limit 值
         * 执行 对应的 限制方法
         *
         * 返回一个 Function （ Function 的编译结束 ）
         *
         * 功能上与 limit(...) 相同， 唯一不同于 limit(...) 的是：
         * 这里 执行完 对应 的 转换功能 后，就结束 Function 的编译了 （ 返回 Function ）
         */
        @NonNull Function<TFrom, TPrevList> thenLimit(int limit);

        /**
         * Adds a {@link Comparator} to the behavior chain to sort the items.
         *
         * @param comparator the comparator to sort the items
         *
         * FList 状态下的 排序方法
         * 需要传入一个 Comparator
         * 执行 对应的 Comparator 排序方法
         *
         * 返回一个 Function （ Function 的编译结束 ）
         *
         * 功能上与 sort(...) 相同， 唯一不同于 sort(...) 的是：
         * 这里 执行完 对应 的 转换功能 后，就结束 Function 的编译了 （ 返回 Function ）
         * 返回 初始状态、初始集合状态 以及 结束状态 于原来一样的 FList
         */
        @NonNull Function<TFrom, TPrevList> thenSort(@NonNull Comparator<TPrev> comparator);
    }
}
