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
 * Exception style class encapsulating Volley errors
 */

/* @formatter:off
 *
 * 继承自 Exception
 * 用于描述 Volley 中所有的错误异常
 * 可以设置 NetworkResponse 和 请求消耗时间
 */
@SuppressWarnings("serial")
public class VolleyError extends Exception {

    // NetworkResponse
    public final NetworkResponse networkResponse;
    // 请求用时
    private long networkTimeMs;


    /**
     * 无参构造方法
     */
    public VolleyError() {
        networkResponse = null;
    }


    /**
     * 如果传入 NetworkResponse
     * 那么就是自己的构造方法
     *
     * @param response response
     */
    public VolleyError(NetworkResponse response) {
        networkResponse = response;
    }


    /**
     * 如果只传入一个 String 进来
     * 那么会走 Exception(String detailMessage)
     *
     * @param exceptionMessage exceptionMessage
     */
    public VolleyError(String exceptionMessage) {
        super(exceptionMessage);
        networkResponse = null;
    }


    /**
     * 如果只传入一个 String 和 Throwable 进来
     * 那么会走 Exception(String detailMessage, Throwable throwable)
     *
     * @param exceptionMessage exceptionMessage
     * @param reason reason
     */
    public VolleyError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        networkResponse = null;
    }


    /**
     * 如果只传入一个 Throwable 进来
     * 那么会走 Exception(Throwable throwable)
     *
     * @param cause cause
     */
    public VolleyError(Throwable cause) {
        super(cause);
        networkResponse = null;
    }


    /**
     * 设置 请求用时
     */
    /* package */ void setNetworkTimeMs(long networkTimeMs) {
        this.networkTimeMs = networkTimeMs;
    }


    /**
     * 获取 请求用时
     *
     * @return networkTimeMs
     */
    public long getNetworkTimeMs() {
        return networkTimeMs;
    }
}
