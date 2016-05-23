/**
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Helper that handles loading and caching images from remote URLs.
 *
 * The simple way to use this class is to call {@link ImageLoader#get(String, ImageListener)}
 * and to pass in the default image listener provided by
 * {@link ImageLoader#getImageListener(ImageView, int, int)}. Note that all function calls to
 * this class must be made from the main thead, and all responses will be delivered to the main
 * thread as well.
 */

/*
 * 实现了 Volley Request 下的 图片加载
 * 是一个 简单的 图片加载 范例
 */
public class ImageLoader {
    /** RequestQueue for dispatching ImageRequests onto. */
    /*
     * ImageLoader 内的 请求队列
     */
    private final RequestQueue mRequestQueue;

    /** Amount of time to wait after first response arrives before delivering all responses. */
    /*
     * mBatchedResponses 通过 主 UI 线程 Handler 通信
     * 因为 批处理 mBatchedResponses 内有 BatchedImageRequest
     * BatchedImageRequest 内有 ImageContainer
     * ImageContainer 内有 ImageListener
     * 而这个 ImageListener 很可能是 （ Activity、Fragment ... 等 View ）
     * 不用主 UI 线程去操作，会抛出异常
     *
     * mBatchResponseDelayMs 表示 主线程 Handler.postDelayed() 的延迟时间 为 0.1s
     */
    private int mBatchResponseDelayMs = 100;

    /** The cache implementation to be used as an L1 cache before calling into volley. */
    /*
     * ImageCache 对象
     * 保存外部提供的 ImageCache 实现类
     * 用于缓存图片
     */
    private final ImageCache mCache;

    /**
     * HashMap of Cache keys -> BatchedImageRequest used to track in-flight requests so
     * that we can coalesce multiple requests to the same URL into a single network request.
     */
    /*
     * ImageLoader 中的 正在执行中的请求集合
     */
    private final HashMap<String, BatchedImageRequest> mInFlightRequests
            = new HashMap<String, BatchedImageRequest>();

    /** HashMap of the currently pending responses (waiting to be delivered). */
    /*
     * ImageLoader 中的 正在执行结束的请求集合
     */
    private final HashMap<String, BatchedImageRequest> mBatchedResponses
            = new HashMap<String, BatchedImageRequest>();

    /** Handler to the main thread. */
    /*
     * 保存一个 主 UI 线程 Handler
     * 用于和 主 UI 线程 通信
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** Runnable for in-flight response delivery. */
    /*
     * 用于 批处理 mBatchedResponses 内的 BatchedImageRequest
     * 找到 BatchedImageRequest 内的 ImageContainer
     * 去调用 ImageContainer 的回调接口：
     * 执行成功回调 或者 失败回调
     */
    private Runnable mRunnable;

    /**
     * Simple cache adapter interface. If provided to the ImageLoader, it
     * will be used as an L1 cache before dispatch to Volley. Implementations
     * must not block. Implementation with an LruCache is recommended.
     */
    /*
     * 对外提供的图片缓存接口
     * 然而没做任何实现
     * 这里推荐 自己实现一套 LruCache
     * 这里只是定义 ImageLoader 需要的方法（ getBitmap(...), putBitmap(...) ）而已
     */
    public interface ImageCache {
        /**
         * 根据 请求 url，去从缓存中获取该 url 对应的 Bitmap
         *
         * @param url url
         * @return Bitmap
         */
        public Bitmap getBitmap(String url);

        /**
         * 缓存 该 url 对应的 Bitmap
         *
         * @param url url
         * @param bitmap bitmap
         */
        public void putBitmap(String url, Bitmap bitmap);
    }


    /**
     * Constructs a new ImageLoader.
     *
     * @param queue The RequestQueue to use for making image requests.
     * @param imageCache The cache to use as an L1 cache.
     */
    public ImageLoader(RequestQueue queue, ImageCache imageCache) {
        mRequestQueue = queue;
        mCache = imageCache;
    }


