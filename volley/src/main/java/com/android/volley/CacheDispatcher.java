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

import android.os.Process;
import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing cache triage on a queue of requests.
 *
 * Requests added to the specified cache queue are resolved from cache.
 * Any deliverable response is posted back to the caller via a
 * {@link ResponseDelivery}.  Cache misses and responses that require
 * refresh are enqueued on the specified network queue for processing
 * by a {@link NetworkDispatcher}.
 */

/*
 * 从 缓存 Request 队列中取出 缓存 Request
 * 然后 根据 缓存 Request CacheKey 去硬盘缓存（ DiskBasedCache ）映射过来的内存缓存中寻找 是否存在 Entry （ Response ）
 * 1. 存在的话，通过 DefaultRetryPolicy 回传相关数据
 * 2. 存在但缓存需要刷新的话，放入 网络 Request 队列内，会在 NetworkDispatcher 的循环体中被用来重新请求
 * 3. 不存在的话，将该 Request，放入 网络 Request 队列内，会在 NetworkDispatcher 的循环体中被用来重新请求
 */
public class CacheDispatcher extends Thread {

    // 记录 VolleyLog 的 debug 开关
    private static final boolean DEBUG = VolleyLog.DEBUG;

    /** The queue of requests coming in for triage. */
    /*
     * 保存 缓存 Request，因为这里会涉及到 并发
     * 所以，采用 BlockingQueue
     */
    private final BlockingQueue<Request<?>> mCacheQueue;

    /** The queue of requests going out to the network. */
    /*
     * 保存 网络 Request，因为这里会涉及到 并发
     * 所以，采用 BlockingQueue
     */
    private final BlockingQueue<Request<?>> mNetworkQueue;

    /** The cache to read from. */
    /*
     * 当前 CacheDispatcher 线程 读取 缓存阻塞队列 mCacheQueue 一个 缓存 Request<?> 后
     * 会获取其 的 的 缓存 key = CacheKey
     * 这里的 Cache 其实是一个 DiskBasedCache 缓存
     * 根据缓存 key 从 DiskBasedCache 缓存中获取一个请求
     */
    private final Cache mCache;

    /** For posting responses. */
    // 用于 传递 缓存 Request 中 抽取出 Response
    private final ResponseDelivery mDelivery;

    /** Used for telling us to die. */
    // 结束标记，标记这个 CacheDispatcher 线程是否结束
    private volatile boolean mQuit = false;


