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

import android.annotation.TargetApi;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing network dispatch from a queue of requests.
 *
 * Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 */

/*
 *  NetworkDispatcher 会将 网络 Request 队列的 Request 逐个抽出
 *  然后进行网络请求后
 *  1. 成功，拿到数据进行解析，然后将 Response 进行硬盘缓存，缓存成 Cache.Entry 的形式，最后
 *     传递 Request 和 Response
 *  2. 失败，失败的话，一般会抛出异常，然后进行 记录请求时长 和 传递错误（ VolleyError ）
 */
public class NetworkDispatcher extends Thread {
    /** The queue of requests to service. */
    /*
     * 保存 网络 Request，因为这里会涉及到 并发
     * 所以，采用 BlockingQueue
     */
    private final BlockingQueue<Request<?>> mQueue;
    /** The network interface for processing requests. */
    /*
     * 用于执行 网络请求 的 Network 接口
     * HttpClientStack 或 HurlStack
     */
    private final Network mNetwork;
    /** The cache to write to. */
     /*
     * 这里的 Cache 其实是一个 DiskBasedCache 缓存
     * 用于将网络请求 回调的 Response 数据进行缓存
     */
    private final Cache mCache;
    /** For posting responses and errors. */
    /*
     * 1. 用于 传递网络请求成功后的 Request 和 Response
     * 2.  用于 传递网络请求失败后的 只有 error 的 Response
     */
    private final ResponseDelivery mDelivery;
    /** Used for telling us to die. */
    // 结束标记，标记这个 NetworkDispatcher 线程是否结束
    private volatile boolean mQuit = false;


    /**
     * Creates a new network dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue Queue of incoming requests for triage
     * @param network Network interface to use for performing requests
     * @param cache Cache interface to use for writing responses to cache
     * @param delivery Delivery interface to use for posting responses
     */
    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
    }


    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    /*
     * 线程结束
     */
    public void quit() {
        // 设置 结束标记
        mQuit = true;
        // 线程中断，run() 内会抛出一个 InterruptedException
        interrupt();
    }


    /*
     * 使用 Android 4.0 以后，DDMS 中的 Network Traffic Tool
     *
     * 这里为 NetworkDispatcher 的打上 Traffic 的 tag
     * 实时地监测网络的使用情况
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addTrafficStatsTag(Request<?> request) {
        // Tag the request (if API >= 14)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            /*
             * 设置 该线程 的 监测网络的使用情况
             * 在 Network Traffic Tool 工具中查到
             */
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
        }
    }


    @Override public void run() {
        // 设置 该线程优先级为 THREAD_PRIORITY_BACKGROUND
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            /*
             * 记录 循环开始时的时间
             * 代指 一个 请求开始时的时间
             */
            long startTimeMs = SystemClock.elapsedRealtime();
            Request<?> request;
            try {
                // Take a request from the queue.
                // 从 网络 Request 队列中拿出一个 Request
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                // 查看 结束标记 是否为 true
                if (mQuit) {
                    // 退出 循环体
                    return;
                }
                // 结束标记 为 false，跳过此次，然后继续循环
                continue;
            }

            try {
                // 为请求添加一个 "cache-queue-take" MarkLog
                request.addMarker("network-queue-take");

                // If the request was cancelled already, do not perform the
                // network request.
                // 如果 Request 已经被取消了
                if (request.isCanceled()) {
                    // 关闭请求，打印 请求中的 MarkLog
                    request.finish("network-discard-cancelled");
                    // 跳过此次，然后继续循环
                    continue;
                }

                // 为 NetworkDispatcher 的打上 Traffic 的 tag
                addTrafficStatsTag(request);

                // Perform the network request.

                /*
                 * 用于执行 网络请求 的 Network 接口
                 * 调用 Network 接口（ HttpClientStack 或 HurlStack ）去请求网络
                 * 但是 HttpStack 处理后，都返回 Apache 的请求结果（ HttpResponse ）
                 * performRequest(...) 接下来会将：Apache HttpResponse -> Volley NetworkResponse 进行转化
                 */
                NetworkResponse networkResponse = mNetwork.performRequest(request);
                // 为请求添加一个 "network-http-complete" MarkLog
                request.addMarker("network-http-complete");

                // If the server returned 304 AND we delivered a response already,
                // we're done -- don't deliver a second identical response.
                /*
                 * 状态码 304： Not Modified 并且 该请求的请求结果 Response （ 响应 ）已经被传递
                 */
                if (networkResponse.notModified && request.hasHadResponseDelivered()) {
                    // 关闭请求，打印 请求中的 MarkLog
                    request.finish("not-modified");
                    // 跳过此次，然后继续循环
                    continue;
                }

                // Parse the response here on the worker thread.
                // 解析 请求结果 Response（ 响应 ）
                Response<?> response = request.parseNetworkResponse(networkResponse);
                // 为请求添加一个 "network-parse-complete" MarkLog
                request.addMarker("network-parse-complete");

                // Write to cache if applicable.
                // TODO: Only update cache metadata instead of entire record for 304s.
                /*
                 * response.cacheEntry：会在 parseNetworkResponse(...) 的时候 执行
                 * Response<T> success(T result, Cache.Entry cacheEntry) 方法 构造一个
                 * Response<T> 对象，并且设置上 cacheEntry
                 *
                 * 所以这里判断了
                 * 1. 请求是否需要缓存
                 * 2. 请求结果 Response（ 响应 ）的 cacheEntry 是否存在
                 */
                if (request.shouldCache() && response.cacheEntry != null) {
                    // 在 DiskBasedCache 上添加缓存，即要缓存到硬盘中
                    mCache.put(request.getCacheKey(), response.cacheEntry);
                    // 为请求添加一个 "network-cache-written" MarkLog
                    request.addMarker("network-cache-written");
                }

                // Post the response back.

                // 修改 传递标识，标识已经被传递了（ 下面就开始传递 ）
                request.markDelivered();
                // 传递 Request 和 Response
                mDelivery.postResponse(request, response);
            } catch (VolleyError volleyError) {
                /*
                 * performRequest(Request<?> request) throws VolleyError
                 * 会抛出一个 VolleyError
                 * 所以这里被理解为 请求网络 的时候发生错误
                 */

                // 设置 请求时长
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                // 解析 并 传递 网络错误
                parseAndDeliverNetworkError(request, volleyError);
            } catch (Exception e) {
                VolleyLog.e(e, "Unhandled exception %s", e.toString());
                // 其他异常的话，也会实例化一个 VolleyError
                VolleyError volleyError = new VolleyError(e);
                // 设置 请求时长
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                // 开始传递 错误
                mDelivery.postError(request, volleyError);
            }
        }
    }

    /*
     * 解析 并 传递 网络错误
     * 会封装成一个 VolleyError
     */
    private void parseAndDeliverNetworkError(Request<?> request, VolleyError error) {
        error = request.parseNetworkError(error);
        mDelivery.postError(request, error);
    }
}