    /**
     * The default implementation of ImageListener which handles basic functionality
     * of showing a default image until the network response is received, at which point
     * it will switch to either the actual image or the error image.
     *
     * @param view The imageView that the listener is associated with.
     * @param defaultImageResId Default image resource ID to use, or 0 if it doesn't exist.
     * @param errorImageResId Error image resource ID to use, or 0 if it doesn't exist.
     */
    public static ImageListener getImageListener(final ImageView view, final int defaultImageResId, final int errorImageResId) {
        return new ImageListener() {
            @Override public void onErrorResponse(VolleyError error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }


            @Override public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        };
    }


    /**
     * Interface for the response handlers on image requests.
     *
     * The call flow is this:
     * 1. Upon being  attached to a request, onResponse(response, true) will
     * be invoked to reflect any cached data that was already available. If the
     * data was available, response.getBitmap() will be non-null.
     *
     * 2. After a network response returns, only one of the following cases will happen:
     * - onResponse(response, false) will be called if the image was loaded.
     * or
     * - onErrorResponse will be called if there was an error loading the image.
     */
    /*
     * ImageListener 扩展了 ErrorListener
     * 使 自身 不仅能回调错误处理，还能回调成功处理
     * 用于回调 图片是否加载 成功 或者 失败
     * onResponse(response, false)：图片加载成功
     * onErrorResponse(VolleyError error)：图片加载失败
     */
    public interface ImageListener extends ErrorListener {
        /**
         * Listens for non-error changes to the loading of the image request.
         *
         * @param response Holds all information pertaining to the request, as well
         * as the bitmap (if it is loaded).
         * @param isImmediate True if this was called during ImageLoader.get() variants.
         * This can be used to differentiate between a cached image loading and a network
         * image loading in order to, for example, run an animation to fade in network loaded
         * images.
         */
        /*
         * isImmediate 表示：是否是直接加载图片
         * 1.目前，Volley 中 只有在 ImageLoader.get() 中会为 true，因为 ImageLoader.get() 是主线程加载图片
         * 2.目前，Volley 中 只有 Runnable 中会为 false，因为 是子线程加载图片
         *
         * 所以可以根据 isImmediate 是否是主线程加载，是的话才能渲染到 View 上
         * 不然 子线程是不允许 渲染View，需要通过 主 UI 线程 Handler
         *
         * 所以，可以发现 isImmediate = false，的情况，都在 主 UI 线程 Handler 中的 Runnable 中
         */
        public void onResponse(ImageContainer response, boolean isImmediate);
    }


