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

import org.apache.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */

/*
 * NetworkResponse 的作用：保存 Network 实现类 （ 目前 Volley 内就只有 BasicNetwork ）
 * 执行 performRequest(...) 得到的请求结果 和 头信息
 */
public class NetworkResponse {
    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304 and the data was already in cache
     * @param networkTimeMs Round-trip network time to receive network response
     */

    /*
     * statusCode：HTTP 状态码
     * data：请求结果 Response body
     * headers：请求结果 头信息
     * notModified：HTTP 状态码 304 -> SC_NOT_MODIFIED -> true，否则为 false
     * networkTimeMs：请求时长
     */
    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers,
            boolean notModified, long networkTimeMs) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.notModified = notModified;
        this.networkTimeMs = networkTimeMs;
    }

    /*
     * networkTimeMs 时长默认设置为 0
     */
    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers,
            boolean notModified) {
        this(statusCode, data, headers, notModified, 0);
    }

    /*
     * statusCode：HTTP 状态码，默认为 200 -> SC_OK
     * headers：请求结果 头信息，默认设置一个 EmptyMap
     * notModified：默认不是 304，即默认为 false
     * networkTimeMs 时长默认设置为 0
     */
    public NetworkResponse(byte[] data) {
        this(HttpStatus.SC_OK, data, Collections.<String, String>emptyMap(), false, 0);
    }

    /*
     * statusCode：HTTP 状态码，默认为 200 -> SC_OK
     * notModified：默认不是 304，即默认为 false
     * networkTimeMs 时长默认设置为 0
     */
    public NetworkResponse(byte[] data, Map<String, String> headers) {
        this(HttpStatus.SC_OK, data, headers, false, 0);
    }

    /** The HTTP status code. */
    // HTTP 状态码
    public final int statusCode;

    /** Raw data from this response. */
    // 请求结果 Response body
    public final byte[] data;

    /** Response headers. */
    // 请求结果 头信息
    public final Map<String, String> headers;

    /** True if the server returned a 304 (Not Modified). */
    // HTTP 状态码 304 -> SC_NOT_MODIFIED -> true，否则为 false
    public final boolean notModified;

    /** Network roundtrip time in milliseconds. */
    // 请求时长
    public final long networkTimeMs;
}

