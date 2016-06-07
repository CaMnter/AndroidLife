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

import static com.google.android.agera.Common.FALSE_CONDICATE;
import static com.google.android.agera.Common.TRUE_CONDICATE;
import static com.google.android.agera.Preconditions.checkNotNull;

/**
 * Utility methods for obtaining {@link Condition} instances.
 *
 * Condition 的工具类
 * 可以获取 Condition
 */
public final class Conditions {

    /**
     * Returns a {@link Condition} that always returns {@code true}.
     *
     * 构造一个 StaticCondicate 实例化 作为 Condition
     * StaticCondicate 实现了 Condition 和 Predicate 接口
     *
     * 然后设置 构造参数为 true 作为 TRUE_CONDICATE
     * 即，判断好 为 true 的 Condition
     */
    @NonNull
    public static Condition trueCondition() {
        return TRUE_CONDICATE;
    }


    /**
     * Returns a {@link Condition} that always returns {@code false}.
     *
     * 构造一个 StaticCondicate 实例化 作为 Condition
     * StaticCondicate 实现了 Condition 和 Predicate 接口
     *
     * 然后设置 构造参数为 false 作为 FALSE_CONDICATE
     * 即，判断好 为 false 的 Condition
     */
    @NonNull
    public static Condition falseCondition() {
        return FALSE_CONDICATE;
    }


    /**
     * Returns a {@link Condition} that always returns {@code value}.
     *
     * 根据给定 value 判断返回
     * true 的 Condition（ StaticCondicate ）
     * 还是
     * false 的 Condition（ StaticCondicate ）
     */
    @NonNull
    public static Condition staticCondition(final boolean value) {
        return value ? TRUE_CONDICATE : FALSE_CONDICATE;
    }


    /**
     * Returns a {@link Condition} that negates the given {@code condition}.
     *
     * 条件取反
     *
     * true 的 Condition 会变为 false 的 Condition
     * false 的 Condition 会变为 true 的 Condition
     */
    @NonNull
    public static Condition not(@NonNull final Condition condition) {
        /*
         * 如果 是 NegatedCondition（ 相反条件类 ）
         * 调用自身的反转条件方法
         */
        if (condition instanceof NegatedCondition) {
            return ((NegatedCondition) condition).condition;
        }
        /*
         * true 的 Condition 会变为 false 的 Condition
         */
        if (condition == TRUE_CONDICATE) {
            return FALSE_CONDICATE;
        }
        /*
         * false 的 Condition 会变为 true 的 Condition
         */
        if (condition == FALSE_CONDICATE) {
            return TRUE_CONDICATE;
        }
        /*
         * 实例 一个 新的  NegatedCondition 相反条件类
         */
        return new NegatedCondition(condition);
    }


    /**
     * Returns a {@link Condition} that evaluates to {@code true} if any of the given
     * {@code conditions} evaluates to {@code true}. If {@code conditions} is empty, the returned
     * {@link Condition} will always evaluate to {@code false}.
     *
     * 只要有一个 条件 是 true -> 返回 true 的条件
     * 没有条件 -> 返回 false 的条件
     */
    @NonNull
    public static Condition any(@NonNull final Condition... conditions) {
        return composite(conditions, falseCondition(), trueCondition());
    }


    /**
     * Returns a {@link Condition} that evaluates to {@code true} if all of the given
     * {@code conditions} evaluates to {@code true}. If {@code conditions} is empty, the returned
     * {@link Condition} will always evaluate to {@code true}.
     *
     * 只要所有 条件 是 true -> 返回 true 的条件
     * 没有条件 -> 返回 true 的条件
     */
    @NonNull
    public static Condition all(@NonNull final Condition... conditions) {
        return composite(conditions, trueCondition(), falseCondition());
    }


    /**
     * Returns a {@link Condition} from a {@link Predicate} and a {@link Supplier}.
     *
     * <p>When applied the {@link Supplier} return value will be provided to the {@link Predicate}
     * and
     * the result will be returned.
     * If {@link Predicates#truePredicate} or {@link Predicates#falsePredicate} is passed,
     * {@code supplier} will never be called.
     *
     * 构造一个 Predicate -> Condition 转换器实例
     */
    @NonNull
    public static <T> Condition predicateAsCondition(@NonNull final Predicate<T> predicate,
                                                     @NonNull
                                                     final Supplier<? extends T> supplier) {
        /*
         * 如果是 true 的 StaticCondicate
         * 返回 该 StaticCondicate（ 因为实现了 Condition, Predicate 接口 ）
         */
        if (predicate == TRUE_CONDICATE) {
            return TRUE_CONDICATE;
        }
        /*
         * 如果是 false 的 StaticCondicate
         * 返回 该 StaticCondicate（ 因为实现了 Condition, Predicate 接口 ）
         */
        if (predicate == FALSE_CONDICATE) {
            return FALSE_CONDICATE;
        }
        // 实例化 Predicate -> Condition 转换器实例
        return new PredicateCondition<>(predicate, supplier);
    }


