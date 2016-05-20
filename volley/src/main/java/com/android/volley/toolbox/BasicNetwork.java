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

import android.os.SystemClock;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.ClientError;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

/**
 * A network performing Volley requests over an {@link HttpStack}.
 */

/*
 * BasicNetwork 是 目前 Volley 内，Network 接口的唯一实现
 *
 * performRequest(...) 的要做的事情是：
 * 1. 调用  HttpStack 接口的实现类 （ HurlStack, HttpClientStack ） 去执行网络请求
 *    拿到一个 Apache HttpResponse
 * 2. 将 Apache HttpResponse -> Volley NetworkResponse 进行转化，并返回
 */
public class BasicNetwork implements Network {
    // 获取 VolleyLog 中的 debug 开关
    protected static final boolean DEBUG = VolleyLog.DEBUG;

    // 请求 debug log 的最小间隔
    private static int SLOW_REQUEST_THRESHOLD_MS = 3000;

    // ByteArrayPool 池的默认 byte[] 总长度之和（ 容量 ）
    private static int DEFAULT_POOL_SIZE = 4096;

    // HttpStack 接口 （ HurlStack, HttpClientStack ）
    protected final HttpStack mHttpStack;

    /*
     * ByteArrayPool
     * 用于 byte[] 的回收再利用，减少内存分配和回收。
     */
    protected final ByteArrayPool mPool;


    /**
     * @param httpStack HTTP stack to be used
     */
    /*
     * 默认的 ByteArrayPool：是一个 容量为 4096 的 ByteArrayPool
     */
    public BasicNetwork(HttpStack httpStack) {
        // If a pool isn't passed in, then build a small default pool that will give us a lot of
        // benefit and not use too much memory.
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }


    /**
     * @param httpStack HTTP stack to be used
     * @param pool a buffer pool that improves GC performance in copy operations
     */
    public BasicNetwork(HttpStack httpStack, ByteArrayPool pool) {
        mHttpStack = httpStack;
        mPool = pool;
    }


    /*
     * 执行处理 Volley内的 抽象请求 Request<?>
     * 但是 HttpStack 处理后，都返回 Apache 的请求结果（ HttpResponse ）
     * performRequest(...) 接下来会将：Apache HttpResponse -> Volley NetworkResponse 进行转化
     */
    @Override public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        // 记录下 请求开始时间
        long requestStart = SystemClock.elapsedRealtime();
        // 进入一个 循环体
        while (true) {
            // 用于保存 请求结果（ 响应 ）
            HttpResponse httpResponse = null;
            // 用于保存 请求结果（ 响应 ）的 body
            byte[] responseContents = null;
            // 用于保存 请求结果（ 响应 ）的 Header
            Map<String, String> responseHeaders = Collections.emptyMap();
            try {
                // Gather headers.
                Map<String, String> headers = new HashMap<String, String>();
                /*
                 * 拿出缓存 Response Header 的数据
                 * 放到 headers 内
                 * 用于此次请求
                 */
                addCacheHeaders(headers, request.getCacheEntry());
                // 调用 HttpStack 执行请求，拿到 请求结果（ 响应 ）
                httpResponse = mHttpStack.performRequest(request, headers);
                // 提取 状态行 信息
                StatusLine statusLine = httpResponse.getStatusLine();
                // 拿到 请求结果（ 响应 ）的状态码
                int statusCode = statusLine.getStatusCode();

                // 进行 Apache Header[] -> Map<String, String> 转换
                responseHeaders = convertHeaders(httpResponse.getAllHeaders());
                // Handle cache validation.

                /*
                 * 状态码 304： Not Modified
                 */
                if (statusCode == HttpStatus.SC_NOT_MODIFIED) {

                    // 获取 该请求的缓存 Entry
                    Entry entry = request.getCacheEntry();
                    /*
                     * 没有返回 缓存 Entry
                     *
                     * data 返回 null
                     * header 采用此次请求的 responseHeaders
                     *
                     * 封装成一个 NetworkResponse
                     */
                    if (entry == null) {
                        return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED, null,
                                responseHeaders, true,
                                SystemClock.elapsedRealtime() - requestStart);
                    }

                    // A HTTP 304 response does not have all header fields. We
                    // have to use the header fields from the cache entry plus
                    // the new ones from the response.
                    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5

                    // 将此次请求 的 responseHeaders 放入 缓存 Entry 的 header 内
                    entry.responseHeaders.putAll(responseHeaders);
                    /*
                     * data 返回 缓存内的数据 data
                     * header 为 缓存内的 header
                     *
                     * 封装成一个 NetworkResponse
                     */
                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED, entry.data,
                            entry.responseHeaders, true,
                            SystemClock.elapsedRealtime() - requestStart);
                }

