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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import java.io.UnsupportedEncodingException;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */

/*
 * 继承扩展了 Request，指定了泛型为 <String>
 * 会将请求结果解析成 String 类型数据
 * 并且 需要你 传入一个 Response.Listener<String> 进行解析结果数据进行回调
 */
public class StringRequest extends Request<String> {
    private final Listener<String> mListener;

    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public StringRequest(int method, String url, Listener<String> listener,
            ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new GET request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    /*
     * 默认的 请求方法 为 GET 请求方法
     */
    public StringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    /*
     * 扩展实现传递 数据方法
     * 由于指定了泛型 <String>，所以这里也只能 传递 String 类型数据
     */
    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    /*
     * 解析网络请求 为 String 类型
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            /*
             * 1. HttpHeaderParser 解析编码集
             * 2. 将请求结果 Response 的数据 （ data ）通过编码集实例化一个数据 （ data ） String 类型
             */
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            // 出现编码异常，就不进行编码处理，直接实例化一个 String
            parsed = new String(response.data);
        }
        /*
         * 解析成功，没有异常
         * 1. 将保存好 通过编码解析的 String 数据 返回
         * 2. 再通过 HttpHeaderParser 从网络请求回来的请求结果 NetworkResponse 的 Header 中提取出一个用于缓存的 Cache.Entry
         */
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}
