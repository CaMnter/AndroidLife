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
import com.android.volley.VolleyLog;
import java.io.UnsupportedEncodingException;

/**
 * A request for retrieving a T type response body at a given URL that also
 * optionally sends along a JSON body in the request specified.
 *
 * @param <T> JSON type of response expected
 */
/*
 * JsonRequest<T> 抽象继承了 Request<T> 类
 *
 * JsonRequest<T>：
 * 1. 进行了 Request<T> -> JsonRequest<T> 的转换
 * 2. 将请求结果数据 根据 "charset=utf-8" 转换为 byte[]
 * 3. 通过覆写 getBodyContentType() 方法，将 Content-Type 设置为 application/json; charset=utf-8
 */
public abstract class JsonRequest<T> extends Request<T> {
    /** Default charset for JSON request. */
    // Content-Type 中 默认的 charset=utf-8
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    /*
     * Content-Type：application/json; charset=utf-8
     */
    private static final String PROTOCOL_CONTENT_TYPE = String.format(
            "application/json; charset=%s", PROTOCOL_CHARSET);

    // 解析结果的 回调接口
    private final Listener<T> mListener;
    // 保存 请求结果 （ Response ） 数据
    private final String mRequestBody;


    /**
     * Deprecated constructor for a JsonRequest which defaults to GET unless {@link #getPostBody()}
     * or {@link #getPostParams()} is overridden (which defaults to POST).
     *
     * @deprecated Use {@link #JsonRequest(int, String, String, Listener, ErrorListener)}.
     */
    /*
     * 默认方法：Method.DEPRECATED_GET_OR_POST -> GET 或 POST 请求
     */
    public JsonRequest(String url, String requestBody, Listener<T> listener, ErrorListener errorListener) {
        this(Method.DEPRECATED_GET_OR_POST, url, requestBody, listener, errorListener);
    }


    public JsonRequest(int method, String url, String requestBody, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mRequestBody = requestBody;
    }


    /*
     * 回调 解析结果
     */
    @Override protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }


    /*
     * 这里没做解析网络请求结果的逻辑
     * 而是将这个解析任务强制抽象抛给子类（ JsonObjectRequest, JsonArrayRequest ）
     */
    @Override abstract protected Response<T> parseNetworkResponse(NetworkResponse response);


    /**
     * @deprecated Use {@link #getBodyContentType()}.
     */
    /*
     * 由于修改了 getBodyContentType() 方法
     * 为了 老版本，兼容 getPostBodyContentType() 方法
     * 将逻辑嵌入 getBodyContentType() 方法
     */
    @Override public String getPostBodyContentType() {
        return getBodyContentType();
    }


    /**
     * @deprecated Use {@link #getBody()}.
     */

    /*
     * 由于修改了 getBody() 方法
     * 为了 老版本，兼容 getPostBody() 方法
     * 将逻辑嵌入 getBody() 方法中
     */
    @Override public byte[] getPostBody() {
        return getBody();
    }


    /*
     * 覆写 Request<T>.getBodyContentType() 方法
     * 将原来的 application/x-www-form-urlencoded; charset=UTF-8
     * 改为
     * 现在的 PROTOCOL_CONTENT_TYPE： application/json; charset=utf-8
     */
    @Override public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }


    /*
     * 判断 请求结果（ 响应 ） NetworkResponse 数据 是否不为 null
     * 将请求结果数据 根据 "charset=utf-8" 转换为 byte[]
     */
    @Override public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }
}
