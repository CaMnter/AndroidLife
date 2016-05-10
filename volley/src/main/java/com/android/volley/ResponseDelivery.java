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

/*
 * ResponseDelivery 接口的作用：从 内存缓存 或者 服务器 取得请求的数据，由 ResponseDelivery 去做结果分发及回调处理。
 *
 * 默认基本实现在 : ExecutorDelivery
 */
public interface ResponseDelivery {
    /**
     * Parses a response from the network or cache and delivers it.
     */
    // 传递 请求 以及 请求结果
    public void postResponse(Request<?> request, Response<?> response);

    /**
     * Parses a response from the network or cache and delivers it. The provided
     * Runnable will be executed after delivery.
     */
    /*
     * 传递 请求、请求结果 以及
     * 还可以提供一个 Runnable 去执行相应的回调处理
     */
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    /**
     * Posts an error for the given request.
     */
    // 传递 请求 以及 相应的错误
    public void postError(Request<?> request, VolleyError error);
}
