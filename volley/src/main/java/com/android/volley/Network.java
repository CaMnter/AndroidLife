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
 * An interface for performing requests.
 */

/*
 * Volley 中 用于 处理 网络请求 的接口
 * 只有一个方法：performRequest(...) 执行请求
 *
 * performRequest(...) 的要做的事情是：
 * 1. 调用  HttpStack 接口的实现类 （ HurlStack, HttpClientStack ） 去执行网络请求
 *    拿到一个 Apache HttpResponse
 * 2. 将 Apache HttpResponse -> Volley NetworkResponse 进行转化，并返回
 *
 * Network 的 实现类有 BasicNetwork
 */
public interface Network {
    /**
     * Performs the specified request.
     *
     * @param request Request to process
     * @return A {@link NetworkResponse} with data and caching metadata; will never be null
     * @throws VolleyError on errors
     */
    /*
     * 执行处理 Volley内的 抽象请求 Request<?>
     * 但是 HttpStack 处理后，都返回 Apache 的请求结果（ HttpResponse ）
     * performRequest(...) 接下来会将：Apache HttpResponse -> Volley NetworkResponse 进行转化
     */
    public NetworkResponse performRequest(Request<?> request) throws VolleyError;
}
