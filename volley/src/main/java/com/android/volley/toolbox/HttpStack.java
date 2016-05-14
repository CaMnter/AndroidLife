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

package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpResponse;

/**
 * An HTTP stack abstraction.
 */
/*
 * HttpStack 是 Volley 内 处理网络请求的接口
 * 实现类有：
 * 1. HttpClientStack：基于 org.apache.http 的网络请求实现。负责 系统版本 2.3 以下的网络请求。
 * 2. HurlStack：基于 HttpURLConnection 的网络请求实现。负责 系统版本 2.3 以上的网络请求。
 */
public interface HttpStack {
    /**
     * Performs an HTTP request with the given parameters.
     *
     * <p>A GET request is sent if request.getPostBody() == null. A POST request is sent otherwise,
     * and the Content-Type header is set to request.getPostBodyContentType().</p>
     *
     * @param request the request to perform
     * @param additionalHeaders additional headers to be sent together with
     * {@link Request#getHeaders()}
     * @return the HTTP response
     */

    /*
     * 执行请求
     * 这里解释的的是：
     * request.getPostBody() == null，发送一个 GET 请求
     * request.getPostBody() != null，发送一个 POST 请求
     *
     * additionalHeaders 请求头信息
     */
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError;
}
