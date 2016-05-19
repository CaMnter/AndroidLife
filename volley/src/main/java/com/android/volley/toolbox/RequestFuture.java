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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Future that represents a Volley request.
 *
 * Used by providing as your response and error listeners. For example:
 * <pre>
 * RequestFuture&lt;JSONObject&gt; future = RequestFuture.newFuture();
 * MyRequest request = new MyRequest(URL, future, future);
 *
 * // If you want to be able to cancel the request:
 * future.setRequest(requestQueue.add(request));
 *
 * // Otherwise:
 * requestQueue.add(request);
 *
 * try {
 *   JSONObject response = future.get();
 *   // do something with response
 * } catch (InterruptedException e) {
 *   // handle the error
 * } catch (ExecutionException e) {
 *   // handle the error
 * }
 * </pre>
 *
 * @param <T> The type of parsed response this future expects.
 */

/*
 * RequestFuture<T> 实现了 Future<T> 接口
 * 用于对 Request 解析数据的结果进行取消、查询是否完成、获取结果
 */
public class RequestFuture<T> implements Future<T>, Response.Listener<T>, Response.ErrorListener {
    // 被监控的 Request
    private Request<?> mRequest;
    // 标识 请求解析结果 是否收到了
    private boolean mResultReceived = false;
    // 请求解析结果
    private T mResult;
    // 请求错误
    private VolleyError mException;


    /*
     * 对外只提供这个 newFuture() 方法去
     * 创建一个 RequestFuture<E> 对象
     */
    public static <E> RequestFuture<E> newFuture() {
        return new RequestFuture<E>();
    }


    /*
     * 屏蔽默认的无参构造方法
     */
    private RequestFuture() {}


    /*
     * 设置 Request
     */
    public void setRequest(Request<?> request) {
        mRequest = request;
    }


    /*
     * 取消一个 Request
     */
    @Override public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        // 不存在 Request，返回取消失败
        if (mRequest == null) {
            return false;
        }

        // 如果没执行完毕了
        if (!isDone()) {
            // 设置 Request 的取消标识
            mRequest.cancel();
            // 返回取消成功
            return true;
        } else {
            // 如果执行完毕了，取消不了，返回取消失败
            return false;
        }
    }


    /*
     * 调用 doGet(...) 去获取
     * Request 请求结果解析数据
     */
    @Override public T get() throws InterruptedException, ExecutionException {
        try {
            return doGet(null);
        } catch (TimeoutException e) {
            throw new AssertionError(e);
        }
    }


    /*
     * 调用 doGet(...) 去 超时时间 获取
     * Request 请求结果解析数据
     */
    @Override public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return doGet(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }


    /*
     * 获取 Request 的请求结果解析数据，可以设置 超时时间
     */
    private synchronized T doGet(Long timeoutMs)
            throws InterruptedException, ExecutionException, TimeoutException {
        // 如果 存在异常，抛出异常
        if (mException != null) {
            throw new ExecutionException(mException);
        }

        // 如果 解析数据已经 收到了，则返回 解析数据
        if (mResultReceived) {
            return mResult;
        }

        // 延迟处理
        if (timeoutMs == null) {
            wait(0);
        } else if (timeoutMs > 0) {
            wait(timeoutMs);
        }

        // 如果 发生异常，抛出异常
        if (mException != null) {
            throw new ExecutionException(mException);
        }

        // 如果 在 超时时间 内，没拿到数据，则抛出超时异常
        if (!mResultReceived) {
            throw new TimeoutException();
        }

        // 在 超时时间内 拿到了解析数据，则返回
        return mResult;
    }


    /*
     * 查看 Request 是否取消
     */
    @Override public boolean isCancelled() {
        // 如果请求 Request 不存在，返回 false
        if (mRequest == null) {
            return false;
        }
        // 返回 Request 的请求状态
        return mRequest.isCanceled();
    }


    /*
     *
     */
    @Override public synchronized boolean isDone() {
        return mResultReceived || mException != null || isCancelled();
    }


    /*
     * 设置 请求结果解析数据 （ 比如 ImageRequest 的话，就是 Bitmap ）
     */
    @Override public synchronized void onResponse(T response) {
        // 设置 请求结果接收 标识
        mResultReceived = true;
        mResult = response;
        notifyAll();
    }


    /*
     * 设置 请求结果 （ Response ） 错误
     */
    @Override public synchronized void onErrorResponse(VolleyError error) {
        mException = error;
        notifyAll();
    }
}

