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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * An HttpStack that performs request over an {@link HttpClient}.
 */
/*
 * HttpClientStack 实现了 HttpStack 接口
 * 基于 org.apache.http 提供的网络实现，
 * 处理了 2.3 版本以下的各种网络请求
 */
public class HttpClientStack implements HttpStack {

    // 保存一个 org.apache.http.HttpClient 对象，用于发起请求
    protected final HttpClient mClient;

    // 设置请求头信息的 key 内容 "Content-Type"
    private final static String HEADER_CONTENT_TYPE = "Content-Type";


    /*
     * 构造方法
     * 由于 HttpClientStack 的设计是要基于 org.apache.http.HttpClient 实现网络请求
     * 所以，一个 HttpClient 对象 作为参数
     */
    public HttpClientStack(HttpClient client) {
        mClient = client;
    }


    /*
     * 添加 请求头信息
     * 就是遍历一个 Map ，去多次调用 org.apache.http.client.methods.HttpUriRequest
     * 的添加 头信息方法，将数据逐个添加进去
     */
    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }


    /*
     * 通过传入的 POST 请求参数（ 一个 Map ）
     * 然后构造成 org.apache.http 的 POST 请求的 请求参数 一组 NameValuePair
     */
    @SuppressWarnings("unused")
    private static List<NameValuePair> getPOSTParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(postParams.size());
        for (String key : postParams.keySet()) {
            result.add(new BasicNameValuePair(key, postParams.get(key)));
        }
        return result;
    }


    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        // 创建一个 Apache HTTP 请求
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        // 添加 performRequest 方法传入的 头信息
        addHeaders(httpRequest, additionalHeaders);
        // 添加 Volley 抽象请求了 Request 设置的 头信息
        addHeaders(httpRequest, request.getHeaders());
        // 回调（ 如果被覆写的话 ） 预请求 方法
        onPrepareRequest(httpRequest);
        // 获取 Apache 请求的 HttpParams 对象
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();
        // TODO: Reevaluate this connection timeout based on more wide-scale
        // data collection and possibly different for wifi vs. 3G.
        // 设置超时时间
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        // 设置 SO_TIMEOUT
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        // 开始执行请求
        return mClient.execute(httpRequest);
    }


    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     */
    /* protected */

    /*
     * Volley 的抽象请求 Request -> Apache 请求的转换
     * 会判断 Request 的请求方法，然后封装成对应的 Apache 请求
     *
     * Request.Method.DEPRECATED_GET_OR_POST：表示可能是 GET 请求，也可能是 POST 请求
     *                                        还需要进一步判断 request.getPostBody() 是否为null
     *                                        然后再决定转换为 HttpPost 还是 HttpGet
     *
     * Request.Method.GET：转换为 HttpGet
     *
     * Request.Method.DELETE：转换为 HttpDelete
     *
     * Request.Method.POST：转换为 HttpPost
     *
     * Request.Method.PUT：转换为 HttpPut
     *
     * Request.Method.HEAD：转换为 HttpHead
     *
     * Request.Method.OPTIONS：转换为 HttpOptions
     *
     * Request.Method.TRACE：转换为 HttpTrace
     *
     * Request.Method.PATCH：转换为 HttpPatch （ HttpClientStack 内自实现的一个
     *                       提供给 HttpClientStack 在 HTTP PATCH 方法实现的内部类 ）
     */
    @SuppressWarnings("deprecation")
    static HttpUriRequest createHttpRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws AuthFailureError {
        switch (request.getMethod()) {
            /*
             * 在不明确是 GET 请求，还是 POST 请求的情况下
             * 标识为 Method.DEPRECATED_GET_OR_POST 方法
             * 以下进行了一波判断：request.getPOSTBody() ？
             * 1. request.getPOSTBody()==null 判断为 GET 请求
             * 2. request.getPOSTBody()!=null 判断为 POST 请求，然后进行添加 Header 信息，以及添加
             *    Apache 定义的 HttpEntity 信息
             */
            case Method.DEPRECATED_GET_OR_POST: {
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                // 判定为 POST 请求
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(request.getUrl());
                    // 添加 HttpPost 的 Header 数据
                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    HttpEntity entity;
                    // 添加 HttpPost 的 HttpEntity 数据
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    // 判定为 GET 请求
                    return new HttpGet(request.getUrl());
                }
            }
            // 转换为 HttpGet
            case Method.GET:
                return new HttpGet(request.getUrl());
            // 转换为 HttpDelete
            case Method.DELETE:
                return new HttpDelete(request.getUrl());
            // 转换为 HttpPost
            case Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrl());
                // 设置 请求头信息
                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                // 需要给 Apache 的 POST 请求 设置 HttpEntity
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            // 转换为 HttpPut
            case Method.PUT: {
                HttpPut putRequest = new HttpPut(request.getUrl());
                // 设置 请求头信息
                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                // 需要给 Apache 的 PUT 请求 设置 HttpEntity
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            // 转换为 HttpHead
            case Method.HEAD:
                return new HttpHead(request.getUrl());
            // 转换为 HttpOptions
            case Method.OPTIONS:
                return new HttpOptions(request.getUrl());
            // 转换为 HttpTrace
            case Method.TRACE:
                return new HttpTrace(request.getUrl());
            case Method.PATCH: {
                HttpPatch patchRequest = new HttpPatch(request.getUrl());
                // 设置 请求头信息
                patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                // 需要给 Apache 的 PATCH 请求 设置 HttpEntity
                setEntityIfNonEmptyBody(patchRequest, request);
                return patchRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }


    /**
     * Volley 的 抽象请求 Request -> org.apache.http.client.methods.HttpEntityEnclosingRequestBase
     * 的封装过渡
     *
     * 即 Volley 的 抽象请求 Request -> Apache 请求的 封装过渡
     * 主要工作是 判断 Volley 的 抽象请求 Request 的 请求数据 赋值 给一个 org.apache.http.client.methods.HttpEntityEnclosingRequestBase
     * 作为其的 HttpEntityEnclosingRequestBase.HttpEntity 存在
     *
     * @param httpRequest httpRequest
     * @param request request
     * @throws AuthFailureError AuthFailureError
     */
    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest, Request<?> request)
            throws AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }


    /**
     * Called before the request is executed using the underlying HttpClient.
     *
     * <p>Overwrite in subclasses to augment the request.</p>
     */

    /*
     * 开放一个 预请求 方法
     * 可以进行覆写，然后在 请求前会被调用
     * 你可以做一些 请求 前要做的事情
     *
     * 这里：默认是没有实现
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }


    /**
     * The HttpPatch class does not exist in the Android framework, so this has been defined here.
     */

    /*
     * 自定义了一个 HttpPatch 类 扩展了 org.apache.http.client.methods.HttpEntityEnclosingRequestBase 类
     * HttpPatch 主要提供给 HttpClientStack 在 HTTP PATCH 方法实现的内部类
     */
    public static final class HttpPatch extends HttpEntityEnclosingRequestBase {

        /*
         * 写一个 Patch 方法名标识
         * 标识 这个 HttpPatch 就代表处理 Patch 方法
         */
        public final static String METHOD_NAME = "PATCH";


        public HttpPatch() {
            super();
        }


        /*
         * 就是额外调用了 HttpEntityEnclosingRequestBase.setURI(Uri uri)
         */
        public HttpPatch(final URI uri) {
            super();
            setURI(uri);
        }


        /**
         * @throws IllegalArgumentException if the uri is invalid.
         */
        /*
         * 还是额外调用了 HttpEntityEnclosingRequestBase.setURI(Uri uri)
         */
        public HttpPatch(final String uri) {
            super();
            setURI(URI.create(uri));
        }


        @Override public String getMethod() {
            return METHOD_NAME;
        }
    }
}
