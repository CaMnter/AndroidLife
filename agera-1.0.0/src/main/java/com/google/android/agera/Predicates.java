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
 * Utility methods for obtaining {@link Predicate} instances.
 *
 * Predicate 的工具类
 * 可以获取 Predicate
 */
public final class Predicates {

    /**
     * 实例化一个 长度为0字符串 断定类
     */
    private static final Predicate<CharSequence> EMPTY_STRING_PREDICATE
        = new EmptyStringPredicate();


    /**
     * Returns a {@link Predicate} from a {@link Condition}.
     *
     * <p>When applied the {@link Predicate} input parameter will be ignored and the result of
     * {@code condition} will be returned.
     *
     * 构造一个 Condition -> Predicate 转换器实例
     */
    @NonNull
    public static <T> Predicate<T> conditionAsPredicate(@NonNull final Condition condition) {

        // 如果是 StaticCondicate 类型，直接返回
        if (condition == TRUE_CONDICATE) {
            return truePredicate();
        }
        // 如果是 StaticCondicate 类型，直接返回
        if (condition == FALSE_CONDICATE) {
            return falsePredicate();
        }
        // 返回一个 Condition -> Predicate 转换器实例
        return new ConditionAsPredicate<>(condition);
    }


    /**
     * Returns a {@link Predicate} that always returns {@code true}.
     *
     * 构造一个 StaticCondicate 实例化 作为 Predicate
     * StaticCondicate 实现了 Condition 和 Predicate 接口
     *
     * 然后设置 构造参数为 true 作为 TRUE_CONDICATE
     * 即，判断好 为 true 的 Predicate
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> truePredicate() {
        return TRUE_CONDICATE;
    }


    /**
     * Returns a {@link Predicate} that always returns {@code false}.
     *
     * 构造一个 StaticCondicate 实例化 作为 Predicate
     * StaticCondicate 实现了 Condition 和 Predicate 接口
     *
     * 然后设置 构造参数为 false 作为 FALSE_CONDICATE
     * 即，判断好 为 false 的 Predicate
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> falsePredicate() {
        return FALSE_CONDICATE;
    }


    /**
     * Returns a {@link Predicate} that indicates whether {@code object} is equal to the
     * {@link Predicate} input.
     *
     * 构造 一个 EqualToPredicate 相等断定类
     */
    @NonNull
    public static <T> Predicate<T> equalTo(@NonNull final T object) {
        return new EqualToPredicate<>(object);
    }


    /**
     * Returns a {@link Predicate} that indicates whether the {@link Predicate} input is an
     * instance of {@code type}.
     *
     * 构造 一个 InstanceOfPredicate 相等断定类
     */
    @NonNull
    public static <T> Predicate<T> instanceOf(@NonNull final Class<?> type) {
        return new InstanceOfPredicate<>(type);
    }


    /**
     * Returns a {@link Predicate} that indicates whether the {@link Predicate} input is an
     * empty {@link CharSequence}.
     *
     * 返回实例好的 一个 长度为0字符串 断定类
     */
    @NonNull
    public static Predicate<CharSequence> emptyString() {
        return EMPTY_STRING_PREDICATE;
    }


    /**
     * Returns a {@link Predicate} that negates {@code predicate}.
     *
     * 反向断定
     *
     * true 的 Predicate 会变为 false 的 Predicate
     * false 的 Predicate 会变为 true 的 Predicate
     */
    @NonNull
    public static <T> Predicate<T> not(@NonNull final Predicate<T> predicate) {
        /*
         * 如果 是 NegatedPredicate（ 相反断定类 ）
         * 调用自身的反转条件方法
         */
        if (predicate instanceof NegatedPredicate) {
            return ((NegatedPredicate<T>) predicate).predicate;
        }
        /*
         * true 的 Predicate 会变为 false 的 Predicate
         */
        if (predicate == truePredicate()) {
            return falsePredicate();
        }
        /*
         * false 的 Predicate 会变为 true 的 Predicate
         */
        if (predicate == falsePredicate()) {
            return truePredicate();
        }

        /*
         * 实例 一个 新的  NegatedPredicate 相反断定类
         */
        return new NegatedPredicate<>(predicate);
    }


    /**
     * Returns a {@link Predicate} that evaluates to {@code true} if any of the given
     * {@code predicates} evaluates to {@code true}. If {@code predicates} is empty, the returned
     * {@link Predicate} will always evaluate to {@code false}.
     *
     * 只要有一个 条件 是 true -> 返回 true 的条件
     * 没有条件 -> 返回 false 的条件
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    @NonNull
    public static <T> Predicate<T> any(@NonNull final Predicate<? super T>... predicates) {
        return composite(predicates, falsePredicate(), truePredicate(), true);
    }


    /**
     * Returns a {@link Predicate} that evaluates to {@code true} if all of the given
     * {@code conditions} evaluates to {@code true}. If {@code conditions} is empty, the returned
     * {@link Condition} will always evaluate to {@code true}.
     *
     * 只要所有 条件 是 true -> 返回 true 的条件
     * 没有条件 -> 返回 true 的条件
     */
    @SafeVarargs
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> all(@NonNull final Predicate<? super T>... predicates) {
        return composite(predicates, truePredicate(), falsePredicate(), false);
    }


