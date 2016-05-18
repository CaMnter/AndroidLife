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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.widget.ImageView.ScaleType;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Bitmap.
 */

/*
 * 继承扩展了 Request，指定了泛型为 <Bitmap>
 * 会将请求结果解析成 Bitmap 类型数据
 * 并且需要你传入：
 * 1. Response.Listener<Bitmap>：将解析结果数据 进行回调8
 * 2. maxWidth：最大宽度
 * 3. maxHeight：最大高度
 * 4. ScaleType：ImageView 的 ScaleType（ CENTER_CROP, FIT_XY ... ）
 * 5. Config：Bitmap 的 Config （ ALPHA_8, RGB_565, ARGB_4444, ARGB_8888 ）
 * 6. Response.ErrorListener：请求结果 Response 发生错误的回调接口
 */
public class ImageRequest extends Request<Bitmap> {
    /** Socket timeout in milliseconds for image requests */
    // ImageRequest 请求的 Socket 超时时间 -> 1000ms
    public static final int DEFAULT_IMAGE_TIMEOUT_MS = 1000;

    /** Default number of retries for image requests */
    // ImageRequest 的默认重试次数 -> 2
    public static final int DEFAULT_IMAGE_MAX_RETRIES = 2;

    /** Default backoff multiplier for image requests */
    // ImageRequest 默认的 重试策略类的 退避乘数
    public static final float DEFAULT_IMAGE_BACKOFF_MULT = 2f;

    // 解析结果数据 的回调接口
    private final Response.Listener<Bitmap> mListener;
    // Bitmap 的解析配置
    private final Config mDecodeConfig;
    // Bitmap 的最大宽度
    private final int mMaxWidth;
    // Bitmap 的最大高度
    private final int mMaxHeight;
    // ImageView 的 ScaleType（ CENTER_CROP, FIT_XY ... ）
    private ScaleType mScaleType;

    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    /*
     * 全局解码锁
     * 所有 ImageRequest 都被这一个锁控制
     * 一次只能解析一张图片，避免 OOM
     */
    private static final Object sDecodeLock = new Object();


