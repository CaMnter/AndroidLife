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
 * Precondition checks.
 *
 * 通用 检查 工具类
 */
public final class Preconditions {
    /**
     * 检查状态
     * 只要 expression = false，就抛出 指定错误信息的 IllegalStateException
     *
     * @param expression 状态
     * @param errorMessage 错误信息
     */
    public static void checkState(final boolean expression, @NonNull final String errorMessage) {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }


    /**
     * 目前，同 checkState(...) 方法
     */
    public static void checkArgument(final boolean expression, @NonNull final String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }


    /**
     * 检查 数据是否不为 null
     *
     * @param object 目标数据
     * @param <T> 目标类型
     * @return 合法的目标数据
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static <T> T checkNotNull(@NonNull final T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return object;
    }


    /**
     * 屏蔽默认的构造方法
     */
    private Preconditions() {}
}