    @NonNull
    private static Condition composite(@NonNull final Condition[] conditions,
                                       @NonNull final Condition defaultCondition,
                                       @NonNull final Condition definingCondition) {
        int nonDefaultCount = 0;
        Condition lastNonDefaultCondition = null;
        /*
         * 先判断所有 条件
         * 是否 是 定义条件 （ definingCondition ）
         * 再判断是否 不是 默认条件（ defaultCondition ）
         */
        for (final Condition condition : conditions) {
            if (condition == definingCondition) {
                return definingCondition;
            } else if (condition != defaultCondition) {
                nonDefaultCount++;
                lastNonDefaultCondition = condition;
            }
        }
        // 如果 条件 全都都 不是定义条件 但是 是默认条件
        if (nonDefaultCount == 0) {
            return defaultCondition;
        } else if (nonDefaultCount == 1) {
            // 如果只有一次 不是默认条件，就返回那次条件
            return lastNonDefaultCondition;
        }
        /*
         * 实在判断出去来
         * 实例化一个 CompositeCondition
         * 交给 CompositeCondition 接着判断
         */
        return new CompositeCondition(conditions.clone(), definingCondition.applies());
    }


    /**
     * CompositeCondition 复合条件类
     * 实现了 Condition 接口
     *
     * 需要传入一组 Condition ，和一个 预期结果 definingResult
     * 只要有一个条件判断后达到 预期结果 definingResult
     * 就返回 definingResult
     * 否则，返回 !definingResult
     */
    private static final class CompositeCondition implements Condition {
        @NonNull
        private final Condition[] conditions;
        private final boolean definingResult;


        CompositeCondition(@NonNull final Condition[] conditions, final boolean definingResult) {
            this.definingResult = definingResult;
            this.conditions = checkNotNull(conditions);
        }


        /**
         * 只要有一个条件判断后达到 预期结果 definingResult
         * 就返回 definingResult
         * 否则，返回 !definingResult
         */
        @Override
        public boolean applies() {
            for (final Condition condition : conditions) {
                if (condition.applies() == definingResult) {
                    return definingResult;
                }
            }
            return !definingResult;
        }
    }

    /**
     * NegatedCondition 实现了 Condition 接口
     * 并实现了 相反条件
     * 就是
     * 1. true -> false
     * 2. false -> true
     */
    private static final class NegatedCondition implements Condition {
        @NonNull
        final Condition condition;


        NegatedCondition(@NonNull final Condition condition) {
            this.condition = condition;
        }


        /**
         * 条件反转
         *
         * @return 反转后的条件
         */
        @Override
        public boolean applies() {
            return !condition.applies();
        }
    }

    /**
     * PredicateCondition  供应者断定条件
     * 实现了 Condition 接口，但是内部逻辑实质上 走的是 Predicate 的断定逻辑
     *
     * @param <T> 供应者 目标类型，也是 断定者 的判断类型
     *
     * 通过 供应者 提供的数据，断定者 进行断定此数据的逻辑
     * 然后调用自身 身为 Condition 的方法 applies()，将 断定者 的断定结果返回
     */
    private static final class PredicateCondition<T> implements Condition {
        @NonNull
        private final Predicate<T> predicate;
        @NonNull
        private final Supplier<? extends T> supplier;


        PredicateCondition(@NonNull final Predicate<T> predicate,
                           @NonNull final Supplier<? extends T> supplier) {
            this.predicate = checkNotNull(predicate);
            this.supplier = checkNotNull(supplier);
        }


        /**
         * 通过 供应者 提供的数据，断定者 进行断定此数据的逻辑
         * 然后调用自身 身为 Condition 的方法 applies()，将 断定者 的断定结果返回
         */
        @Override
        public boolean applies() {
            return predicate.apply(supplier.get());
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Conditions() {}
}