    /**
     * Creates a new image request, decoding to a maximum specified width and
     * height. If both width and height are zero, the image will be decoded to
     * its natural size. If one of the two is nonzero, that dimension will be
     * clamped and the other one will be set to preserve the image's aspect
     * ratio. If both width and height are nonzero, the image will be decoded to
     * be fit in the rectangle of dimensions width x height while keeping its
     * aspect ratio.
     *
     * @param url URL of the image
     * @param listener Listener to receive the decoded bitmap
     * @param maxWidth Maximum width to decode this bitmap to, or zero for none
     * @param maxHeight Maximum height to decode this bitmap to, or zero for
     * none
     * @param scaleType The ImageViews ScaleType used to calculate the needed image size.
     * @param decodeConfig Format to decode the bitmap to
     * @param errorListener Error listener, or null to ignore errors
     */
    public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, ScaleType scaleType, Config decodeConfig, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_IMAGE_TIMEOUT_MS, DEFAULT_IMAGE_MAX_RETRIES,
                DEFAULT_IMAGE_BACKOFF_MULT));
        mListener = listener;
        mDecodeConfig = decodeConfig;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mScaleType = scaleType;
    }


    /**
     * For API compatibility with the pre-ScaleType variant of the constructor. Equivalent to
     * the normal constructor with {@code ScaleType.CENTER_INSIDE}.
     */
    /*
     * 默认 ScaleType 参数：ScaleType.CENTER_INSIDE
     */
    @Deprecated
    public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, Config decodeConfig, Response.ErrorListener errorListener) {
        this(url, listener, maxWidth, maxHeight, ScaleType.CENTER_INSIDE, decodeConfig,
                errorListener);
    }


    /*
     * 覆写 getPriority()
     * 修改优先级为：Priority.LOW （ 最低 ）
     */
    @Override public Priority getPriority() {
        return Priority.LOW;
    }


    /**
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary Maximum size of the primary dimension (i.e. width for
     * max width), or zero to maintain aspect ratio with secondary
     * dimension
     * @param maxSecondary Maximum size of the secondary dimension, or zero to
     * maintain aspect ratio with primary dimension
     * @param actualPrimary Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     * @param scaleType The ScaleType used to calculate the needed image size.
     */
    /*
     * maxPrimary：最大宽度 或 最大高度，如果 getResizedDimension(...) 用于计算宽度，则传宽度；反之，传高度
     * maxSecondary：最大高度 或 最大宽度，如果 getResizedDimension(...) 用于计算高度，则传高度；反之，传宽度
     * actualPrimary：Bitmap 的实际宽度 或 最大高度
     *                如果 getResizedDimension(...) 用于计算宽度，则传Bitmap 的实际宽度；反之，传Bitmap 的实际高度
     * actualSecondary：Bitmap 的实际高度
     *                如果 getResizedDimension(...) 用于计算高度，则传Bitmap 的实际高度；反之，传Bitmap 的实际宽度
     * ScaleType：ImageView 的 ScaleType（ CENTER_CROP, FIT_XY ... ），默认值：ScaleType.CENTER_INSIDE
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary, ScaleType scaleType) {

        // If no dominant value at all, just return the actual.
        /*
         * 没有设置 最大宽度 和 最大高度
         * 如果 getResizedDimension(...) 用于计算宽度，则返回 Bitmap 的实际宽度
         * 如果 getResizedDimension(...) 用于计算高度，则返回 Bitmap 的实际高度
         */
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        /*
         * 如果设置了 ScaleType.FIT_XY，则进行填充
         */
        if (scaleType == ScaleType.FIT_XY) {
            /*
             * 如果 getResizedDimension(...) 用于计算宽度，返回 Bitmap 的实际宽度
             * 如果 getResizedDimension(...) 用于计算高度，返回 Bitmap 的实际高度
             */
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            /*
             * 如果 getResizedDimension(...) 用于计算宽度，返回 设置好的 最大宽度
             * 如果 getResizedDimension(...) 用于计算高度，返回 设置好的 最大高度
             */
            return maxPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        /*
         * 1. 如果 getResizedDimension(...) 用于计算宽度：
         *      那么，这里缺少 最大宽度
         *      则，计算 最大高度 / Bitmap 实际高度 的比例
         *      然后，再让 Bitmap 实际宽度 * 上述比例 = 这样返回 需要的宽度
         *
         * 2. 如果 getResizedDimension(...) 用于计算高度：
         *      那么，这里缺少 最大高度
         *      则，计算 最大宽度 / Bitmap 实际宽度 的比例
         *      然后，再让 Bitmap 实际高度 * 上述比例 = 这样返回 需要的高度
         */
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        /*
         * 1. 如果 getResizedDimension(...) 用于计算宽度：
         *      那么，这里缺少 最大高度
         *      返回 最大宽度 即可
         *
         * 2. 如果 getResizedDimension(...) 用于计算高度：
         *      那么，这里缺少 最大宽度
         *      返回 最大高度 即可
         */
        if (maxSecondary == 0) {
            return maxPrimary;
        }

        /*****************************************
         * 以下为：最大宽度 和 最大高度 都存在的情况下 *
         *****************************************/

        /*
         * 1. 如果 getResizedDimension(...) 用于计算宽度：
         *      那么，这么计算的是：Bitmap 实际高度 / Bitmap 实际宽度
         *      得到的是 实际高宽比
         *
         * 2. 如果 getResizedDimension(...) 用于计算高度：
         *      那么，这么计算的是：Bitmap 实际宽度 / Bitmap 实际高度
         *      得到的是 实际宽高比
         */
        double ratio = (double) actualSecondary / (double) actualPrimary;

        /*
         * 1. 如果 getResizedDimension(...) 用于计算宽度：
         *      那么 最大宽度，记录在 resized
         *
         * 2. 如果 getResizedDimension(...) 用于计算高度：
         *      那么 最大高度，记录在 resized
         */
        int resized = maxPrimary;

        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect ratio.

        /*
         * 如果设置了 ScaleType.CENTER_CROP
         */
        if (scaleType == ScaleType.CENTER_CROP) {
            /*
             * ScaleType.CENTER_CROP 调整
             *
             * 1. 如果 getResizedDimension(...) 用于计算宽度：
             *      resized = 最大宽度
             *      ratio = 高宽比
             *      <1>.最大宽度 * 实际高宽比 < 最大高度：表示合格（ 因为还小于 最大高度 ），根据
             *          实际宽高比调整的 高宽比 合格
             *          重新 利用 resized 复制上：调整宽度 = （ 最大高度 ）maxSecondary / ratio ( 合格的高宽比 )
             *      <2.>不合格，就代表超过最大高度，直接返回 最大宽度 即可
             *
             * 2. 如果 getResizedDimension(...) 用于计算高度：
             *      resized = 最大高度
             *      ratio = 宽高比
             *      <1>.最大高度 * 实际宽高比 < 最大宽度：表示合格（ 因为还小于 最大宽度 ），根据
             *          实际宽高比调整的 宽高比 合格
             *          重新 利用 resized 复制上：调整宽度 = （ 最大宽度 ）maxSecondary / ratio ( 合格的宽高比 )
             *      <2.>不合格，就代表超过最大宽度，直接返回 最高高度 即可
             */
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        /*
         * 除了 ScaleType.FIT_XY 和 ScaleType.CENTER_CROP 以外的默认调整
         *
         *
         * 1. 如果 getResizedDimension(...) 用于计算宽度：
         *      resized = 最大宽度
         *      ratio = 高宽比
         *      <1>.最大宽度 * 实际高宽比 > 最大高度：表示合格（ 因为大于 最大高度 ），根据
         *          实际高宽比 调整的 高宽比 合格
         *          重新 利用 resized 复制上：调整宽度 = （ 最大高度 ）maxSecondary / ratio ( 合格的高宽比 )
         *      <2.>不合格，就代表超过最大高度，直接返回 最大宽度 即可
         *
         * 2. 如果 getResizedDimension(...) 用于计算高度：
         *      resized = 最大高度
         *      ratio = 宽高比
         *      <1>.最大高度 * 实际宽高比 > 最大宽度：表示合格（ 因为大于 最大宽度 ），根据
         *          实际宽高比 调整的 宽高比 合格
         *          重新 利用 resized 复制上：调整宽度 = （ 最大宽度 ）maxSecondary / ratio ( 合格的宽高比 )
         *      <2.>不合格，就代表超过最大宽度，直接返回 最高高度 即可
         */
        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }


    @Override protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        // Serialize all decode on a global lock to reduce concurrent heap usage.
        // 解析 ImageRequest 的网络请求这块进行加锁，避免 OOM
        synchronized (sDecodeLock) {
            try {
                return doParse(response);
            } catch (OutOfMemoryError e) {
                // 发生 OOM，返回一个只带有 error 的 Response
                VolleyLog.e("Caught OOM for %d byte image, url=%s", response.data.length, getUrl());
                return Response.error(new ParseError(e));
            }
        }
    }


    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     */
    /*
     * 这里开始真正解析 网络请求结果（ 响应 ）NetworkResponse
     * NetworkResponse -> Response<Bitmap> 的转换
     */
    private Response<Bitmap> doParse(NetworkResponse response) {
        // 拿到结果数据
        byte[] data = response.data;
        // 实例化一个 BitmapFactory.Options 用于解析数据成 Bitmap
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        // 如果缺少 最大宽度 和 最大高度
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            // 设置 BitmapFactory.Options.Config
            decodeOptions.inPreferredConfig = mDecodeConfig;
            // 开始生成 Bitmap
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.

            /**
             * 如果存在 最大宽度 和 最大高度
             */

            /*
             * 由于一下四行操作只是想拿到这个 Bitmap 的自身的实际宽高，但又不想申请一个 Bitmap 内存
             * 可以设置 inJustDecodeBounds = true，只是读图片大小，不申请 Bitmap 内存
             * BitmapFactory.decodeByteArray(...) 的时候，就会 return null
             * 此时，再通过 BitmapFactory.Options 内被设置好的 outWidth 和 outHeight
             * 拿到该 Bitmap 的自身的实际宽高
             */
            decodeOptions.inJustDecodeBounds = true;
            /*
             * 这里正常是 Bitmap bitmap = BitmapFactory.decodeByteArray(...)
             * 但是由于上面设置了 inJustDecodeBounds = true
             * 这里一定返回 null
             * 但是这里为 BitmapFactory.Options 设置了 Bitmap 的数据参数
             * 所以下面能拿到 Bitmap 的实际宽高
             */
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            // 记录该 Bitmap 的实际宽度
            int actualWidth = decodeOptions.outWidth;
            // 记录该 Bitmap 的实际高度
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.

            // 根据 最宽高、Bitmap 实际宽高 以及 ImageView.ScaleType，计算出 需求宽度
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight, actualWidth, actualHeight,
                    mScaleType);
            // 根据 最宽高、Bitmap 实际宽高 以及 ImageView.ScaleType，计算出 需求高度
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth, actualHeight,
                    actualWidth, mScaleType);

            // Decode to the nearest power of two scaling factor.
            // 关闭 inJustDecodeBounds，因为以下要进行真实的 Bitmap 内存申请
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
            // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;

            /*
             * 计算缩放比例
             * 如果 BitmapFactory.Options.inSampleSize = 4，那么宽高为 原 Bitmap 的 1/4
             */
            decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth,
                    desiredHeight);
            // 解析出 测试 Bitmap
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.

            /*
             * 上面解析出的 测试 Bitmap
             * 如果 测试 Bitmap 的宽高 超过 需求宽高
             * 重新 根据 需求宽高 再拿 测试 Bitmap 解析一遍
             * 得到 最终 Bitmap 返回
             */
            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                    tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }
        /*
         * 没有解析出的 Bitmap，调用错误回调，回调一个 ParseError
         * 有解析出的 Bitmap，调用 解析结果数据 的回调接口，回调 Bitmap
         */
        if (bitmap == null) {
            return Response.error(new ParseError(response));
        } else {
            return Response.success(bitmap, HttpHeaderParser.parseCacheHeaders(response));
        }
    }


    /*
     * 解析结果数据 的回调接口 开始 传递解析结果数据
     */
    @Override protected void deliverResponse(Bitmap response) {
        mListener.onResponse(response);
    }


    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    /*
     * 这是 Volley 的提供的算法
     * 根据 实际 Bitmap 宽高、需求宽高 计算得出 BitmapFactory.Options.inSampleSize
     * 如果 BitmapFactory.Options.inSampleSize = 4，那么宽高为 原 Bitmap 的 1/4
     *
     */
    // Visible for testing.
    static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }
}
