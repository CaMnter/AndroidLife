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

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import java.util.Map;

/**
 * Utility methods for parsing HTTP headers.
 */
/*
 * Http header 的解析工具类
 *
 * 主要作用：
 * 解析 Header，判断返回结果是否需要缓存。需要缓存的话，返回 Header 中相关信息
 */
public class HttpHeaderParser {

    /**
     * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
     *
     * @param response The network response to parse headers from
     * @return a cache entry for the given response, or null if the response is not cacheable.
     */
    /*
     * 从网络请求回来的请求结果 NetworkResponse 的 Header 中提取出一个用于缓存的 Cache.Entry
     */
    public static Cache.Entry parseCacheHeaders(NetworkResponse response) {
        // 记录开始解析时的时间
        long now = System.currentTimeMillis();

        // 拿到网络请求结果的 Header 数据
        Map<String, String> headers = response.headers;

        // 准备好 Cache.Entry 对象需要的数据 Part-1
        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;

        // 准备好 Cache.Entry 对象需要的数据 Part-2
        String serverEtag = null;
        String headerValue;

        // 获取 网络请求结果的 Header "Date" 数据
        headerValue = headers.get("Date");
        if (headerValue != null) {
            // 记录服务器时间
            serverDate = parseDateAsEpoch(headerValue);
        }

        // 获取 网络请求结果的 Header "Cache-Control" 数据
        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            // 标记有 "Cache-Control" 数据
            hasCacheControl = true;
            // 拆分 "Cache-Control" 数据
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                /*
                 * 如果 "Cache-Control" 数据内有
                 * "no-cache" 或 "no-store" -> 直接返回 null，拿不到 Cache.Entry，不缓存
                 */
                if (token.equals("no-cache") || token.equals("no-store")) {
                    return null;
                } else if (token.startsWith("max-age=")) {
                    // 如果 "Cache-Control" 数据内有 "max-age=" 开头的字段
                    try {
                        // 记录最大时间
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.startsWith("stale-while-revalidate=")) {
                    // 如果 "Cache-Control" 数据内有 "stale-while-revalidate=" 开头的字段
                    try {
                        // 记录重新验证时间
                        staleWhileRevalidate = Long.parseLong(token.substring(23));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    /*
                     * 如果 "Cache-Control" 数据内有
                     * "must-revalidate" 或 "proxy-revalidate" 开头的字段
                     * 标记 mustRevalidate = true，必须验证
                     */
                    mustRevalidate = true;
                }
            }
        }

        // 获取 网络请求结果的 Header "Expires" 数据
        headerValue = headers.get("Expires");
        if (headerValue != null) {
            // 记录服务器到期时间
            serverExpires = parseDateAsEpoch(headerValue);
        }
        // 获取 网络请求结果的 Header "Last-Modified" 数据
        headerValue = headers.get("Last-Modified");
        if (headerValue != null) {
            // 记录最后修改时间
            lastModified = parseDateAsEpoch(headerValue);
        }

        // 记录 "ETag" 数据
        serverEtag = headers.get("ETag");

        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
        // is more restrictive.
        /*
         * 如果判断有 "Cache-Control" 数据
         *
         * 计算过期时间，"Cache－Control" 优先于 "Expires"
         */
        if (hasCacheControl) {
            // 记录刷新时间
            softExpire = now + maxAge * 1000;
            // 记录过期时间
            finalExpire = mustRevalidate
                    ? softExpire
                    : softExpire + staleWhileRevalidate * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            // Default semantic for Expire header in HTTP specification is softExpire.
            // 默认语义头在 HTTP 规范 softExpire 到期。
            /*
             * 不存在 "Cache-Control" 数据
             * 另外一套计算 刷新时间 和 过期时间 的逻辑
             */
            softExpire = now + (serverExpires - serverDate);
            finalExpire = softExpire;
        }

        // 封装一个缓存用的 Cache.Entry 数据
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = finalExpire;
        entry.serverDate = serverDate;
        entry.lastModified = lastModified;
        entry.responseHeaders = headers;

        return entry;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    /*
     * 解析时间，将 RFC1123 的时间格式，解析成 epoch 时间
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            // 耦合了 Apache 的时间工具类
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    /**
     * Retrieve a charset from headers
     *
     * @param headers An {@link java.util.Map} of headers
     * @param defaultCharset Charset to return if none can be found
     * @return Returns the charset specified in the Content-Type of this header,
     * or the defaultCharset if none can be found.
     */
    /*
     * 解析编码集，在 Content-Type 中获取编码集（ charset ），如果没有找到，默认返回 defaultCharset 编码
     */
    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return defaultCharset;
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    /*
     * 解析编码集，在 Content-Type 中获取编码集（ charset ），如果没有找到，默认返回 ISO-8859-1 编码
     */
    public static String parseCharset(Map<String, String> headers) {
        return parseCharset(headers, HTTP.DEFAULT_CONTENT_CHARSET);
    }
}
