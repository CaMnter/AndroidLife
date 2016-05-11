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

import java.util.Collections;
import java.util.Map;

/**
 * An interface for a cache keyed by a String with a byte array as data.
 */

/*
 * 缓存的接口
 * 提供一些接口方法需要是钱
 * 全 Volley 中主要实现了一个 DiskBasedCache
 */
public interface Cache {
    /**
     * Retrieves an entry from the cache.
     *
     * @param key Cache key
     * @return An {@link Entry} or null in the event of a cache miss
     */
    // 获取 缓存的 Response 数据
    public Entry get(String key);

    /**
     * Adds or replaces an entry to the cache.
     *
     * @param key Cache key
     * @param entry Data to store and metadata for cache coherency, TTL, etc.
     */
    // 存放 需要缓存的 Response 数据
    public void put(String key, Entry entry);

    /**
     * Performs any potentially long-running actions needed to initialize the cache;
     * will be called from a worker thread.
     */
    /*
     * 初始化缓存操作
     * 实现该接口的缓存，初始化操作时都需要做一些事情
     * 比如 DiskBasedCache.initialize() 初始化时，对文件目录以及文件进行了创建和写入
     */
    public void initialize();

    /**
     * Invalidates an entry in the cache.
     *
     * @param key Cache key
     * @param fullExpire True to fully expire the entry, false to soft expire
     */
    /*
     * 放一条缓存数据 无效
     * 默认情况是 把 过期时间=0
     * fullExpire=true 的话 刷新时间=0 要刷新
     * fullExpire=false 的话 刷新时间=不变 需要判断是否小于当前时间 小于则刷新
     */
    public void invalidate(String key, boolean fullExpire);

    /**
     * Removes an entry from the cache.
     *
     * @param key Cache key
     */
    // 移除某条缓存
    public void remove(String key);

    /**
     * Empties the cache.
     */
    // 清空整个缓存
    public void clear();

    /**
     * Data and metadata for an entry returned by the cache.
     */

    /*
     * 用于缓存 Response 数据
     */
    public static class Entry {
        /** The data returned from cache. */
        // 缓存 Response contents
        public byte[] data;

        /** ETag for cache coherency. */
        // 缓存 Response Header "ETag"
        public String etag;

        /** Date of this response as reported by the server. */
        // 缓存 Response Header "Date"
        public long serverDate;

        /** The last modified date for the requested object. */
        // 缓存 Response Header Last-Modified
        public long lastModified;

        /** TTL for this record. */
        // 缓存 计算好的 过期时间
        public long ttl;

        /** Soft TTL for this record. */
        // 缓存 计算好的 刷新时间
        public long softTtl;

        /** Immutable response headers as received from server; must be non-null. */
        // 缓存 Response 对应的 Request Header 数据
        public Map<String, String> responseHeaders = Collections.emptyMap();


        /** True if the entry is expired. */
        // 判断是否过期
        public boolean isExpired() {
            return this.ttl < System.currentTimeMillis();
        }


        /** True if a refresh is needed from the original data source. */
        // 判断是否需要刷新
        public boolean refreshNeeded() {
            /*
             * 由此可知
             * softTtl 是一个 刷新时间
             * 如果小于了当前当前时间 就刷新
             */
            return this.softTtl < System.currentTimeMillis();
        }
    }
}