                // Some responses such as 204s do not have content.  We must check.
                /*
                 * 这里处理一些 像 状态码 204： No Content
                 * 判断 Apache HttpResponse 中 的 HttpEntity 是否存在
                 */
                if (httpResponse.getEntity() != null) {
                    /*
                     * 如果 Apache HttpResponse 中 的 HttpEntity 存在
                     * 那么 执行 Apache HttpEntity -> byte[] 的转化
                     */
                    responseContents = entityToBytes(httpResponse.getEntity());
                } else {
                    // Add 0 byte response as a way of honestly representing a
                    // no-content request.
                    /*
                     * 如果 Apache HttpResponse 中 的 HttpEntity 存在
                     * 为了不让 responseContents 不为 null，只能创建一个 byte[0]
                     */
                    responseContents = new byte[0];
                }

                // if the request is slow, log it.

                /*
                 * 拿到 当前的时间，和刚才请求执行前记录的时间
                 * 去计算 这个请求的运行时间 = 当前的时间 - 请求执行前记录的时间
                 */
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                // 判断 请求时长是否在 3000ms 以上。是的话，打印 log
                logSlowRequests(requestLifetime, request, responseContents, statusLine);

                /*
                 * 200-299 用于表示请求成功。
                 */
                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                /*
                 * statusCode：状态码
                 * responseContents：请求结果（ 响应 ） content
                 * responseHeaders：请求结果（ 响应 ） header
                 * notModified：false
                 * SystemClock.elapsedRealtime() - requestStart： 请求时长
                 *
                 * 封装成一个 NetworkResponse，并且返回
                 */
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false,
                        SystemClock.elapsedRealtime() - requestStart);
            } catch (SocketTimeoutException e) {
                // 尝试 重试，这里只是累加 实际超时时间而已
                attemptRetryOnException("socket", request, new TimeoutError());
            } catch (ConnectTimeoutException e) {
                // 尝试 重试，这里只是累加 实际超时时间而已
                attemptRetryOnException("connection", request, new TimeoutError());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {

                int statusCode;
                // 如果 没有 请求结果（ 响应 ），判断为 网络错误
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                } else {
                    throw new NoConnectionError(e);
                }
                VolleyLog.e("Unexpected response code %d for %s", statusCode, request.getUrl());
                NetworkResponse networkResponse;
                // 状态码 204： No Content
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false, SystemClock.elapsedRealtime() - requestStart);
                    // 状态码 401（ Unauthorized ）、 403（ Forbidden ）
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED ||
                            statusCode == HttpStatus.SC_FORBIDDEN) {
                        // 尝试 重试，这里只是累加 实际超时时间而已
                        attemptRetryOnException("auth", request,
                                new AuthFailureError(networkResponse));
                    } else if (statusCode >= 400 && statusCode <= 499) {
                        // Don't retry other client errors.
                        throw new ClientError(networkResponse);
                    } else if (statusCode >= 500 && statusCode <= 599) {
                        if (request.shouldRetryServerErrors()) {
                            // 尝试 重试，这里只是累加 实际超时时间而已
                            attemptRetryOnException("server", request,
                                    new ServerError(networkResponse));
                        } else {
                            throw new ServerError(networkResponse);
                        }
                    } else {
                        // 3xx? No reason to retry.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    // 尝试 重试，这里只是累加 实际超时时间而已
                    attemptRetryOnException("network", request, new NetworkError());
                }
            }
        }
    }


    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    /*
     * 如果
     * 1. Debug log 的开关已经打开
     * 2. 请求时长超过 SLOW_REQUEST_THRESHOLD_MS = 3000ms
     *
     * 打印请求结果（ 响应 ）相关信息：请求时长、Volley 抽象请求 Request、
     *                             请求结果（ 响应 ） content，请求结果（ 响应 ）状态行
     */
    private void logSlowRequests(long requestLifetime, Request<?> request, byte[] responseContents, StatusLine statusLine) {
        if (DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
            VolleyLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " +
                            "[rc=%d], [retryCount=%s]", request, requestLifetime,
                    responseContents != null ? responseContents.length : "null",
                    statusLine.getStatusCode(), request.getRetryPolicy().getCurrentRetryCount());
        }
    }


    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     *
     * @param request The request to use.
     */
    /*
     * 尝试 重试策略 处理
     */
    private static void attemptRetryOnException(String logPrefix, Request<?> request, VolleyError exception)
            throws VolleyError {
        // 拿到 该请求 Request 内对应的 重试策略 类
        RetryPolicy retryPolicy = request.getRetryPolicy();
        // 获取请求此次的超时时间
        int oldTimeout = request.getTimeoutMs();

        try {
            /*
             * 进行 重试：
             * 根据 请求此次的超时时间 * 退避乘数 = 此次的实际超时时间
             * 将 实际超时时间 累加到 重试策略类 中的 超时总时长 内
             */
            retryPolicy.retry(exception);
        } catch (VolleyError e) {
            // 打印 log
            request.addMarker(
                    String.format("%s-timeout-giveup [timeout=%s]", logPrefix, oldTimeout));
            throw e;
        }
        // 打印 log
        request.addMarker(String.format("%s-retry [timeout=%s]", logPrefix, oldTimeout));
    }


    /*
     * 将 Request 内 Entry 保存的
     * Response Header 数据 抽出来
     * 放到 headers Map 里返回，用于此次请求
     */
    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return;
        }

        if (entry.etag != null) {
            // 设置 If-None-Match
            headers.put("If-None-Match", entry.etag);
        }

        if (entry.lastModified > 0) {
            Date refTime = new Date(entry.lastModified);
            // 设置 If-Modified-Since
            headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }

    /*
     * 打印 错误
     */
    protected void logError(String what, String url, long start) {
        long now = SystemClock.elapsedRealtime();
        VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }


    /** Reads the contents of HttpEntity into a byte[]. */
    /*
     * 执行 Apache HttpEntity -> byte[] 的转化
     */
    private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
        /*
         * 实例化一个 PoolingByteArrayOutputStream 对象
         *
         * PoolingByteArrayOutputStream 内设置了一个 ByteArrayPool，会将一些 实例化好的 byte[] 进行缓存
         * 然后回收利用，提供读取流数据
         *
         * 将当前 BasicNetwork 的 ByteArrayPool 提供 给 PoolingByteArrayOutputStream
         * 还需要将 传入一个 需要 output 的数据长度 给 PoolingByteArrayOutputStream，这里用
         * (int) entity.getContentLength() 进行获取
         */
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool,
                (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            // 获取 Apache HttpEntity 的 content 数据流
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            // 获取一个 byte[].length = 1024 的 byte[]
            buffer = mPool.getBuf(1024);
            int count;
            // 开始 边读边写
            while ((count = in.read(buffer)) != -1) {
                // 写入到 PoolingByteArrayOutputStream 中
                bytes.write(buffer, 0, count);
            }
            // 从 PoolingByteArrayOutputStream 中取出 byte[] 数据
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                // 调用 Apache API 关闭 Apache HttpEntity 的 content 数据流
                entity.consumeContent();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                VolleyLog.v("Error occured when calling consumingContent");
            }
            // 将使用的 byte[] 放入 ByteArrayPool 缓存回收
            mPool.returnBuf(buffer);
            // 关闭 PoolingByteArrayOutputStream 流
            bytes.close();
        }
    }


    /**
     * Converts Headers[] to Map<String, String>.
     */
    /*
     * 进行 Apache Header[] -> Map<String, String> 转换
     */
    protected static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
