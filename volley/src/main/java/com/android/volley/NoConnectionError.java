/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley;

/**
 * Error indicating that no connection could be established when performing a Volley request.
 */

/* @formatter:off
 *
 * NoConnectionError 继承了 NetworkError
 * 表示：网络错误
 *
 * 添加了
 * 1.无参构造方法
 * 2.NoConnectionError(Throwable reason)
 *
 * 到底层都是调用了 Exception 的构造方法
 */
@SuppressWarnings("serial")
public class NoConnectionError extends NetworkError {
    public NoConnectionError() {
        super();
    }

    public NoConnectionError(Throwable reason) {
        super(reason);
    }
}