    @SuppressWarnings("unchecked")
    @NonNull
    private static Predicate composite(@NonNull final Predicate[] predicates,
                                       @NonNull final Predicate defaultPredicate,
                                       @NonNull final Predicate definingPredicate,
                                       final boolean definingResult) {
        int nonDefaultCount = 0;
        Predicate lastNonDefaultPredicate = null;
        /*
         * 先判断所有 断定
         * 是否 是 定义断定 （ definingPredicate ）
         * 再判断是否 不是 默认断定（ defaultPredicate ）
         */
        for (final Predicate predicate : predicates) {
            if (predicate == definingPredicate) {
                return definingPredicate;
            } else if (predicate != defaultPredicate) {
                nonDefaultCount++;
                lastNonDefaultPredicate = predicate;
            }
        }
        // 如果 断定 全都都 不是定义断定 但是 是默认断定
        if (nonDefaultCount == 0) {
            return defaultPredicate;
        } else if (nonDefaultCount == 1) {
            // 如果只有一次 不是默认断定，就返回那次断定
            return lastNonDefaultPredicate;
        }
        /*
         * 实在判断不出来
         * 实例化一个 CompositePredicate
         * 交给 CompositePredicate 接着判断
         */
        return new CompositePredicate<>(predicates.clone(), definingResult);
    }


    /**
     * CompositePredicate 复合断定类
     * 实现了 Predicate 接口
     *
     * 需要传入一组 Predicate ，和一个 预期结果 definingResult
     * 只要有一个断定 达到 预期结果 definingResult
     * 就返回 definingResult
     * 否则，返回 !definingResult
     *
     * @param <T> Predicate 的目标类型
     */
    private static final class CompositePredicate<T> implements Predicate<T> {
        @NonNull
        private final Predicate<T>[] predicates;
        private final boolean definingResult;


        CompositePredicate(@NonNull final Predicate<T>[] predicates, final boolean definingResult) {
            this.definingResult = definingResult;
            this.predicates = checkNotNull(predicates);
        }


        /**
         * 只要有一个条件判断 达到 预期结果 definingResult
         * 就返回 definingResult
         * 否则，返回 !definingResult
         */
        @Override
        public boolean apply(@NonNull final T value) {
            for (final Predicate<T> predicate : predicates) {
                if (predicate.apply(value) == definingResult) {
                    return definingResult;
                }
            }
            return !definingResult;
        }
    }


    /**
     * EmptyStringPredicate 长度为0的字符串 的断定类
     * 实现了 Predicate 接口
     * 仅仅是判断改 字符串 长度 是否为 0，返回 true
     * 如果字符串 长度不为 0，返回 false
     */
    private static final class EmptyStringPredicate implements Predicate<CharSequence> {

        @Override
        public boolean apply(@NonNull final CharSequence input) {
            return input.length() == 0;
        }
    }


    /**
     * NegatedPredicate 相反断定类
     * 并实现了 相反断定
     * 就是
     * 1. true -> false
     * 2. false -> true
     *
     * @param <T> Predicate 的目标类型
     */
    private static final class NegatedPredicate<T> implements Predicate<T> {
        @NonNull
        private final Predicate<T> predicate;


        NegatedPredicate(@NonNull final Predicate<T> predicate) {
            this.predicate = checkNotNull(predicate);
        }


        /**
         * 条件反转
         *
         * @param t Predicate 的目标类型数据
         * @return 反转后的条件
         */
        @Override
        public boolean apply(@NonNull final T t) {
            return !predicate.apply(t);
        }
    }


    /**
     * ConditionAsPredicate 作为
     * Condition -> Predicate 的转换器
     * ConditionAsPredicate 实现了 Predicate 接口
     * 实现了 apply(...) 方法，并且将 Condition 的 applies() 方法逻辑对接到
     * 自身的作为 Predicate 的 apply(...) 方法上
     *
     * @param <T> Predicate 的目标类型
     */
    private static final class ConditionAsPredicate<T> implements Predicate<T> {
        @NonNull
        private final Condition condition;


        ConditionAsPredicate(@NonNull final Condition condition) {
            this.condition = checkNotNull(condition);
        }


        @Override
        public boolean apply(@NonNull T input) {
            return condition.applies();
        }
    }


    /**
     * InstanceOfPredicate 超类或接口判断类
     * 实现了 Predicate 接口
     * 用传入的 type 类，来判断 type 类 是不是 断定 的数据类 type 超类或者接口
     * 调用的是 Class.isAssignableFrom(Class<?> c)
     *
     * @param <T> 要判断的数据类型
     */
    private static final class InstanceOfPredicate<T> implements Predicate<T> {
        @NonNull
        private final Class<?> type;


        InstanceOfPredicate(@NonNull final Class<?> type) {
            this.type = checkNotNull(type);
        }


        @Override
        public boolean apply(@NonNull final T input) {
            return type.isAssignableFrom(input.getClass());
        }
    }


    /**
     * EqualToPredicate 相等断定类
     * 实现了 Predicate 接口
     * 判断数据是否相等，调用的 是 equals(...) 方法
     *
     * @param <T> 要判断的数据类型
     */
    private static final class EqualToPredicate<T> implements Predicate<T> {
        @NonNull
        private final T object;


        EqualToPredicate(@NonNull final T object) {
            this.object = checkNotNull(object);
        }


        @Override
        public boolean apply(@NonNull final T input) {
            return input.equals(object);
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Predicates() {}
}
