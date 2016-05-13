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
 * Encapsulates a parsed response for delivery.
 *
 * @param <T> Parsed type of this response
 */

/*
 * Response  请求结果（响应）类
 */
public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    /*
     * 回调接口
     * 提供 解析好的 Response
     */
    public interface Listener<T> {
        /** Called when a response is received. */
        /*
         * 当接受到 请求结果（响应）时，将解析结果返回
         */
        public void onResponse(T response);
    }

    /** Callback interface for delivering error responses. */
    /*
     * 回调接口
     * 提供 请求 发生的错误
     */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        /*
         * 发生错误时，将调用该方法
         * 将返回 VolleyError
         */
        public void onErrorResponse(VolleyError error);
    }


    /** Returns a successful response containing the parsed result. */
    /*
     * 返回一个 成功的请求结果（响应）和解析结果
     */
    public static <T> Response<T> success(T result, Cache.Entry cacheEntry) {
        return new Response<T>(result, cacheEntry);
    }


    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    /*
     * 返回一个 失败的请求结果（响应）
     */
    public static <T> Response<T> error(VolleyError error) {
        return new Response<T>(error);
    }


    /** Parsed response, or null in the case of error. */
    /*
     * 在未发生错误的情况下
     * 成功情况下的 请求结果（响应）解析数据
     */
    public final T result;

    /** Cache metadata for this response, or null in the case of error. */
    /*
     * 在未发生错误的情况下
     * 成功的情况下的 请求结果（响应）缓存数据
     */
    public final Cache.Entry cacheEntry;

    /** Detailed error information if <code>errorCode != OK</code>. */
    // 记录请求过程中发生的促欧文
    public final VolleyError error;

    /** True if this response was a soft-expired one and a second one MAY be coming. */
    /*
     * Request.intermediate=true 只会在 CacheDispatcher.run() 需要 刷新的情况下 触发
     * 可以知道 如果是用的是 缓存 Request，以及缓存 Request 解析出的 Response
     *
     * // Mark the response as intermediate.
     * response.intermediate = true;
     *
     * // Post the intermediate response back to the user and have
     * // the delivery then forward the request along to the network.
     * mDelivery.postResponse(request, response, new Runnable() {
     *     @Override
     *     public void run() {
     *         try {
     *             mNetworkQueue.put(request);
     *         } catch (InterruptedException e) {
     *             // Not much we can do about this.
     *         }
     *     }
     *});
     *
     * 注： 这里 之所以 Request.intermediate=true 的可能性，只发生在 缓存请求 需要刷新
     * 不然，都有了响应，就不需要再次请求了（ 执行 finish("done") ），所以不属于 刷新缓存 的
     * 情景都 要 finish("done")
     *
     * 属于 刷新缓存 的情况，会将请求加入到 NetworkQueue 内。
     *
     * 所以得出了 如果从缓存中拿出 Request 判断需要刷新的话
     */
    public boolean intermediate = false;


    /**
     * Returns whether this response is considered successful.
     */
    // 判断这次 请求结果（响应）是否成功
    public boolean isSuccess() {
        return error == null;
    }


    /**
     * 构造方法：用于实例化一个请求正常的 Response
     *
     * @param result 请求结果（响应）解析数据
     * @param cacheEntry 缓存数据
     */
    private Response(T result, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
    }


    /**
     * 构造方法：用于实例化一个请求食饱的 Response
     *
     * @param error 请求错误
     */
    private Response(VolleyError error) {
        this.result = null;
        this.cacheEntry = null;
        this.error = error;
    }
}
