package com.camnter.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description：BitmapUtils
 * Created by：CaMnter
 * Time：2016-02-25 22:23
 */
public class BitmapUtils {

    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;
    private static final Canvas sCanvas = new Canvas();


    private BitmapUtils() {

    }


    public static int dp2Px(int dp) {
        return Math.round(dp * DENSITY);
    }


    /**
     * 通过 View 创建 Bitmap
     *
     * @param view view
     * @return Bitmap
     */
    public static Bitmap createBitmapFromView(View view) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        view.clearFocus();
        Bitmap bitmap = createBitmapSafely(view.getWidth(), view.getHeight(),
            Bitmap.Config.ARGB_8888, 1);
        if (bitmap != null) {
            synchronized (sCanvas) {
                Canvas canvas = sCanvas;
                canvas.setBitmap(bitmap);
                view.draw(canvas);
                canvas.setBitmap(null);
            }
        }
        return bitmap;
    }


    /**
     * 安全地创建 Bitmap
     *
     * @param width width
     * @param height height
     * @param config config
     * @param retryCount retryCount
     * @return Bitmap
     */
    public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }


    /**
     * Drawable to Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) ((BitmapDrawable) drawable).getBitmap();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = createBitmapSafely(width, height, Bitmap.Config.ARGB_8888, 1);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * 压缩图片
     */
    @SuppressWarnings("deprecation")
    public static Bitmap getBitmapCompressed(String pathName, float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        BitmapFactory.decodeFile(pathName, newOpts);

        newOpts.inJustDecodeBounds = false;

        int be = Math.min((int) (newOpts.outWidth / width), (int) (newOpts.outHeight / height));

        newOpts.inSampleSize = be;// 设置采样率

        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;// 该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

        Bitmap bitmap = BitmapFactory.decodeFile(pathName, newOpts);
        // return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
        // 其实是无效的,大家尽管尝试
        return bitmap;
    }


    public static Bitmap getBitmap(String path) {
        try {
            return BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bitmap getBitmap(InputStream is) {
        try {
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bitmap getBitmapCompressedByWidth(Bitmap bgimage, double newWidth) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleWidth);
        return Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
    }


    public static Bitmap getBitmapCompressedByHeight(Bitmap bgimage, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleHeight, scaleHeight);
        return Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
    }


    public static Bitmap rotate(Bitmap originalBitmap, float alpha) {
        if (originalBitmap == null) return null;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        return Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);
    }


    public static boolean save(String fileName, Bitmap bitmap) {
        if (fileName == null || bitmap == null) {
            return false;
        }
        boolean savedSuccessfully = false;
        OutputStream os = null;
        File imageFile = new File(fileName);
        File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
        try {
            if (!imageFile.getParentFile().exists()) {
                imageFile.getParentFile().mkdirs();
            }
            os = new BufferedOutputStream(new FileOutputStream(tmpFile));
            savedSuccessfully = bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (savedSuccessfully && tmpFile != null && !tmpFile.renameTo(imageFile)) {
                savedSuccessfully = false;
            }
            if (!savedSuccessfully) {
                tmpFile.delete();
            }
        }
        return savedSuccessfully;
    }


    /**
     * @param imagePath 图片路径
     * @param expectWidth 期望宽度
     * @return 期望 Bitmap
     */
    public static Bitmap decodeBitmapAndCompressedByWidth(@NonNull final String imagePath,
                                                           final int expectWidth) {
        int originalWidth;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        originalWidth = options.outWidth;

        // 加载宽度接近一张小图
        options.inJustDecodeBounds = false;
        options.inSampleSize = (originalWidth / expectWidth) + 1;
        Bitmap smallBitmap = BitmapFactory.decodeFile(imagePath, options);

        // 再一次缩放到准确的小图
        Bitmap accurateBitmap = BitmapUtils.getBitmapCompressedByWidth(smallBitmap, expectWidth);
        // 回收上一张无用的 bitmap
        smallBitmap.recycle();
        return accurateBitmap;
    }


    /**
     * @param context context
     * @param drawableRes 图片资源
     * @param expectWidth 期望宽度
     * @return 期望 Bitmap
     */
    public static Bitmap decodeBitmapAndCompressedByWidth(@NonNull final Context context,
                                                           @DrawableRes final int drawableRes,
                                                           final int expectWidth) {
        int originalWidth;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), drawableRes);
        originalWidth = options.outWidth;

        Bitmap accurateBitmap;
        if (originalWidth > expectWidth) {
            // 加载宽度接近一张小图
            options.inJustDecodeBounds = false;
            options.inSampleSize = (originalWidth / expectWidth) + 1;
            Bitmap smallBitmap = BitmapFactory.decodeResource(context.getResources(), drawableRes,
                options);

            // 再一次缩放到准确的小图
            accurateBitmap = BitmapUtils.getBitmapCompressedByWidth(smallBitmap, expectWidth);
            // 回收上一张无用的 bitmap
            smallBitmap.recycle();
        } else {
            // 加载原图
            accurateBitmap = BitmapFactory.decodeResource(context.getResources(), drawableRes);
        }
        return accurateBitmap;
    }

}
