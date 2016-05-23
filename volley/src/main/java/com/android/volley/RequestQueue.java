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

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A request dispatch queue with a thread pool of dispatchers.
 *
 * Calling {@link #add(Request)} will enqueue the given Request for dispatch,
 * resolving from either cache or network on a worker thread, and then delivering
 * a parsed response on the main thread.
 */

/*
 * RequestQueue 被定义为请求队列
 * 用于操作 缓存请求执行线程（ CacheDispatcher ）和 网络请求执行线程（ NetworkDispatcher ）
 *
 * 默认的情况下：
 * 启动 1 个 缓存请求执行线程（ CacheDispatcher ）
 * 启动 4 个 网络请求执行线程（ NetworkDispatcher ），根据 DEFAULT_NETWORK_THREAD_POOL_SIZE 的值
 *
 * 这些线程共享 缓存请求队列 （ mCacheQueue ）和 网络请求队列（ mNetworkQueue ）
 */
public class RequestQueue {

    /** Callback interface for completed requests. */
    /*
     * 提供一个请求完成接口，当一个请求完成后，会执行此回调
     * 会在 RequestQueue.finish(Request<T> request)
     */
    public static interface RequestFinishedListener<T> {
        /** Called when a request has finished processing. */
        /*
         * 回调 已经完成的 请求
         * 缓存请求执行线程（ CacheDispatcher ）和 网络请求执行线程（ NetworkDispatcher ）
         * 完成的每个请求后，都会调用 Request.finish(...)，在 Request.finish(...) 中，会调用
         * 请求队列 RequestQueue.finish(...)。最后，再调用该接口的 onRequestFinished(...)
         * 将请求回调出去
         */
        public void onRequestFinished(Request<T> request);
    }

    /** Used for generating monotonically-increasing sequence numbers for requests. */
    /*
     * 为 一个 Request 分配一个 队列序号
     * 用于管理 Request 在队列中的顺序
     * 采用原子类，是因为会涉及到 并发（ 多线程 ）
     */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    /**
     * Staging area for requests that already have a duplicate request in flight.
     *
     * <ul>
     * <li>containsKey(cacheKey) indicates that there is a request in flight for the given cache
     * key.</li>
     * <li>get(cacheKey) returns waiting requests for the given cache key. The in flight request
     * is <em>not</em> contained in that list. Is null if no requests are staged.</li>
     * </ul>
     */
    /*
     * 正在等待的请求集合
     */
    private final Map<String, Queue<Request<?>>> mWaitingRequests
            = new HashMap<String, Queue<Request<?>>>();

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    /*
     * 当前正在请求队列
     */
    private final Set<Request<?>> mCurrentRequests = new HashSet<Request<?>>();

    /** The cache triage queue. */
    /*
     * 缓存请求队列
     */
    private final PriorityBlockingQueue<Request<?>> mCacheQueue
            = new PriorityBlockingQueue<Request<?>>();

    /** The queue of requests that are actually going out to the network. */
    /*
     * 网络请求队列
     */
    private final PriorityBlockingQueue<Request<?>> mNetworkQueue
            = new PriorityBlockingQueue<Request<?>>();

    /** Number of network request dispatcher threads to start. */
    /*
     * 最大 网络请求执行线程（ NetworkDispatcher ） 的个数
     */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /** Cache interface for retrieving and storing responses. */
    /*
     * 缓存数据
     * 在 Volley 内的实现目前只有 DiskBasedCache （ Request 的硬盘缓存 ）
     */
    private final Cache mCache;

    /** Network interface for performing requests. */
    /*
     * Network 接口 用于执行 网路请求
     * Volley 内的话，实现类只有 BasicNetwork
     * BasicNetwork 会调用 HttpStack 的子类（ HurlStack, HttpClientStack ）去完成网络请求
     */
    private final Network mNetwork;

    /** Response delivery mechanism. */
    /*
     * 负责将 网络请求执行线程（ NetworkDispatcher ） 和 缓存请求执行线程（ CacheDispatcher ）
     * 内发生的，两种互斥情况：
     * 1. 网络错误 （ 只有 VolleyError 的 Response ）
     * 2. 网络请求结果（ 带有 解析结果的 Response ） + 网络请求（ 设置好 Cache 的 Request ）
     *    + 回调执行线程（ Runnable ）
     */
    private final ResponseDelivery mDelivery;

    /** The network dispatchers. */
    /*
     * 网络请求执行线程（ NetworkDispatcher ）组
     */
    private NetworkDispatcher[] mDispatchers;

    /** The cache dispatcher. */
    /*
     * 缓存请求执行线程（ CacheDispatcher ）
     */
    private CacheDispatcher mCacheDispatcher;

    /*
     * RequestFinishedListener 回调接口 集合
     */
    private List<RequestFinishedListener> mFinishedListeners
            = new ArrayList<RequestFinishedListener>();


    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache A Cache to use for persisting responses to disk
     * @param network A Network interface for performing HTTP requests
     * @param threadPoolSize Number of network dispatcher threads to create
     * @param delivery A ResponseDelivery interface for posting responses and errors
     */

    /*
     * cache：硬盘缓存，DiskBasedCache
     * network：Network 接口 用于执行 网路请求
     * threadPoolSize：最大 网络请求执行线程（ NetworkDispatcher ） 的个数
     * delivery：一般 Volley 中只有 ExecutorDelivery，实现了传递逻辑
     */
    public RequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery) {
        mCache = cache;
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery;
    }


    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache A Cache to use for persisting responses to disk
     * @param network A Network interface for performing HTTP requests
     * @param threadPoolSize Number of network dispatcher threads to create
     */
    /*
     * 默认 delivery：主线程的 new ExecutorDelivery(new Handler(Looper.getMainLooper())
     */
    public RequestQueue(Cache cache, Network network, int threadPoolSize) {
        this(cache, network, threadPoolSize,
                new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }


    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache A Cache to use for persisting responses to disk
     * @param network A Network interface for performing HTTP requests
     */
    /*
     * 默认 threadPoolSize：DEFAULT_NETWORK_THREAD_POOL_SIZE = 4
     */
    public RequestQueue(Cache cache, Network network) {
        this(cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
    }


    /**
     * Starts the dispatchers in this queue.
     */
    public void start() {
        /*
         * 停止 RequestQueue 内 的所有线程（ 如果有的话 ）
         * 因为 底下要重新创建新的 缓存请求执行线程（ CacheDispatcher ）和 网络请求执行线程（ NetworkDispatcher ）
         */
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create the cache dispatcher and start it.

        /*
         * 创建 1 个 缓存请求执行线程（ CacheDispatcher ）
         * 共享了数据：
         * 1. RequestQueue 的 缓存请求队列 mCacheQueue（ PriorityBlockingQueue<Request<?>> ）
         * 2. RequestQueue 的 网络请求队列 mNetworkQueue（ PriorityBlockingQueue<Request<?>> ）
         * 3. RequestQueue 的 硬盘缓存 mCache（ DiskBasedCache ）
         * 4. RequestQueue 的 数据传递接口 mDelivery（ ExecutorDelivery ）
         *
         * 然后开始 启动这个 缓存请求执行线程
         */
        mCacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache, mDelivery);
        mCacheDispatcher.start();

        // Create network dispatchers (and corresponding threads) up to the pool size.
        /*
         * 创建 N 个 网络请求执行线程（ NetworkDispatcher ）
         * 共享了数据：
         * 1. RequestQueue 的 网络请求队列 mNetworkQueue（ PriorityBlockingQueue<Request<?>> ）
         * 2. RequestQueue 的 网络请求接口 mNetwork（ BasicNetwork ）
         * 3. RequestQueue 的 硬盘缓存 mCache（ DiskBasedCache ）
         * 4. RequestQueue 的 数据传递接口 mDelivery（ ExecutorDelivery ）
         *
         * 然后开始 启动每一个 网络请求执行线程
         */
        for (int i = 0; i < mDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(mNetworkQueue, mNetwork,
                    mCache, mDelivery);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }


    /**
     * Stops the cache and network dispatchers.
     */

    /*
     * 停止 RequestQueue 内的所有 线程
     * 1 个 CacheDispatcher 线程
     * N 个 NetworkDispatcher 线程
     */
    public void stop() {
        if (mCacheDispatcher != null) {
            mCacheDispatcher.quit();
        }
        for (int i = 0; i < mDispatchers.length; i++) {
            if (mDispatchers[i] != null) {
                mDispatchers[i].quit();
            }
        }
    }


    /**
     * Gets a sequence number.
     */
    /*
     * 获取一个 队列序号
     * 用于 为 一个 Request 分配 队列序号
     * 便于 Request 在请求中的排序
     */
    public int getSequenceNumber() {
        // 原子类 AtomicInteger 实现 ++i
        return mSequenceGenerator.incrementAndGet();
    }


    /**
     * Gets the {@link Cache} instance being used.
     */
    /*
     * 获取 缓存
     * 但 Volley 对于 缓存的实现 只有 DiskBasedCache
     */
    public Cache getCache() {
        return mCache;
    }


    /**
     * A simple predicate or filter interface for Requests, for use by
     * {@link RequestQueue#cancelAll(RequestFilter)}.
     */
    /*
     * 提供一个 Request 的过滤接口
     * 目前，在 RequestQueue.cancelAll(RequestFilter) 内的使用
     * 是 根据 Request 的 tag 信息 是否匹配 过滤出 相同要取消的请求的 Request
     * 因为，这个 tag 标识的定义是用于：Request 的批量取消操作
     */
    public interface RequestFilter {
        public boolean apply(Request<?> request);
    }


    /**
     * Cancels all requests in this queue for which the given filter applies.
     *
     * @param filter The filtering function to use
     */
    /*
     * 根据 给定的 RequestFilter 过滤逻辑
     * 去批量取消 当前正在请求队列 中的 Request
     */
    public void cancelAll(RequestFilter filter) {
        synchronized (mCurrentRequests) {
            for (Request<?> request : mCurrentRequests) {
                if (filter.apply(request)) {
                    request.cancel();
                }
            }
        }
    }


    /**
     * Cancels all requests in this queue with the given tag. Tag must be non-null
     * and equality is by identity.
     */
    /*
     * 根据 tag
     * 去过滤出 当前正在请求队列 中与这个 tag 匹配的 Request
     * 找出，然后取消，完成批量取消功能
     */
    public void cancelAll(final Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        }
        cancelAll(new RequestFilter() {
            @Override public boolean apply(Request<?> request) {
                return request.getTag() == tag;
            }
        });
    }


    /**
     * Adds a Request to the dispatch queue.
     *
     * @param request The request to service
     * @return The passed-in request
     */
    public <T> Request<T> add(Request<T> request) {
        // Tag the request as belonging to this queue and add it to the set of current requests.

        // 为该 Request 添加 请求队列（ RequestQueue ）
        request.setRequestQueue(this);

        // 锁上 mCurrentRequests 数据
        synchronized (mCurrentRequests) {
            // 将该 Request 添加到 当前正在请求队列 内
            mCurrentRequests.add(request);
        }

        // Process requests in the order they are added.
        /*
         *  为该 Request 添加 队列序号
         */
        request.setSequence(getSequenceNumber());
        // 为该 Request 添加一个 "cache-queue-take" MarkLog
        request.addMarker("add-to-queue");

        // If the request is uncacheable, skip the cache queue and go straight to the network.
        /*
         * 根据 Request.mShouldCache 标识
         * 去判断该 Request 是否需要缓存
         */
        if (!request.shouldCache()) {
            // 不需要缓存的话，添加在 网络请求队列 内
            mNetworkQueue.add(request);
            // 并返回此次添加的 Request
            return request;
        }

        // Insert request into stage if there's already a request with the same cache key in flight.
        // 锁上 mWaitingRequests 数据
        synchronized (mWaitingRequests) {
            // 获取该 Request 的 cacheKey
            String cacheKey = request.getCacheKey();
            // 判断 正在等待的请求集合 内 是否存在数据
            if (mWaitingRequests.containsKey(cacheKey)) {
                // There is already a request in flight. Queue up.
                // 拿出相同 cacheKey 的请求队列
                Queue<Request<?>> stagedRequests = mWaitingRequests.get(cacheKey);
                // 如果队列 为 null
                if (stagedRequests == null) {
                    // 创建一个 队列
                    stagedRequests = new LinkedList<Request<?>>();
                }
                // 将该 Request 添加到队列内
                stagedRequests.add(request);
                // 将 cacheKey 和 对应的 请求队列 放入 Map 内
                mWaitingRequests.put(cacheKey, stagedRequests);
                if (VolleyLog.DEBUG) {
                    VolleyLog.v("Request for cacheKey=%s is in flight, putting on hold.", cacheKey);
                }
            } else {
                // Insert 'null' queue for this cacheKey, indicating there is now a request in
                // flight.
                // 将 cacheKey 放入 Map 内
                mWaitingRequests.put(cacheKey, null);
                // 同时，将该 Request 放入 缓存请求执行线程（ CacheDispatcher ）
                mCacheQueue.add(request);
            }
            return request;
        }
    }


    /**
     * Called from {@link Request#finish(String)}, indicating that processing of the given request
     * has finished.
     *
     * <p>Releases waiting requests for <code>request.getCacheKey()</code> if
     * <code>request.shouldCache()</code>.</p>
     */
    <T> void finish(Request<T> request) {
        // Remove from the set of requests currently being processed.
        // 锁上 mCurrentRequests 数据
        synchronized (mCurrentRequests) {
            // 在 当前正在请求队列 中移除 该 Request
            mCurrentRequests.remove(request);
        }
        // 锁上 mFinishedListeners 数据
        synchronized (mFinishedListeners) {
            /*
             * 拿出 所有 的 RequestFinishedListener
             * 逐个调用 onRequestFinished(...) 回传 该 Request
             */
            for (RequestFinishedListener<T> listener : mFinishedListeners) {
                listener.onRequestFinished(request);
            }
        }

        /*
         * 根据 Request.mShouldCache 标识
         * 去判断该 Request 是否需要缓存
         */
        if (request.shouldCache()) {
            /*******************
             * Request 需要缓存 *
             *******************/

            // 锁上 mWaitingRequests 数据
            synchronized (mWaitingRequests) {
                // 获取该 Request 的 cacheKey
                String cacheKey = request.getCacheKey();
                // 从 正在等待的请求集合 内 删除 该 cacheKey 对应的 请求队列
                Queue<Request<?>> waitingRequests = mWaitingRequests.remove(cacheKey);
                if (waitingRequests != null) {
                    if (VolleyLog.DEBUG) {
                        VolleyLog.v("Releasing %d waiting requests for cacheKey=%s.",
                                waitingRequests.size(), cacheKey);
                    }
                    // Process all queued up requests. They won't be considered as in flight, but
                    // that's not a problem as the cache has been primed by 'request'.
                    /*
                     * 由于上面 判断了 需要缓存该 Request
                     * 所以，将 该 cacheKey 对应的 队列数据
                     * 添加到 缓存请求队列
                     * 然后 缓存请求执行线程（ CacheDispatcher ）会去处理 缓存请求队列 的数据
                     * 进行请求的硬盘缓存
                     */
                    mCacheQueue.addAll(waitingRequests);
                }
            }
        }
    }


    /*
     * 添加一个 RequestFinishedListener 回调
     */
    public <T> void addRequestFinishedListener(RequestFinishedListener<T> listener) {
        synchronized (mFinishedListeners) {
            mFinishedListeners.add(listener);
        }
    }


    /**
     * Remove a RequestFinishedListener. Has no effect if listener was not previously added.
     */
    /*
     * 移除一个 RequestFinishedListener 回调
     */
    public <T> void removeRequestFinishedListener(RequestFinishedListener<T> listener) {
        synchronized (mFinishedListeners) {
            mFinishedListeners.remove(listener);
        }
    }
}