    /**
     * Checks if the item is available in the cache.
     *
     * @param requestUrl The url of the remote image
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return True if the item exists in cache, false otherwise.
     */
    /*
     * 默认 scaleType：ScaleType.CENTER_INSIDE
     */
    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        return isCached(requestUrl, maxWidth, maxHeight, ScaleType.CENTER_INSIDE);
    }


    /**
     * Checks if the item is available in the cache.
     *
     * @param requestUrl The url of the remote image
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @param scaleType The scaleType of the imageView.
     * @return True if the item exists in cache, false otherwise.
     */
    /*
     * 检查 图片数据 是否被缓存
     * 根据四个数据匹配图片：
     * 1. requestUrl：该图片的 url
     * 2. maxWidth：该图片的 最大宽度
     * 3. maxHeight：该图片的 最大高度
     * 4. scaleType：该图片的 ImageView.ScaleType（ CENTER_CROP, FIT_XY ... ）
     */
    public boolean isCached(String requestUrl, int maxWidth, int maxHeight, ScaleType scaleType) {
        // 检查是否为 主 UI 线程
        throwIfNotOnMainThread();

        // 根据数据，获取 cacheKey
        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
        /*
         * 从 外部提供的 ImageCache 实现类中
         * 去查找该 url 对应的 Bitmap 缓存
         */
        return mCache.getBitmap(cacheKey) != null;
    }


    /**
     * Returns an ImageContainer for the requested URL.
     *
     * The ImageContainer will contain either the specified default bitmap or the loaded bitmap.
     * If the default was returned, the {@link ImageLoader} will be invoked when the
     * request is fulfilled.
     *
     * @param requestUrl The URL of the image to be loaded.
     */
    /*
     * 默认 maxWidth：0
     * 默认 maxHeight：0
     */
    public ImageContainer get(String requestUrl, final ImageListener listener) {
        return get(requestUrl, listener, 0, 0);
    }


    /**
     * Equivalent to calling {@link #get(String, ImageListener, int, int, ScaleType)} with
     * {@code Scaletype == ScaleType.CENTER_INSIDE}.
     */
    /*
     * 默认 scaleType： ScaleType.CENTER_INSIDE
     */
    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight) {
        return get(requestUrl, imageListener, maxWidth, maxHeight, ScaleType.CENTER_INSIDE);
    }


    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     *
     * @param requestUrl The url of the remote image
     * @param imageListener The listener to call when the remote image is loaded
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @param scaleType The ImageViews ScaleType used to calculate the needed image size.
     * @return A container object that contains all of the properties of the request, as well as
     * the currently available image (default if remote is not loaded).
     */
    /*
     * 加载图片主方法
     * 需要传入：
     * 1. 图片的 url
     * 2. 图片的 ImageListener 加载回调接口
     * 3. 图片的 最大宽度
     * 4. 图片的 最大高度
     * 5. 图片的 ImageView.ScaleType（ CENTER_CROP, FIT_XY ... ）
     *
     * 最后会 返回一个 ImageContainer，包含了 这个 url 对应图片的 各种信息
     */
    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight, ScaleType scaleType) {

        // only fulfill requests that were initiated from the main thread.
        // 主 UI 线程 检查
        throwIfNotOnMainThread();

        // 根据数据，获取 cacheKey
        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);

        // Try to look up the request in the cache of remote images.
        /*
         * 用外部提供的 ImageCache 尝试性获取该 url 的 Bitmap 缓存
         */
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        // 如果拿到改 url 的 Bitmap 缓存
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            // 创建一个 ImageContainer
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            /*
             * 调用 加载回调接口 回调 这个 ImageContainer
             * isImmediate 设置为 true，是因为这里是 主 UI 线程内
             * 上面的 throwIfNotOnMainThread() 会检查是否是 主 UI 线程
             * 是的话，才能执行下来
             */
            imageListener.onResponse(container, true);
            // 返回 ImageContainer
            return container;
        }

        /***********************************************
         * 以下执行到的话，是不存在该 url 对应的缓存 Bitmap *
         ***********************************************/

        // The bitmap did not exist in the cache, fetch it!

        /*
         * 没有缓存，还是要创建一个 ImageContainer
         * 注：只是这个 ImageContainer 内，没有 Bitmap 数据而已
         */
        ImageContainer imageContainer = new ImageContainer(null, requestUrl, cacheKey,
                imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        /*
         * 调用 加载回调接口 回调 这个 ImageContainer
         * isImmediate 设置为 true，是因为这里是 主 UI 线程内
         * 上面的 throwIfNotOnMainThread() 会检查是否是 主 UI 线程
         * 是的话，才能执行下来
         * 注：只是这个 ImageContainer 内，没有 Bitmap 数据而已
         */
        imageListener.onResponse(imageContainer, true);

        /*********************************
         * 查看 这个 Request 是否正在执行中 *
         *********************************/

        // Check to see if a request is already in-flight.
        /*
         * 检查在 ImageLoader 中的 正在执行中的请求集合，是否存在这个 Request
         * 意为：ImageLoader 是否记录这个 Request 正在执行中
         */
        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        // 这个 Request 正在执行中
        if (request != null) {
            // If it is, add this request to the list of listeners.
            // 为该 BatchedImageRequest 添加 ImageContainer
            request.addContainer(imageContainer);
            // 然后返回
            return imageContainer;
        }

        /*************************************************
         * 走到这里，表示这个 Request 没有处于正在请求的过程中 *
         *************************************************/

        // The request is not already in flight. Send the new request to the network and
        // track it.

        /*
         * 由于这个 Request 没有处于正在请求的过程中
         * 所以，这里调用 makeImageRequest(...)，并且传入一些参数
         * 去生成一个 Request<Bitmap> 的请求
         */
        Request<Bitmap> newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight, scaleType,
                cacheKey);

        /*
         * 添加到 RequestQueue 内，会有相应的 线程去执行这个请求
         *（ 缓存请求执行线程（ CacheDispatcher ）和 网络请求执行线程（ NetworkDispatcher ））
         * 什么线程去执行这个请求，还是要看 -> 这个请求是否需要缓存
         */
        mRequestQueue.add(newRequest);
        // 添加在 ImageLoader 中的 正在执行中请求集合 内
        mInFlightRequests.put(cacheKey, new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }


    /*
     * 根据以下数据：
     * 1. requestUrl：图片的 url
     * 2. maxWidth：图片的 最大宽度
     * 3. maxHeight：图片的 最大高度
     * 4. scaleType：图片的 ImageView.ScaleType（ CENTER_CROP, FIT_XY ... ）
     * 5. cacheKey：图片的 cacheKey
     *
     * 去创建一个 Request<Bitmap>（ ImageRequest ）
     */
    protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, ScaleType scaleType, final String cacheKey) {
        /*
         * 直接 创建了一个 ImageRequest
         * 用于执行网络请求，并将请求结果解析成 Bitmap
         * 这里 构造 的 ImageRequest，还设置了两个回调：
         * 1. 成功回调的话，会调用 ImageLoader.onGetImageSuccess(...)
         * 1. 失败回调的话，会调用 ImageLoader.onErrorResponse(...)
         */
        return new ImageRequest(requestUrl, new Listener<Bitmap>() {
            @Override public void onResponse(Bitmap response) {
                // 成功的话，会调用 ImageLoader.onGetImageSuccess(...)
                onGetImageSuccess(cacheKey, response);
            }
        }, maxWidth, maxHeight, scaleType, Config.RGB_565, new ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                // 失败的话，会调用 ImageLoader.onErrorResponse(...)
                onGetImageError(cacheKey, error);
            }
        });
    }


    /**
     * Sets the amount of time to wait after the first response arrives before delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     *
     * @param newBatchedResponseDelayMs The time in milliseconds to wait.
     */
    /*
     * 设置 mBatchedResponses 通过 主 UI 线程 Handler 通信
     * 的 最小 延迟时间
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }


    /**
     * Handler for when an image was successfully loaded.
     *
     * @param cacheKey The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    /*
     * makeImageRequest(...) 创建的 ImageRequest
     * 执行成功后，会调用 onGetImageSuccess(...)
     */
    protected void onGetImageSuccess(String cacheKey, Bitmap response) {
        // cache the image that was fetched.
        /*
         * 用 外部提供的 自定义 ImageCache 实现类
         * 去 根据 cacheKey 和 Bitmap 数据
         * 去完成 一次 缓存操作
         */
        mCache.putBitmap(cacheKey, response);

        // remove the request from the list of in-flight requests.
        /*
         * 因为 请求结束了
         * 所以，从 ImageLoader 中的 正在执行中的请求集合 中移除 该请求的 BatchedImageRequest 记录
         */
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);
        // 成功 移除了 BatchedImageRequest 记录，并拿到 该 BatchedImageRequest
        if (request != null) {
            // Update the response bitmap.
            // 为该 BatchedImageRequest 添加 Bitmap 数据
            request.mResponseBitmap = response;

            // Send the batched response
            /*
             * 调用 batchResponse(...) 方法
             * 将该 BatchedImageRequest 的相关数据，添加到 mBatchedResponses 集合内
             * 同时将 BatchedImageRequest 的请求结果，往 主 UI 线程 传递
             */
            batchResponse(cacheKey, request);
        }
    }


    /**
     * Handler for when an image failed to load.
     *
     * @param cacheKey The cache key that is associated with the image request.
     */
    /*
     * makeImageRequest(...) 创建的 ImageRequest
     * 执行失败后，会调用 onGetImageError(...)
     */
    protected void onGetImageError(String cacheKey, VolleyError error) {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        /*
         * 因为 请求结束了
         * 所以，从 ImageLoader 中的 正在执行中的请求集合 中移除 该请求的 BatchedImageRequest 记录
         */
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);
        // 成功 移除了 BatchedImageRequest 记录，并拿到 该 BatchedImageRequest
        if (request != null) {
            // Set the error for this request
            // 为该 BatchedImageRequest 设置 错误 数据
            request.setError(error);

            // Send the batched response
             /*
             * 调用 batchResponse(...) 方法
             * 将该 BatchedImageRequest 的相关数据，添加到 mBatchedResponses 集合内
             * 同时将 BatchedImageRequest 的请求结果，往 主 UI 线程 传递
             */
            batchResponse(cacheKey, request);
        }
    }


    /**
     * Container object for all of the data surrounding an image request.
     */
    /*
     * 用于 封装 图片 的相关信息：
     * 1. 图片的 Bitmap
     * 2. 图片的 url
     * 3. 图片的 cacheKey
     * 4. 图片的 ImageListener 加载回调接口
     */
    public class ImageContainer {
        /**
         * The most relevant bitmap for the container. If the image was in cache, the
         * Holder to use for the final bitmap (the one that pairs to the requested URL).
         */
        // 图片的 Bitmap
        private Bitmap mBitmap;

        // 图片的 ImageListener 加载回调接口
        private final ImageListener mListener;

        /** The cache key that was associated with the request */
        // 图片的 cacheKey
        private final String mCacheKey;

        /** The request URL that was specified */
        // 图片的 url
        private final String mRequestUrl;


        /**
         * Constructs a BitmapContainer object.
         *
         * @param bitmap The final bitmap (if it exists).
         * @param requestUrl The requested URL for this container.
         * @param cacheKey The cache key that identifies the requested URL for this container.
         */
        public ImageContainer(Bitmap bitmap, String requestUrl, String cacheKey, ImageListener listener) {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }


        /**
         * Releases interest in the in-flight request (and cancels it if no one else is listening).
         */
        public void cancelRequest() {
            // 如果没有 加载回调接口
            if (mListener == null) {
                return;
            }

            // 获取到 该 cacheKey 对应的 BatchedImageRequest
            BatchedImageRequest request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                // 移除 BatchedImageRequest 内 的该 ImageContainer 数据，并取消请求
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                // 移除成功了
                if (canceled) {
                    // 删除 对应 cacheKey 的请求
                    mInFlightRequests.remove(mCacheKey);
                }
            } else {
                // check to see if it is already batched for delivery.
                // 检查该 cacheKey 对应的 BatchedImageRequest 是否被传递
                request = mBatchedResponses.get(mCacheKey);
                // 已经被传递了
                if (request != null) {
                    // 移除 BatchedImageRequest 内 的该 ImageContainer 数据，并取消请求
                    request.removeContainerAndCancelIfNecessary(this);
                    // 如果 BatchedImageRequest 没有 ImageContainer 数据，移除该 BatchedImageRequest
                    if (request.mContainers.size() == 0) {
                        mBatchedResponses.remove(mCacheKey);
                    }
                }
            }
        }


        /**
         * Returns the bitmap associated with the request URL if it has been loaded, null
         * otherwise.
         */
        /*
         * 获取 图片的 Bitmap
         */
        public Bitmap getBitmap() {
            return mBitmap;
        }


        /**
         * Returns the requested URL for this container.
         */
        /*
         * 获取 图片的 url
         */
        public String getRequestUrl() {
            return mRequestUrl;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active ImageContainer objects that are
     * interested in its results.
     */
    private class BatchedImageRequest {
        /** The request being tracked */
        // Volley 请求
        private final Request<?> mRequest;

        /** The result of the request being tracked by this item */
        // 请求结果解析 Bitmap
        private Bitmap mResponseBitmap;

        /** Error if one occurred for this response */
        // 请求发生的错误（ VolleyError ）
        private VolleyError mError;

        /** List of all of the active ImageContainers that are interested in the request */
        /*
         * 该 Request 对应的 一组 ImageContainer
         */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();


        /**
         * Constructs a new BatchedImageRequest object
         *
         * @param request The request being tracked
         * @param container The ImageContainer of the person who initiated the request.
         */
        public BatchedImageRequest(Request<?> request, ImageContainer container) {
            mRequest = request;
            mContainers.add(container);
        }


        /**
         * Set the error for this response
         */
        /*
         * 设置 请求发生的错误（ VolleyError ）
         */
        public void setError(VolleyError error) {
            mError = error;
        }


        /**
         * Get the error for this response
         */
        /*
         * 获取 请求发生的错误（ VolleyError ）
         */
        public VolleyError getError() {
            return mError;
        }


        /**
         * Adds another ImageContainer to the list of those interested in the results of
         * the request.
         */
        /*
         * 添加一个 ImageContainer
         */
        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }


        /**
         * Detatches the bitmap container from the request and cancels the request if no one is
         * left listening.
         *
         * @param container The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        /*
         * 移除 一个 ImageContainer
         * 当 ImageContainer 集合没有数据时，取消该 Request
         */
        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }


    /**
     * Starts the runnable for batched delivery of responses if it is not already started.
     *
     * @param cacheKey The cacheKey of the response being delivered.
     * @param request The BatchedImageRequest to be delivered.
     */
    /*
     * 1. 根据 cacheKey 为 key，BatchedImageRequest 为 value，将该 BatchedImageRequest 添加到
     *    mBatchedResponses 集合中，mBatchedResponses 存放的是 执行成功的 BatchedImageRequest
     * 2.
     */
    private void batchResponse(String cacheKey, BatchedImageRequest request) {
        // 将执行完毕的请求数据（ 无论 成功 或者 失败 ），放入 mBatchedResponses 中
        mBatchedResponses.put(cacheKey, request);
        // If we don't already have a batch delivery runnable in flight, make a new one.
        // Note that this will be used to deliver responses to all callers in mBatchedResponses.
        /*
         * 如果 在 主 UI 线程 执行 处理线程
         * 1. 不存在的话，需要创建一个 Runnable
         * 2. 存在的话，复用上次用过的 Runnable。因为操作一样，都是非方法域 对象 mBatchedResponses 集合内的数据
         */
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override public void run() {
                    // 拿出 每一个 执行完毕的 BatchedImageRequest 数据
                    for (BatchedImageRequest bir : mBatchedResponses.values()) {
                        /*
                         * 再拿出 每一个 BatchedImageRequest 内的
                         * 每一个 的 ImageContainer 数据
                         */
                        for (ImageContainer container : bir.mContainers) {
                            // If one of the callers in the batched request canceled the request
                            // after the response was received but before it was delivered,
                            // skip them.
                            // 如果 ImageContainer 内的回调为 null，则返回
                            if (container.mListener == null) {
                                continue;
                            }

                            /**************************************************
                             * 走到这里 表示 ImageContainer.mListener 不为 null *
                             **************************************************/

                            /**************************************************
                             * 其实 也因为 ImageContainer.mListener 可能是 View *
                             * 所以 才会利用到 主 UI 线程的 Handler 去完成通信操作 *
                             **************************************************/

                            // 如果 该 BatchedImageRequest 请求成功
                            if (bir.getError() == null) {
                                // 为 ImageContainer.mBitmap 赋值
                                container.mBitmap = bir.mResponseBitmap;
                                /*
                                 * 回调 ImageContainer 数据，并标识为
                                 * isImmediate = false：子线程 传递过来的数据
                                 */
                                container.mListener.onResponse(container, false);
                            } else {
                                /*
                                 * 回调 错误 （ VolleyError ）
                                 */
                                container.mListener.onErrorResponse(bir.getError());
                            }
                        }
                    }
                    // 每执行一次 batchResponse(...)，都会清空 mBatchedResponses 的数据
                    mBatchedResponses.clear();
                    // 每执行一次 batchResponse(...)，都会清空 mRunnable 的数据
                    mRunnable = null;
                }
            };
            // Post the runnable.
            // 开始给 主 UI 线程 传递数据
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }


    /*
     * 判断 当前线程 是否 为 主 UI 线程
     * 不是的话，抛出异常
     */
    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }


    /**
     * Creates a cache key for use with the L1 cache.
     *
     * @param url The URL of the request.
     * @param maxWidth The max-width of the output.
     * @param maxHeight The max-height of the output.
     * @param scaleType The scaleType of the imageView.
     */
    /*
     * 创建 一个 图片 cacheKey
     * 根据：
     * 1. url：该图片的 Url
     * 2. maxWidth：该图片的 最大宽度
     * 3. maxHeight：该图片的 最大高度
     * 4. scaleType：该图片的 ImageView.ScaleType（ CENTER_CROP, FIT_XY ... ）
     */
    private static String getCacheKey(String url, int maxWidth, int maxHeight, ScaleType scaleType) {
        return new StringBuilder(url.length() + 12).append("#W")
                                                   .append(maxWidth)
                                                   .append("#H")
                                                   .append(maxHeight)
                                                   .append("#S")
                                                   .append(scaleType.ordinal())
                                                   .append(url)
                                                   .toString();
    }
}