    /**
     * Creates a new cache triage dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param cacheQueue Queue of incoming requests for triage
     * @param networkQueue Queue to post requests that require network to
     * @param cache Cache interface to use for resolution
     * @param delivery Delivery interface to use for posting responses
     */
    /*
     * cacheQueue：缓存 Request<?> 队列，RequestQueue 中传入进来的
     * networkQueue：网络 Request<?> 队列，RequestQueue 中传入进来的
     * cache：DiskBasedCache 对象，用于读取硬盘上的缓存数据。Volley -> RequestQueue -> this
     * delivery：ExecutorDelivery 对象，RequestQueue 中传入进来的
     */
    public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue, Cache cache, ResponseDelivery delivery) {
        mCacheQueue = cacheQueue;
        mNetworkQueue = networkQueue;
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


    @Override public void run() {
        if (DEBUG) VolleyLog.v("start new dispatcher");

        // 设置 该线程优先级为 THREAD_PRIORITY_BACKGROUND
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        // Make a blocking call to initialize the cache.
        /*
         * 执行 DiskBasedCache 的初始化操作：
         * 1. 判断缓存目录是否存在，不存在则创建一系列文件夹，然后返回
         * 2. 存在缓存文件，开始读取缓存文件内容。每一个缓存文件内容对应一个 CacheHeader
         */
        mCache.initialize();

        while (true) {
            try {
                // Get a request from the cache triage queue, blocking until
                // at least one is available.

                // 从 缓存 Request 队列内，拿出一个 Request
                final Request<?> request = mCacheQueue.take();
                // 为请求添加一个 "cache-queue-take" MarkLog
                request.addMarker("cache-queue-take");

                // If the request has been canceled, don't bother dispatching it.
                // 如果 Request 已经被取消了
                if (request.isCanceled()) {
                    // 关闭请求，打印 请求中的 MarkLog
                    request.finish("cache-discard-canceled");
                    continue;
                }

                // Attempt to retrieve this item from cache.
                /*
                 * 从 缓存 Request 中，获取缓存 key
                 * 通过缓存 key，去硬盘缓存中，获取对应的 Response（ Entry ）数据
                 */
                Cache.Entry entry = mCache.get(request.getCacheKey());
                // 如果缓存 Entry 不存在
                if (entry == null) {
                    // 为请求添加一个 "cache-miss" MarkLog
                    request.addMarker("cache-miss");
                    // Cache miss; send off to the network dispatcher.
                    /*
                     * 由于 缓存 Response 丢失，所以重新放去 网络 Request 队列内
                     * 重新请求
                     */
                    mNetworkQueue.put(request);
                    continue;
                }

                // If it is completely expired, just send it to the network.
                // 如果缓存 Entry 到了过期时间
                if (entry.isExpired()) {
                    // 为请求添加一个 "cache-hit-expired" MarkLog
                    request.addMarker("cache-hit-expired");
                    // 为 请求 添加 缓存的 Response 数据（ Entry ）
                    request.setCacheEntry(entry);
                    /*
                     * 由于 缓存 Response 过期，所以重新放去 网络 Request 队列内
                     * 重新请求
                     */
                    mNetworkQueue.put(request);
                    continue;
                }

                // We have a cache hit; parse its data for delivery back to the request.
                /*
                 * 为请求添加一个 "cache-hit" MarkLog
                 * 说明 硬盘缓存 中存在 该缓存 Request 对应的 Response 数据 （ Entry ）
                 */
                request.addMarker("cache-hit");

                /*
                 * 开始 拿到 硬盘缓存 中的 Response 数据（ Entry ）
                 * 去解析该 Entry 数据，转换成 Volley 定义的 Response
                 */
                Response<?> response = request.parseNetworkResponse(
                        new NetworkResponse(entry.data, entry.responseHeaders));
                // 为请求添加一个 "cache-hit-parsed" MarkLog
                request.addMarker("cache-hit-parsed");

                // 判断 缓存 Entry 是否需要刷新
                if (!entry.refreshNeeded()) {
                    // Completely unexpired cache hit. Just deliver the response.
                    // 不需要刷新，则直接回传 Request 和 Response 数据
                    mDelivery.postResponse(request, response);
                } else {
                    // Soft-expired cache hit. We can deliver the cached response,
                    // but we need to also send the request to the network for
                    // refreshing.

                    /*********************
                     * 缓存 Entry 需要刷新 *
                     *********************/

                    // 为请求添加一个 "cache-hit-refresh-needed" MarkLog
                    request.addMarker("cache-hit-refresh-needed");
                    // 为 请求 添加 缓存的 Response 数据（ Entry ）
                    request.setCacheEntry(entry);

                    // Mark the response as intermediate.
                    /*
                     * 为了打印一个 intermediate-response 的 MarkLog
                     */
                    response.intermediate = true;

                    // Post the intermediate response back to the user and have
                    // the delivery then forward the request along to the network.

                    /*
                     * 由于 缓存 Entry 需要刷新
                     * 所以在 回传 Request 和 Response 数据 的时候
                     * 执行一个 Runnable 去将该请求
                     * 添加到 网络 Request 队列内
                     */
                    mDelivery.postResponse(request, response, new Runnable() {
                        @Override public void run() {
                            try {
                                mNetworkQueue.put(request);
                            } catch (InterruptedException e) {
                                // Not much we can do about this.
                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.

                /****************************
                 * 调用了 Thread.interrupt() *
                 ****************************/

                // 查看 结束标记 是否为 true
                if (mQuit) {
                    // 退出 循环体
                    return;
                }
                // 结束标记 为 false，继续循环
                continue;
            }
        }
    }
}
