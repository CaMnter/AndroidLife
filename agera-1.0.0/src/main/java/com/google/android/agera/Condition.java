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

/**
 * Determines whether a condition applies.
 *
 * <p>The {@link Conditions} class provides common conditions and related utilities.
 *
 * Agera 中抽象出来的 条件 接口
 * 用于判断一个条件是否适用
 * Conditions 中 提供常见的 条件实现类 和 相关工具
 * 和 断定（ Predicate ）接口的区别在于：没有根据输入值去判断
 */
public interface Condition {

    /**
     * Returns whether the condition applies.
     * 返回 条件是否适用
     */
    boolean applies();
}
