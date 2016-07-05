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

import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Result.failure;

final class Common {
    // Throwable -> Result 的 Function
    static final Function<Throwable, ? extends Result<?>> FAILED_RESULT = new FailedResult<>();
    // 通用的 Function
    static final Function IDENTITY_FUNCTION = new IdentityFunction();
    /*
     * 正确的 断定 和 条件
     */
    static final StaticCondicate TRUE_CONDICATE = new StaticCondicate(true);
    /*
     * 错误的 断定 和 条件
     */
    static final StaticCondicate FALSE_CONDICATE = new StaticCondicate(false);


    /**
     * 通用的 Function
     * 主要用于 转为 Function
     */
    private static final class IdentityFunction implements Function {
        /**
         * 什么也不做，直接返回 from 值
         *
         * @param from from
         * @return from
         */
        @NonNull
        @Override
        public Object apply(@NonNull final Object from) {
            return from;
        }
    }


    /**
     * 主要用于
     * 转为 Condition, Predicate
     */
    private static final class StaticCondicate implements Condition, Predicate {
        private final boolean staticValue;


        private StaticCondicate(final boolean staticValue) {
            this.staticValue = staticValue;
        }


        /**
         * 不做任何 Predicate 的断定逻辑
         * 直接返回 staticValue 值
         *
         * @param value value
         * @return staticValue
         */
        @Override
        public boolean apply(@NonNull final Object value) {
            return staticValue;
        }


        /**
         * 不做任何 Condition 的条件逻辑
         * 直接返回 staticValue 值
         *
         * @return staticValue
         */
        @Override
        public boolean applies() {
            return staticValue;
        }
    }


    /**
     * 主要用于
     * 转为 Supplier, Function, Merger
     *
     * @param <TFirst> 第一个参数类型
     * @param <TSecond> 第二个参数类型
     * @param <TTo> 目标类型
     */
    static final class StaticProducer<TFirst, TSecond, TTo>
        implements Supplier<TTo>, Function<TFirst, TTo>, Merger<TFirst, TSecond, TTo> {
        @NonNull
        private final TTo staticValue;


        StaticProducer(@NonNull final TTo staticValue) {
            this.staticValue = checkNotNull(staticValue);
        }


        /**
         * 不做任何 Function 的转换逻辑
         * 直接返回 staticValue 值
         *
         * @param input 第一个参数值
         * @return staticValue
         */
        @NonNull
        @Override
        public TTo apply(@NonNull final TFirst input) {
            return staticValue;
        }


        /**
         * 不做任何 Merger 的合并逻辑
         * 直接返回 staticValue 值
         *
         * @param o 第一个参数值
         * @param o2 第二个参数值
         * @return staticValue
         */
        @NonNull
        @Override
        public TTo merge(@NonNull final TFirst o, @NonNull final TSecond o2) {
            return staticValue;
        }


        /**
         * 直接返回 staticValue 值
         *
         * @return staticValue
         */
        @NonNull
        @Override
        public TTo get() {
            return staticValue;
        }
    }


    /**
     * 主要用于 转为 Function
     *
     * @param <T> Result 的类型
     */
    private static final class FailedResult<T> implements Function<Throwable, Result<T>> {

        /**
         * Throwable -> 错误 Result
         *
         * @param input Throwable
         * @return 返回一个 错误 Result
         */
        @NonNull
        @Override
        public Result<T> apply(@NonNull final Throwable input) {
            return failure(input);
        }
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Common() {}
}
