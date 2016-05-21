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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */

/*
 * HurlStack 实现了 HttpStack 接口
 * 基于 javax.net.ssl.HttpsURLConnection 提供的网络实现，
 * 处理了 2.3 版本以上的各种网络请求
 */
public class HurlStack implements HttpStack {

    // 设置请求头信息的 key 内容 "Content-Type"
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * An interface for transforming URLs before use.
     */

    /*
     * 提供一个 UrlRewriter 接口
     * 用于在 执行 网络请求前，改变 Url
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        /*
         * 传入 原始要请求的 Url
         * 可以写自定义的转换 Url 逻辑：比如原本要请求 xxx，可以转换为 xxx/me 等需要转换 Url 的情况
         */
        public String rewriteUrl(String originalUrl);
    }

    // 保存一个 UrlRewriter 对象，用于转换 Url
    private final UrlRewriter mUrlRewriter;
    // 保存一个 SSLSocketFactory 对象，用于处理 HTTPS 请求
    private final SSLSocketFactory mSslSocketFactory;


    /*
     * 无参构造方法
     * UrlRewriter 都默认为 null
     * SSLSocketFactory 都默认为 null
     */
    public HurlStack() {
        this(null);
    }


    /**
     * @param urlRewriter Rewriter to use for request URLs
     */
    /*
     * UrlRewriter 参数构造方法
     * 可以指定 UrlRewriter
     */
    public HurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }


    /**
     * @param urlRewriter Rewriter to use for request URLs
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    /*
     * UrlRewriter、SSLSocketFactory 参数构造方法
     * 可以指定 UrlRewriter
     * 可以指定 SSLSocketFactory
     */
    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
    }


    /*
     * 执行处理 Volley内的 抽象请求 Request<?>
     * 这里会调用 HttpURLConnection 去处理网络请求
     * 但是 HttpURLConnection 处理后，都返回 Apache 的请求结果（ HttpResponse ）
     * performRequest(...) 接下来会将：Apache HttpResponse -> Volley NetworkResponse 进行转化
     */
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        // 获取 Volley 抽象请求 Request 中的 String 类型的 Url
        String url = request.getUrl();
        // 实例化一个 HashMap 来存放 Header 信息
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        // 判断是否有 Url 重写的逻辑
        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            // 重新赋值上 UrlRewriter 接口 重写的 Url
            url = rewritten;
        }
        // 实例化一个 java.net.Url 对象
        URL parsedUrl = new URL(url);
        /*
         * 将 URL 对象 和 Volley 的抽象请求 Request 传入到 openConnection(...) 方法内
         * 完成 Volley 抽象请求 Request -> HttpURLConnection 的转换过渡
         * 此时拿到一个 HttpURLConnection 对象
         */
        HttpURLConnection connection = openConnection(parsedUrl, request);
        // 根据刚才存放 Header 信息的 Map，给 HttpURLConnection 添加头信息
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        // 设置 HttpURLConnection 请求方法类型
        setConnectionParametersForRequest(connection, request);
        // Initialize HttpResponse with data from the HttpURLConnection.
        /*
         * 初始化 一个 Apache 的 HTTP 协议 （ ProtocolVersion ）
         */
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);

        /*
         * 正常情况下 HttpURLConnection 是依靠 connect() 发请求的
         *
         * 但是 HttpURLConnection 的 getInputStream() 和 getOutputStream() 也会自动调用 connect()
         *
         * HttpURLConnection 的 getResponseCode() 会调用 getInputStream()
         * 然后 getInputStream() 又会自动调用 connect()，于是
         *
         * 这里就是 发请求了
         */
        int responseCode = connection.getResponseCode();

        // responseCode == -1 表示 没有返回内容
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        // 实例化  org.apache.http.StatusLine 对象
        StatusLine responseStatus = new BasicStatusLine(protocolVersion,
                connection.getResponseCode(), connection.getResponseMessage());
        // 用 org.apache.http.StatusLine 去实例化一个 Apache 的 Response
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        /*
         * 判断请求结果 Response 是否存在 body
         *
         * 有的话，给刚才实例话的 Apache Response 设置 HttpEntity（ 调用 entityFromConnection(...)
         * 通过一个 HttpURLConnection 获取其对应的 HttpEntity ）
         */
        if (hasResponseBody(request.getMethod(), responseStatus.getStatusCode())) {
            response.setEntity(entityFromConnection(connection));
        }
        // 设置 请求结果 Response 的头信息
        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }
        // 返回设置好的 Apache Response
        return response;
    }


    /**
     * Checks if a response message contains a body.
     *
     * @param requestMethod request method
     * @param responseCode response status code
     * @return whether the response has a body
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     */
    /*
     * 检查请求结果 Response 是否存在 body
     *
     * 规则是这样的，要 必须 满足 5 点：
     * 1. 请求方法不是 HEAD 方法
     * 2. Status > 100
     * 3. Status < 200
     * 4. Status != 204
     * 5. Status != 304
     *
     */
    private static boolean hasResponseBody(int requestMethod, int responseCode) {
        return requestMethod != Request.Method.HEAD &&
                !(HttpStatus.SC_CONTINUE <= responseCode && responseCode < HttpStatus.SC_OK) &&
                responseCode != HttpStatus.SC_NO_CONTENT &&
                responseCode != HttpStatus.SC_NOT_MODIFIED;
    }


    /**
     * Initializes an {@link HttpEntity} from the given {@link HttpURLConnection}.
     *
     * @return an HttpEntity populated with data from <code>connection</code>.
     */
    /*
     *  通过一个 HttpURLConnection 获取其对应的 HttpEntity （ 这里就 HttpEntity 而言，耦合了 Apache ）
     */
    private static HttpEntity entityFromConnection(HttpURLConnection connection) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        // 设置 HttpEntity 的内容
        entity.setContent(inputStream);
        // 设置 HttpEntity 的长度
        entity.setContentLength(connection.getContentLength());
        // 设置 HttpEntity 的编码
        entity.setContentEncoding(connection.getContentEncoding());
        // 设置 HttpEntity Content-Type
        entity.setContentType(connection.getContentType());
        return entity;
    }


    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    /*
     * 根据 URL 创建一个 HttpURLConnection
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {
        // URL -> HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Workaround for the M release HttpURLConnection not observing the
        // HttpURLConnection.setFollowRedirects() property.
        // https://code.google.com/p/android/issues/detail?id=194495
        /*
         * 对 M 版本以上 做兼容
         */
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());

        return connection;
    }


    /**
     * Opens an {@link HttpURLConnection} with parameters.
     *
     * @return an open connection
     * @throws IOException
     */

    /*
     * Volley 抽象请求 Request -> HttpURLConnection 的转换过渡
     */
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        // 根据 URL 创建一个 HttpURLConnection
        HttpURLConnection connection = createConnection(url);

        // 获取 Volley 抽象请求 Request 中的 超时时间
        int timeoutMs = request.getTimeoutMs();
        // 设置超时时间
        connection.setConnectTimeout(timeoutMs);
        // 设置读取时间
        connection.setReadTimeout(timeoutMs);
        // 关闭缓存
        connection.setUseCaches(false);
        /*
         * 使用 URL 连接进行输入，则将 DoInput 标志设置为 true
         * 之后就可以使用conn.getInputStream().read()
         */
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS

        /*
         * 处理 HTTPS （ HTTP2 ） 请求：
         * 就是拿到 url 的协议，判断是不是 "https" && 存在 SSLSocketFactory 对象
         * 然后 HttpsURLConnection.setSSLSocketFactory(SSLSocketFactory sf)
         */
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }


    /* package */
    /*
     * 设置 HttpURLConnection 请求方法类型
     *
     * Method.DEPRECATED_GET_OR_POST
     *
     * Method.GET：setRequestMethod("GET")
     *
     * Method.DELETE：setRequestMethod("DELETE")
     *
     * Method.POST：setRequestMethod("POST")
     *
     * Method.PUT：setRequestMethod("PUT")
     *
     * Method.HEAD：setRequestMethod("HEAD")
     *
     * Method.OPTIONS：setRequestMethod("OPTIONS")
     *
     * Method.TRACE：setRequestMethod("TRACE")
     *
     * Method.PATCH：setRequestMethod("PATCH")
     */
    @SuppressWarnings("deprecation")
    static void setConnectionParametersForRequest(HttpURLConnection connection, Request<?> request)
            throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            /*
             * 在不明确是 GET 请求，还是 POST 请求的情况下
             * 标识为 Method.DEPRECATED_GET_OR_POST 方法
             * 以下进行了一波判断：request.getPOSTBody() ？
             * 1. request.getPOSTBody()==null 判断为 GET 请求，设置 HttpURLConnection.setRequestMethod("GET")
             * 2. request.getPOSTBody()!=null 判断为 POST 请求，然后进行添加 Content-Type 信息，以及调用
             *    HttpURLConnection 的 OutputStream 去将 请求数据写入
             */
            case Method.DEPRECATED_GET_OR_POST:
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    // Prepare output. There is no need to set Content-Length explicitly,
                    // since this is handled by HttpURLConnection using the size of the prepared
                    // output stream.
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty(HEADER_CONTENT_TYPE,
                            request.getPostBodyContentType());
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(postBody);
                    out.close();
                }
                break;
            // GET 请求，这是请求方法为 "GET"
            case Method.GET:
                // Not necessary to set the request method because connection defaults to GET but
                // being explicit here.
                connection.setRequestMethod("GET");
                break;
            // DELETE 请求，这是请求方法为 "DELETE"
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            // POST 请求，这是请求方法为 "POST"
            case Method.POST:
                connection.setRequestMethod("POST");
                // POST 请求需要添加 请求数据
                addBodyIfExists(connection, request);
                break;
            // PUT 请求，这是请求方法为 "PUT"
            case Method.PUT:
                connection.setRequestMethod("PUT");
                // PUT 请求需要添加 请求数据
                addBodyIfExists(connection, request);
                break;
            // HEAD 请求，这是请求方法为 "HEAD"
            case Method.HEAD:
                connection.setRequestMethod("HEAD");
                break;
            // OPTIONS 请求，这是请求方法为 "OPTIONS"
            case Method.OPTIONS:
                connection.setRequestMethod("OPTIONS");
                break;
            // TRACE 请求，这是请求方法为 "TRACE"
            case Method.TRACE:
                connection.setRequestMethod("TRACE");
                break;
            // PATCH 请求，这是请求方法为 "PATCH"
            case Method.PATCH:
                connection.setRequestMethod("PATCH");
                // PATCH 请求需要添加 请求数据
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }


    /*
     * 给一个 HttpURLConnection 添加 请求数据
     * 如果 Volley 的抽象请求 Request 存放着 请求数据
     * 那么就给 HttpURLConnection 添加请求数据
     */
    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            /*
             * 使用 URL 连接进行输出，则将 DoOutput 标志设置为 true
             * 之后就可以使用conn.getOutputStream().write()
             */
            connection.setDoOutput(true);
            // 添加头信息 Content-Type
            connection.addRequestProperty(HEADER_CONTENT_TYPE, request.getBodyContentType());
            // DataOutputStream 写入 请求数据 byte[]
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }
}
