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

import android.os.Handler;
import android.os.Looper;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

/**
 * A synthetic request used for clearing the cache.
 */

/*
 * ClearCacheRequest 用于情况 HTTP 缓存的请求
 *
 * 如果该请求被添加到请求队列（ RequestQueue ）中，由于覆写了 getPriority() 方法
 * 将优先级设置为 Priority.IMMEDIATE （ 立即执行 ）
 */
public class ClearCacheRequest extends Request<Object> {
    private final Cache mCache;
    // 保存 清空缓存后的 回调执行线程
    private final Runnable mCallback;


    /**
     * Creates a synthetic request for clearing the cache.
     *
     * @param cache Cache to clear
     * @param callback Callback to make on the main thread once the cache is clear,
     * or null for none
     */
    /*
     * 默认请求方法为 GET 请求
     */
    public ClearCacheRequest(Cache cache, Runnable callback) {
        super(Method.GET, null, null);
        mCache = cache;
        mCallback = callback;
    }


    @Override public boolean isCanceled() {
        // This is a little bit of a hack, but hey, why not.

        /*
         * 这里进行了 Hack 操作
         * 如果在 网络请求线程中（ NetworkDispatcher ）run() 循环内，执行到 ClearCacheRequest.isCanceled()
         * 会进行缓存清空；然而其他 Request.isCanceled() 不会
         */
        mCache.clear();
        // 存在 清空缓存后的 回调执行线程
        if (mCallback != null) {
            // 实例化一个主（ UI ）线程的 Handler
            Handler handler = new Handler(Looper.getMainLooper());
            // 放在主（ UI ）线程的消息队列（ MessageQueue ）立即执行，因为没有设置时间间隔
            handler.postAtFrontOfQueue(mCallback);
        }
        return true;
    }


    /*
     * 覆写该请求优先级方法
     * 设置优先级 = Priority.IMMEDIATE（ 立即执行 ）
     */
    @Override public Priority getPriority() {
        return Priority.IMMEDIATE;
    }


    /*
     * 被强制覆写的抽象方法，由于该请求用于清空缓存
     * 不处理，网络请求
     * 所以，没有指定，请求结果的解析步骤
     */
    @Override protected Response<Object> parseNetworkResponse(NetworkResponse response) {
        return null;
    }


    /*
     * 被强制覆写的抽象方法，由于该请求用于清空缓存
     * 不处理，网络请求
     * 所欲，没有可以传递的 请求结果解析数据
     */
    @Override protected void deliverResponse(Object response) {
    }
}
