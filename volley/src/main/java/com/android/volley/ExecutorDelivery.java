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
import java.util.concurrent.Executor;

/**
 * Delivers responses and errors.
 */

/*
 * ExecutorDelivery 实现了 ResponseDelivery 接口
 *
 * 主要功能就是：
 * 1.传递 请求、请求结果 or 相应错误 以及 可能有自定义的 Runnable 执行回调操作。
 * 2.定义了一个 Executor，因为可能会有自定义的 Runnable 执行回调操作，所以需要它的存在。
 * 3.因为有 Executor 和 Runnable 的存在，但是都在子线程内。所以，还
 *   需要传入一个 UI 线程的 Handler（大多情况回调都是跟 UI 线程通信）
 *   可以在 RequestQueue 类中 搜搜 "new Handler(Looper.getMainLooper())" 关键句
 *
 * 构造方法也是有趣设计：
 *
 * 1. Handler 作为参数的构造方法。全 Volley 只有 RequestQueue 内使用，并且传入一个 UI 线程的 Handler
 *    可以简单得出：这个构造方法可以指定（ 不只是 UI 线程 ）线程的 Handler 去处理这个 Runnable
 *    看过 Handler 源码的都知道：你给我一个 Runnable，我会 拿到这个 Handler 实例化时的 Looper，再拿到
 *    MessageQueue 去添加一条 Message ，Runnable 则作为 这个 Message.callback 保存在这，然后在
 *    Handler.dispatchMessage(...) 的时候，会执行 Message.callback.run()
 *
 *    所以，这里（不只是 UI 线程）线程的 Handler，说明了可以给其他线程传递（ 通信 ）。
 *
 * 2. Executor 作为参数的构造方法。这里由于 Executor 作为外部提供的参数，那么 Handler 也在外面提供了，更具有
 *    可定制性。Handler 作为参数的构造方法 就是 这个 Executor 作为参数的构造方法 的升级版。
 *
 *    因为，ExecutorDelivery 需要 Executor 和 Handler。
 *    Handler 作为参数的构造方法：实例化了 Executor，Handler由外部提供，这里 Executor 是默认执行
 *                             handler.post(Runnable command)
 *    Executor 作为参数的构造方法：都需要外部提供。
 *
 */
public class ExecutorDelivery implements ResponseDelivery {
    /** Used for posting responses, typically to the main thread. */
    // 用于处理 Runnable
    private final Executor mResponsePoster;


    /**
     * Creates a new response delivery interface.
     *
     * @param handler {@link Handler} to post responses on
     */
    // Handler 作为参数的构造方法
    public ExecutorDelivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        /*
         * 实例化 Executor，并用 handler 去执行
         * execute(Runnable command) 传入的 Runnable
         */
        mResponsePoster = new Executor() {
            @Override public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }


    /**
     * Creates a new response delivery interface, mockable version
     * for testing.
     *
     * @param executor For running delivery tasks
     */
    // Executor 作为参数的构造方法
    public ExecutorDelivery(Executor executor) {
        mResponsePoster = executor;
    }


    /*
     * Runnable 设置为 null
     * 执行 postResponse(Request<?> request, Response<?> response, Runnable runnable)
     */
    @Override public void postResponse(Request<?> request, Response<?> response) {
        postResponse(request, response, null);
    }


    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
        /*
         * 在 Request 做一个标记，标记这个 Request 具有 response
         * 可以传递 response 内容，而不是下面的 VolleyError
         */
        request.markDelivered();
        /*
         * 添加tag，用于 debug 调试
         * key：post-response
         * value：当前线程的 id
         */
        request.addMarker("post-response");
        // 调用 Executor 去执行回调操作一个 new ResponseDeliveryRunnable
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, runnable));
    }


    @Override public void postError(Request<?> request, VolleyError error) {
        /*
         * 添加tag，用于 debug 调试
         * key：post-error
         * value：当前线程的 id
         */
        request.addMarker("post-error");
        // Response.error 静态方法，构造一个 Error response
        Response<?> response = Response.error(error);
        // 调用 Executor 去执行回调操作一个 new ResponseDeliveryRunnable
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, null));
    }


    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    /*
     * @formatter:off
     *
     * ResponseDeliveryRunnable 实现了 Runnable 接口
     * 扩展了 run() 的功能：使 run() 过程中 调用了一些  Request 的方法
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {    // @formatter:on

        private final Request mRequest;
        private final Response mResponse;
        private final Runnable mRunnable;


        /**
         * 构造方法
         *
         * @param request 请求
         * @param response 请求结果
         * @param runnable 回调执行线程
         */
        public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable) {
            mRequest = request;
            mResponse = response;
            mRunnable = runnable;
        }


        @SuppressWarnings("unchecked") @Override public void run() {
            // If this request has canceled, finish it and don't deliver.
            /*
             * 判断 Request 是否被取消
             * 如果取消，调用 finish()，并且返回。即不执行传递
             * 如果没取消，继续执行
             */
            if (mRequest.isCanceled()) {
                mRequest.finish("canceled-at-delivery");
                return;
            }

            // Deliver a normal response or error, depending.
            // 判断请求是否成功，选择传递 正常响应 或者 错误
            if (mResponse.isSuccess()) {
                // 传递 正常响应
                mRequest.deliverResponse(mResponse.result);
            } else {
                // 传递 错误
                mRequest.deliverError(mResponse.error);
            }

            // If this is an intermediate response, add a marker, otherwise we're done
            // and the request can be finished.

            /*
             * Request.intermediate=true 只会在 CacheDispatcher.run() 需要 刷新的情况下 触发
             * 可以知道 如果是用的是 缓存 Request，以及缓存 Request 解析出的 Response
             *
             * // Mark the response as intermediate.
             * response.intermediate = true;
             *
             * // Post the intermediate response back to the user and have
             * // the delivery then forward the request along to the network.
             * mDelivery.postResponse(request, response, new Runnable() {
             *     @Override
             *     public void run() {
             *         try {
             *             mNetworkQueue.put(request);
             *         } catch (InterruptedException e) {
             *             // Not much we can do about this.
             *         }
             *     }
             *});
             *
             * 由于目前 Volley 全局就 RequestQueue 用一个 ExecutorDelivery
             * 由此可以看出 mDelivery = ExecutorDelivery
             *
             * 在拿到缓存 Request 后，由于调用 ExecutorDelivery 的情景都是在拿到 响应 or 错误后
             * （ 如果还取得 响应 or 错误，就不存在传递数据逻辑）
             * 注： 这里 之所以 Request.intermediate=true 的可能性，只发生在 缓存请求 需要刷新
             * 不然，都有了响应，就不需要再次请求了（ 执行 finish("done") ），所以不属于 刷新缓存 的
             * 情景都 要 finish("done")
             *
             * 属于 刷新缓存 的情况，会将请求加入到 NetworkQueue 内。
             *
             * 所以得出了 如果从缓存中拿出 Request 判断需要刷新的话
             */
            if (mResponse.intermediate) {
                /*
                 * 属于 刷新缓存，这是 tag = "intermediate-response" ，并且会在下面的 Runnable
                 * 执行 过程中 执行 NetworkQueue.put(request) 操作
                 */
                mRequest.addMarker("intermediate-response");
            } else {
                // 不属于 刷新缓存，标记完成。
                mRequest.finish("done");
            }

            // If we have been provided a post-delivery runnable, run it.
            // 如果回调执行线程不为 null，则执行
            if (mRunnable != null) {
                mRunnable.run();
            }
        }
    }
}
