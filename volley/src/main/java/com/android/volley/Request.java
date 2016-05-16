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

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.android.volley.VolleyLog.MarkerLog;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * Base class for all network requests.
 *
 * @param <T> The type of parsed response this request expects.
 */

/*
 * Volley 内所有抽象请求的 基类
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    /**
     * Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}.
     */
    // POST 请求或者 PUT 请求的默认编码 UTF-8
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /**
     * Supported request methods.
     */
    /*
     * 定义一个 Method 接口
     * 用于标识该请求的 请求类型 （方法类型）
     */
    public interface Method {
        // GET 请求 or  POST 请求
        int DEPRECATED_GET_OR_POST = -1;
        // GET 请求
        int GET = 0;
        // POST 请求
        int POST = 1;
        // PUT 请求
        int PUT = 2;
        // DELETE 请求
        int DELETE = 3;
        // HEAD 请求
        int HEAD = 4;
        // OPTIONS 请求
        int OPTIONS = 5;
        // TRACE 请求
        int TRACE = 6;
        // PATCH 请求
        int PATCH = 7;
    }

    /** An event log tracing the lifetime of this request; for debugging. */
    // 实例化一个 MarkerLog 对象，用于 debug log
    private final MarkerLog mEventLog = MarkerLog.ENABLED ? new MarkerLog() : null;

    /**
     * Request method of this request.  Currently supports GET, POST, PUT, DELETE, HEAD, OPTIONS,
     * TRACE, and PATCH.
     */
    // 方法类型，Method 内的内容
    private final int mMethod;

    /** URL of this request. */
    // 请求的 String 类型 url
    private final String mUrl;

    /** Default tag for {@link TrafficStats}. */
    // 保存一个 Url host 的 hashcode
    private final int mDefaultTrafficStatsTag;

    /** Listener interface for errors. */
    // 实例化一个 请求结果 Response 发生错误的回调接口
    private final Response.ErrorListener mErrorListener;

    /** Sequence number of this request, used to enforce FIFO ordering. */
    // 标识该请求的序号，用于维持 FIFO （ 先进先出 ）队列中的顺序
    private Integer mSequence;

    /** The request queue this request is associated with. */
    // 保存 该 请求所在的 请求队列 （ RequestQueue ） 的引用
    private RequestQueue mRequestQueue;

    /** Whether or not responses to this request should be cached. */
    // 标记该请求是否 需要缓存，默认要缓存
    private boolean mShouldCache = true;

    /** Whether or not this request has been canceled. */
    // 标记该请求是否 被取消了，默认不取消
    private boolean mCanceled = false;

    /** Whether or not a response has been delivered for this request yet. */
    // 标记请求的 请求结果（ Response ） 是否已经被传递了，默认还没被传递
    private boolean mResponseDelivered = false;

    /** Whether the request should be retried in the event of an HTTP 5xx (server) error. */
    // 标记该请求发生 5xx 的服务端错误时，是否要重试，默认不重试
    private boolean mShouldRetryServerErrors = false;

    /** The retry policy for this request. */
    /*
     * 重试策略 类
     * 一般，在 Volley 中，只有 DefaultRetryPolicy
     */
    private RetryPolicy mRetryPolicy;

    /**
     * When a request can be retrieved from cache but must be refreshed from
     * the network, the cache entry will be stored here so that in the event of
     * a "Not Modified" response, we can be sure it hasn't been evicted from cache.
     */
    // 保存 从缓存中 取出来的 请求结果 数据
    private Cache.Entry mCacheEntry = null;

    /** An opaque token tagging this request; used for bulk cancellation. */
    // 一个标记，用于处理批量取消
    private Object mTag;


    /**
     * Creates a new request with the given URL and error listener.  Note that
     * the normal response listener is not provided here as delivery of responses
     * is provided by subclasses, who have a better idea of how to deliver an
     * already-parsed response.
     *
     * @deprecated Use {@link #Request(int, String, com.android.volley.Response.ErrorListener)}.
     */
    /*
     * 默认请求方法：DEPRECATED_GET_OR_POST -> GET 方法或者 POST 方法
     * 但是， 该构造方法被废弃
     */
    @Deprecated public Request(String url, Response.ErrorListener listener) {
        this(Method.DEPRECATED_GET_OR_POST, url, listener);
    }


    /**
     * Creates a new request with the given method (one of the values from {@link Method}),
     * URL, and error listener.  Note that the normal response listener is not provided here as
     * delivery of responses is provided by subclasses, who have a better idea of how to deliver
     * an already-parsed response.
     */
    public Request(int method, String url, Response.ErrorListener listener) {
        mMethod = method;
        mUrl = url;
        mErrorListener = listener;
        // 重试策略 类 设置为：一个 DefaultRetryPolicy
        setRetryPolicy(new DefaultRetryPolicy());

        // 设置 Url host 的 hashcode
        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }


    /**
     * Return the method for this request.  Can be one of the values in {@link Method}.
     */
    /*
     * 获取该请求的 方法类型
     */
    public int getMethod() {
        return mMethod;
    }


    /**
     * Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link RequestQueue#cancelAll(Object)}.
     *
     * @return This Request object to allow for chaining.
     */

    /*
     * 设置 mTag 标记，用于批处理取消
     * 在 RequestQueue.cancelAll(Object)
     */
    public Request<?> setTag(Object tag) {
        mTag = tag;
        return this;
    }


    /**
     * Returns this request's tag.
     *
     * @see Request#setTag(Object)
     */
    /*
     * 获取 mTag 标记，用于批处理取消
     */
    public Object getTag() {
        return mTag;
    }


    /**
     * @return this request's {@link com.android.volley.Response.ErrorListener}.
     */
    /*
     * 获取 请求结果 Response 发生错误的回调接口
     */
    public Response.ErrorListener getErrorListener() {
        return mErrorListener;
    }


    /**
     * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
     */
    /*
     * 返回 保存好的 Url host 的 hashcode
     */
    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }


    /**
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    /*
     * 获取一个 Url host 的 hashcode
     * 没有的话 返回 0
     */
    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            // 实例化一个 Uri
            Uri uri = Uri.parse(url);
            if (uri != null) {
                // 获取 host
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }


    /**
     * Sets the retry policy for this request.
     *
     * @return This Request object to allow for chaining.
     */
    /*
     * 设置 重试策略 类
     */
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        mRetryPolicy = retryPolicy;
        return this;
    }


    /**
     * Adds an event to this request's event log; for debugging.
     */
    /*
     * 给 MarkerLog 添加一个 Marker （ Log ）
     * 到时候调用 MarkerLog.finish(...) 会一起打印出来
     */
    public void addMarker(String tag) {
        if (MarkerLog.ENABLED) {
            mEventLog.add(tag, Thread.currentThread().getId());
        }
    }


    /**
     * Notifies the request queue that this request has finished (successfully or with error).
     *
     * <p>Also dumps all events from this request's event log; for debugging.</p>
     */

    /*
     * 通知 这个请求 Request 所在的请求队列（ RequestQueue ）请求已经结束了
     */
    void finish(final String tag) {
        // 判断 请求队列 （ RequestQueue ） 是否 为 null
        if (mRequestQueue != null) {
            // 调用 RequestQueue.finish(Request<T> request)
            mRequestQueue.finish(this);
        }
        // 判断 MarkerLog ( VolleyLog ) 的 开关 是否打开
        if (MarkerLog.ENABLED) {
            final long threadId = Thread.currentThread().getId();
            // 如果不在 主（ UI ） 线程 中
            if (Looper.myLooper() != Looper.getMainLooper()) {
                // If we finish marking off of the main thread, we need to
                // actually do it on the main thread to ensure correct ordering.

                // 实例化一个 主（ UI ） 线程的 Handler
                Handler mainThread = new Handler(Looper.getMainLooper());

                /*
                 * 调用刚才实例化的 主（ UI ） 线程的 Handler
                 * 在 主（ UI ） 线程 中打 log
                 */
                mainThread.post(new Runnable() {
                    @Override public void run() {
                        mEventLog.add(tag, threadId);
                        mEventLog.finish(this.toString());
                    }
                });
                // 然后 返回
                return;
            }

            /*
             * 如果上面没有返回，直接到这了，证明是 主（ UI ） 线程 中
             * 直接 打 log
             */
            mEventLog.add(tag, threadId);
            mEventLog.finish(this.toString());
        }
    }


    /**
     * Associates this request with the given queue. The request queue will be notified when this
     * request has finished.
     *
     * @return This Request object to allow for chaining.
     */
    /*
     * 设置处理该请求 Request 的请求队列（ RequestQueue ）
     */
    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }


    /**
     * Sets the sequence number of this request.  Used by {@link RequestQueue}.
     *
     * @return This Request object to allow for chaining.
     */

    /*
     * 设置 该请求的序号，用于维持 FIFO （ 先进先出 ）队列中的顺序
     */
    public final Request<?> setSequence(int sequence) {
        mSequence = sequence;
        return this;
    }


    /**
     * Returns the sequence number of this request.
     */
    /*
     * 获取 该请求的序号，用于维持 FIFO （ 先进先出 ）队列中的顺序
     */
    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }


    /**
     * Returns the URL of this request.
     */
    /*
     * 获取 该请求 Request 的 Url
     */
    public String getUrl() {
        return mUrl;
    }


    /**
     * Returns the cache key for this request.  By default, this is the URL.
     */
    /*
     * 获取请求
     */
    public String getCacheKey() {
        return getUrl();
    }


    /**
     * Annotates this request with an entry retrieved for it from cache.
     * Used for cache coherency support.
     *
     * @return This Request object to allow for chaining.
     */
    /*
     * 设置 缓存内容 Cache.Entry
     */
    public Request<?> setCacheEntry(Cache.Entry entry) {
        mCacheEntry = entry;
        return this;
    }


    /**
     * Returns the annotated cache entry, or null if there isn't one.
     */
    /*
     * 返回 缓存内容 Cache.Entry
     */
    public Cache.Entry getCacheEntry() {
        return mCacheEntry;
    }


    /**
     * Mark this request as canceled.  No callback will be delivered.
     */
    /*
     * 取消该请求 Request
     * 即，设置 mCanceled 标识 为 true
     */
    public void cancel() {
        mCanceled = true;
    }


    /**
     * Returns true if this request has been canceled.
     */
    /*
     * 查看该请求 Request 是否取消
     */
    public boolean isCanceled() {
        return mCanceled;
    }


    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */

    /*
     * 获取 HTTP 额外的头信息
     * 这里，默认返回一个 EmptyMap
     *
     * 注意：如果有需要的话，需要覆写该方法，为这个请求 Request 提供 额外的 HTTP 头信息
     */
    public Map<String, String> getHeaders() throws AuthFailureError {
        return Collections.emptyMap();
    }


    /**
     * Returns a Map of POST parameters to be used for this request, or null if
     * a simple GET should be used.  Can throw {@link AuthFailureError} as
     * authentication may be required to provide these values.
     *
     * <p>Note that only one of getPostParams() and getPostBody() can return a non-null
     * value.</p>
     *
     * @throws AuthFailureError In the event of auth failure
     * @deprecated Use {@link #getParams()} instead.
     */
    /*
     * 已经废弃了！
     *
     * 获取 HTTP POST 请求的请求参数，默认为 返回 null
     * 1. 如果返回 null 的话，就代表是一个 GET 请求
     * 2. 如果返回有数据 Map<String, String>，就代表是一个 POST 请求
     *
     * 注意：如果有需要的话，需要覆写该方法，为这个请求 Request 提供 请求参数，使其成为一个 POST 请求
     */
    @Deprecated protected Map<String, String> getPostParams() throws AuthFailureError {
        return getParams();
    }


    /**
     * Returns which encoding should be used when converting POST parameters returned by
     * {@link #getPostParams()} into a raw POST body.
     *
     * <p>This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values into bytes prior
     * to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters into a raw
     * byte array.</li>
     * </ol>
     *
     * @deprecated Use {@link #getParamsEncoding()} instead.
     */
    /*
     * 已经废弃了！
     *
     * 获取该请求 Request 的 POST 请求参数编码
     *
     * 注意：如果有需要的话，需要覆写该方法，为这个请求 Request 提供 请求参数编码
     */
    @Deprecated protected String getPostParamsEncoding() {
        return getParamsEncoding();
    }


    /**
     * @deprecated Use {@link #getBodyContentType()} instead.
     */
    /*
     * 已经废弃了！
     *
     * 获取该 请求的 POST 或 PUT 请求 body 的 Content-Type
     */
    @Deprecated public String getPostBodyContentType() {
        return getBodyContentType();
    }


    /**
     * Returns the raw POST body to be sent.
     *
     * @throws AuthFailureError In the event of auth failure
     * @deprecated Use {@link #getBody()} instead.
     */
    /*
     * 已经废弃了！
     *
     * 获取 POST 请求的 body
     */
    @Deprecated public byte[] getPostBody() throws AuthFailureError {
        // Note: For compatibility with legacy clients of volley, this implementation must remain
        // here instead of simply calling the getBody() function because this function must
        // call getPostParams() and getPostParamsEncoding() since legacy clients would have
        // overridden these two member functions for POST requests.

        /*
         * 这里是为了兼容老版本
         * 老版本里的 getPostBody() 方法调用了： getPostParams() 和 getPostParamsEncoding()
         * 而新的版本里推荐的 getBody() 方法调用了：getParams() 和 getParamsEncoding()
         */
        Map<String, String> postParams = getPostParams();
        if (postParams != null && postParams.size() > 0) {
            return encodeParameters(postParams, getPostParamsEncoding());
        }
        return null;
    }


    /**
     * Returns a Map of parameters to be used for a POST or PUT request.  Can throw
     * {@link AuthFailureError} as authentication may be required to provide these values.
     *
     * <p>Note that you can directly override {@link #getBody()} for custom data.</p>
     *
     * @throws AuthFailureError in the event of auth failure
     */

    /*
     * 该方法的出现是为了 新版本中废弃的  getPostParams() 方法
     *
     * 获取 POST 请求或者 PUT 请求的请求参数，默认返回为 null
     *
     * 注意：如果有需要的话，需要覆写该方法，为这个请求 Request 提供 POST 或者 PUT 请求的请求参数
     */
    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }


    /**
     * Returns which encoding should be used when converting POST or PUT parameters returned by
     * {@link #getParams()} into a raw POST or PUT body.
     *
     * <p>This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values into bytes prior
     * to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters into a raw
     * byte array.</li>
     * </ol>
     */
    /*
     * 该方法的出现是为了 新版本中废弃的 getPostParamsEncoding() 方法
     *
     * 获取 POST 请求或者 PUT 请求的请求参数编码，默认返回为 null
     *
     * 注意：如果有需要的话，需要覆写该方法，为这个请求 Request 提供 POST 或者 PUT 请求的请求参数编码
     */
    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }


    /**
     * Returns the content type of the POST or PUT body.
     */
    /*
     * 该方法的出现是为了 新版本中废弃的 getPostBodyContentType() 方法
     *
     * 获取该 请求的 POST 或 PUT 请求 body 的 Content-Type
     */
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }


    /**
     * Returns the raw POST or PUT body to be sent.
     *
     * <p>By default, the body consists of the request parameters in
     * application/x-www-form-urlencoded format. When overriding this method, consider overriding
     * {@link #getBodyContentType()} as well to match the new body format.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    /*
     * 该方法的出现是为了 新版本中废弃的 getPostBody() 方法
     *
     * 获取 POST 或 PUT 请求的 body 数据
     */
    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }


    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    /*
     * 将参数转换成一个 "application/x-www-form-urlencoded" 编码的字符串
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            // 把所有参数拼接成一个字符串
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            // 将字符串进行编码，转换为一个 byte[]
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }


    /**
     * Set whether or not responses to this request should be cached.
     *
     * @return This Request object to allow for chaining.
     */
    /*
     * 设置该请求是否 需要缓存
     */
    public final Request<?> setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
        return this;
    }


    /**
     * Returns true if responses to this request should be cached.
     */
    /*
     * 获取 该请求 需要缓存 的标识
     */
    public final boolean shouldCache() {
        return mShouldCache;
    }


    /**
     * Sets whether or not the request should be retried in the event of an HTTP 5xx (server)
     * error.
     *
     * @return This Request object to allow for chaining.
     */
    /*
     * 设置 该请求发生 5xx 的服务端错误时，是否要重试
     */
    public final Request<?> setShouldRetryServerErrors(boolean shouldRetryServerErrors) {
        mShouldRetryServerErrors = shouldRetryServerErrors;
        return this;
    }


    /**
     * Returns true if this request should be retried in the event of an HTTP 5xx (server) error.
     */
    /*
     * 获取 该请求发生 5xx 的服务端错误时，是否要重试 的标识
     */
    public final boolean shouldRetryServerErrors() {
        return mShouldRetryServerErrors;
    }


    /**
     * Priority values.  Requests will be processed from higher priorities to
     * lower priorities, in FIFO order.
     */
    /*
     * 定义一个枚举
     * 用于标识 这个请求 Request 的优先级别
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }


    /**
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by default.
     */
    /*
     * 获取该请求的 优先级
     */
    public Priority getPriority() {
        return Priority.NORMAL;
    }


    /**
     * Returns the socket timeout in milliseconds per retry attempt. (This value can be changed
     * per retry attempt if a backoff is specified via backoffTimeout()). If there are no retry
     * attempts remaining, this will cause delivery of a {@link TimeoutError} error.
     */
    /*
     * 获取 请求超时时间，默认为 2500 毫秒 （ 见 DefaultRetryPolicy ）
     */
    public final int getTimeoutMs() {
        return mRetryPolicy.getCurrentTimeout();
    }


    /**
     * Returns the retry policy that should be used  for this request.
     */
    /*
     * 获取 重试策略 类
     */
    public RetryPolicy getRetryPolicy() {
        return mRetryPolicy;
    }


    /**
     * Mark this request as having a response delivered on it.  This can be used
     * later in the request's lifetime for suppressing identical responses.
     */
    /*
     * 设置 mResponseDelivered 标识 = true
     * 标识 请求已经被传递了
     */
    public void markDelivered() {
        mResponseDelivered = true;
    }


    /**
     * Returns true if this request has had a response delivered for it.
     */
    /*
     * 查看该请求 Request 是否被传递
     */
    public boolean hasHadResponseDelivered() {
        return mResponseDelivered;
    }


    /**
     * Subclasses must implement this to parse the raw network response
     * and return an appropriate response type. This method will be
     * called from a worker thread.  The response will not be delivered
     * if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    /*
     * 子类 将会覆写该方法
     * 然后做对应的 网络 请求结果（ 响应 ）解析
     */
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);


    /**
     * Subclasses can override this method to parse 'networkError' and return a more specific
     * error.
     *
     * <p>The default implementation just returns the passed 'networkError'.</p>
     *
     * @param volleyError the error retrieved from the network
     * @return an NetworkError augmented with additional information
     */
    /*
     * 子类 将会覆写该方法
     * 然后做对应的 网络 请求错误 解析
     */
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return volleyError;
    }


    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     *
     * @param response The parsed response returned by
     * {@link #parseNetworkResponse(NetworkResponse)}
     */
    /*
     * 子类 将会覆写该方法
     * 然后将对应的 网络 请求结果（ 响应 ）解析结果 传递下去
     */
    abstract protected void deliverResponse(T response);


    /**
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     *
     * @param error Error details
     */
    /*
     * 传递错误
     * 如果 请求结果 Response 发生错误的回调接口 存在
     * 调用回调接口进行错误传递
     */
    public void deliverError(VolleyError error) {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
    }


    /**
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    @Override public int compareTo(Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO ordering.

        /*
         * 1. 如果优先级一样，那么根据 请求的序号 进行比较
         * 2. 如果优先级不一样，那么根据 优先级 进行比较，就是同等优先级，会采用 FIFO 排序
         */
        return left == right ? this.mSequence - other.mSequence : right.ordinal() - left.ordinal();
    }

    /*
     * 输出 Request 信息内容
     */
    @Override public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " " +
                getPriority() + " " + mSequence;
    }
}
