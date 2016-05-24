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

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
/*
 * 一个 自带 图片加载功能的 ImageView
 * 图片的相关信息 和 url 绑定 这个 ImageView 的生命周期
 *
 * 并且，在这个 View 从 Window 上分离的时候，会进行回收操作，防止内存泄漏
 */
public class NetworkImageView extends ImageView {
    /** The URL of the network image to load */
    /*
     * 网络 图片 url
     */
    private String mUrl;

    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    /*
     * 默认加载 本地 图片资源 id
     */
    private int mDefaultImageId;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    /*
     * 失败加载 本地 图片资源 id
     */
    private int mErrorImageId;

    /** Local copy of the ImageLoader. */
    /*
     * 用于加载图片：
     * 1. 先根据 url，从缓存中加载图片
     * 2. 缓存加载失败后，会请求网络加载图片
     */
    private ImageLoader mImageLoader;

    /** Current ImageContainer. (either in-flight or finished) */
    /*
     * 当前显示的图片 的相关信息（ 正在执行的请求 或者 已经完成的请求 ）
     */
    private ImageContainer mImageContainer;


    public NetworkImageView(Context context) {
        this(context, null);
    }


    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
     *
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)} and
     * {@link NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url The URL that should be loaded into this ImageView.
     * @param imageLoader ImageLoader that will be used to make the request.
     */
    /*
     * 设置：
     * 1. 图片 url
     * 2. ImageLoader
     */
    public void setImageUrl(String url, ImageLoader imageLoader) {
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }


    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    /*
     * 设置 默认图片的资源 id
     */
    public void setDefaultImageResId(int defaultImage) {
        mDefaultImageId = defaultImage;
    }


    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    /*
     * 设置 错误图片的资源 id
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }


    /**
     * Loads the image for the view if it isn't already loaded.
     *
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    /*
     * 加载图片
     *
     * isInLayoutPass true：XML 渲染的时候就开始加载
     * isInLayoutPass false：XML 渲染的时候不加载
     */
    void loadImageIfNecessary(final boolean isInLayoutPass) {
        // 获取宽高
        int width = getWidth();
        int height = getHeight();
        // 获取 ImageView.ScaleType（ CENTER_CROP, FIT_XY ... ）
        ScaleType scaleType = getScaleType();

        // 记录 是不是 wrap_content 类型的 宽高
        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        // 如果 宽高 都是 wrap_content 类型的，记录 isFullyWrapContent = true
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.

        /*
         * 如果没有图片的 url
         * 除了 不去加载图片 以外
         * 还要取消 当前正在执行的请求
         */
        if (TextUtils.isEmpty(mUrl)) {
            // 取消 当前正在执行的请求
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            // 设置 默认图片
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        /*
         * 当前显示的图片 的相关信息，还存在（ 也可能是前一个 url ）
         */
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            /*
             * 当前显示的图片 url 和 本次需要 加载 的图片 url 一致
             * 就返回，不做任何操作
             */
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                /*
                 * url 不一样
                 * 不管怎样，先将当前 正在执行的请求 取消
                 * 意思是：当前显示的图片，已经达到预期效果
                 * 为了 预防 被修改，要将 ImageContainer 内 正在执行的请求 取消到
                 * 因为：预期效果已经达到，就必须需要正在执行的请求
                 */
                mImageContainer.cancelRequest();
                // 设置 默认图片
                setDefaultImageOrNull();
            }
        }

        // Calculate the max image width / height to use while ignoring WRAP_CONTENT dimens.

        // 根据 wrap 标记，设置最大宽高
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.

        /******************************************************************
         * 使用 ImageLoader 加载图片 返回 对应的 ImageContainer 图片相关信息类 *
         ******************************************************************/

        ImageContainer newContainer = mImageLoader.get(mUrl, new ImageListener() {
            /*
             * 加载 失败
             */
            @Override public void onErrorResponse(VolleyError error) {
                // 设置 加载失败 图片
                if (mErrorImageId != 0) {
                    setImageResource(mErrorImageId);
                }
            }


            /*
             * 加载 成功
             */
            @Override public void onResponse(final ImageContainer response, boolean isImmediate) {
                // If this was an immediate response that was delivered inside of a layout
                // pass do not set the image immediately as it will trigger a requestLayout
                // inside of a layout. Instead, defer setting the image by posting back to
                // the main thread.
                /*
                 * 只有
                 * isImmediate = true
                 * isInLayoutPass = true
                 */
                if (isImmediate && isInLayoutPass) {
                    // 发回 主 UI 线程
                    post(new Runnable() {
                        @Override public void run() {
                            onResponse(response, false);
                        }
                    });
                    return;
                }

                // 如果 存在 Bitmap 数据
                if (response.getBitmap() != null) {
                    // 设置 图片
                    setImageBitmap(response.getBitmap());
                } else if (mDefaultImageId != 0) {
                    // 设置 默认图片
                    setImageResource(mDefaultImageId);
                }
            }
        }, maxWidth, maxHeight, scaleType);

        // update the ImageContainer to be the new bitmap container.
        // 重新设置 图片相关信息
        mImageContainer = newContainer;
    }


    /*
     * 设置 默认图片
     * 不存在的话，设置为 null
     */
    private void setDefaultImageOrNull() {
        if (mDefaultImageId != 0) {
            setImageResource(mDefaultImageId);
        } else {
            setImageBitmap(null);
        }
    }


    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 加载图片
        loadImageIfNecessary(true);
    }


    /**
     * 将视图从窗体上分离的时候调用该方法。这时视图已经不具有可绘制部分
     *
     * 这里做一些 回收操作 防止内存泄漏
     */
    @Override protected void onDetachedFromWindow() {
        // 存在 图片的相关信息
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            // 取消请求
            mImageContainer.cancelRequest();
            // 设置 图片 为 null
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            // 回收  ImageContainer
            mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    /*
     * 每次状态改变
     * 都重新绘制 只调用 onDraw()
     */
    @Override protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
