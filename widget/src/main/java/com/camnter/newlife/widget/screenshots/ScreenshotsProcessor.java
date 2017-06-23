package com.camnter.newlife.widget.screenshots;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.camnter.newlife.widget.R;
import com.camnter.utils.BitmapUtils;
import com.camnter.utils.DeviceUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static com.camnter.utils.BitmapUtils.decodeBitmapAndCompressedByWidth;

/**
 * @author CaMnter
 */

public class ScreenshotsProcessor {

    private static final String TAG = ScreenshotsProcessor.class.getSimpleName();
    private static final float EXPECT_HEIGHT_SCREEN_RATIO = 0.4453125f;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService processExecutor;

    private WeakReference<Context> contextWeakReference;

    private final String cacheDir;
    private FutureTask<byte[]> shareBitmapTask;


    public static ScreenshotsProcessor newInstance(@NonNull final Context context) {
        return new ScreenshotsProcessor(context);
    }


    private ScreenshotsProcessor(@NonNull final Context context) {
        this.processExecutor = Executors.newCachedThreadPool();
        this.contextWeakReference = new WeakReference<>(context);

        this.cacheDir = context.getApplicationContext().getFilesDir().getAbsolutePath() +
            "/screenshots/";
        File cacheDirFile = new File(this.cacheDir);
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }
    }


    public void process(@NonNull final String imagePath,
                        final int expectWidthDp,
                        @NonNull final WrapperRunnable wrapperRunnable) {
        this.processExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int originalWidth;
                int originalHeight;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);
                originalWidth = options.outWidth;
                originalHeight = options.outHeight;

                Context context;
                if ((context = contextWeakReference.get()) == null) {
                    Log.e(TAG, "[process]   [context] = null");
                    return;
                }

                int expectWidth = DeviceUtils.dp2px(context, expectWidthDp);

                // --> > > > 异步构造一张用于分享的图
                shareBitmapTask = new FutureTask<>(
                    new ShareBitmapCallable(context, imagePath, originalWidth));
                processExecutor.submit(shareBitmapTask);

                Log.d(TAG, "[process]\n   [startSmallBitmap]\n   [threadId] = " +
                    Thread.currentThread().getId() + "\n   [time] = " +
                    SystemClock.elapsedRealtime());

                // 缩放到准确的小图
                Bitmap accurateBitmap = BitmapUtils.decodeBitmapAndCompressedByWidth(imagePath,
                    expectWidth);

                // 按照业务比例裁剪
                int expectHeight = (int) (accurateBitmap.getHeight() * EXPECT_HEIGHT_SCREEN_RATIO);
                Bitmap expectBitmap = Bitmap.createBitmap(accurateBitmap, 0, 0, expectWidth,
                    expectHeight);
                // 回收上一张无用的 bitmap
                accurateBitmap.recycle();

                // 保存
                final String expectPath = cacheDir + UUID.randomUUID();
                BitmapUtils.save(expectPath, expectBitmap);
                // 回收上一张无用的 bitmap
                expectBitmap.recycle();
                Log.d(TAG, "[process]\n   [endSmallBitmap]\n   [threadId] = " +
                    Thread.currentThread().getId() + "\n   [time] = " +
                    SystemClock.elapsedRealtime());

                byte[] shareImageByte = null;
                try {
                    // < < < <-- 阻塞子线程，等待分享图片 task 任务结束
                    shareImageByte = shareBitmapTask.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "[process]   [expectPath] = " + expectPath);
                wrapperRunnable.setShareImageByte(shareImageByte);
                wrapperRunnable.setExpectPath(expectPath);
                mainHandler.post(wrapperRunnable);
            }
        });
    }


    public abstract static class WrapperRunnable implements Runnable {

        private String expectPath;
        private byte[] shareImageByte;


        void setShareImageByte(@Nullable final byte[] shareImageByte) {
            this.shareImageByte = shareImageByte;
        }


        void setExpectPath(@NonNull final String expectPath) {
            this.expectPath = expectPath;
        }


        @Override
        public void run() {
            this.onProcessSuccess(this.shareImageByte, this.expectPath);
        }


        protected abstract void onProcessSuccess(@NonNull final byte[] shareImageByte, @NonNull final String expectPath);

    }


    @WorkerThread
    private static class ShareBitmapCallable implements Callable<byte[]> {

        @DrawableRes
        private static final int QR_CODE_RESOURCE = R.drawable.img_extra;
        private static final int SHARE_BITMAP_MAX_WIDTH = 630;
        // 60 padding px
        private static final int SHARE_BITMAP_PADDING = 60;

        private final WeakReference<Context> contextWeakReference;

        private final String imagePath;
        private final int originalWidth;


        private ShareBitmapCallable(@NonNull final Context context,
                                    @NonNull final String imagePath,
                                    final int originalWidth) {
            this.contextWeakReference = new WeakReference<>(context);
            this.imagePath = imagePath;
            this.originalWidth = originalWidth;
        }


        @Override
        public byte[] call() throws Exception {
            Log.d(TAG, "[ShareBitmapCallable]\n   [startFutureTask]\n   [threadId] = " +
                Thread.currentThread().getId() + "\n   [time] = " + SystemClock.elapsedRealtime());

            Context context;
            if ((context = contextWeakReference.get()) == null) {
                Log.e(TAG, "[ShareBitmapCallable]   [context] = null");
                return null;
            }

            int originalNavigationHeight = this.getHeightWithNavigation(context) -
                this.getHeightWithoutNavigation(context);

          /*
           * 大于 630 则压缩，压到 630
           */
            int navigationHeight;
            final boolean compressCreate = this.originalWidth > SHARE_BITMAP_MAX_WIDTH;
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap accurateWidthBitmap;
            // 大于 630 则压缩
            if (compressCreate) {
                // 压缩到最接近 宽度 630 的 bitmap
                options.inSampleSize = (this.originalWidth / SHARE_BITMAP_MAX_WIDTH) + 1;
                Bitmap smallBitmap = BitmapFactory.decodeFile(imagePath, options);

                // 压缩到 630
                accurateWidthBitmap = BitmapUtils.getBitmapCompressedByWidth(smallBitmap,
                    SHARE_BITMAP_MAX_WIDTH);
                smallBitmap.recycle();
                float heightRatio = ((float) this.originalWidth / SHARE_BITMAP_MAX_WIDTH);
                navigationHeight = (int) (((float) originalNavigationHeight / heightRatio) + 0.5);
            } else {
                accurateWidthBitmap = BitmapFactory.decodeFile(this.imagePath, options);
                navigationHeight = originalNavigationHeight;
            }

            // 生成无 Navigation 的图
            Bitmap withoutNavigationBitmap = Bitmap.createBitmap(
                accurateWidthBitmap,
                0,
                0,
                accurateWidthBitmap.getWidth(),
                accurateWidthBitmap.getHeight() - navigationHeight
            );
            accurateWidthBitmap.recycle();

            // 加载和内容一样宽度的 二维码图
            Bitmap qrBitmap = decodeBitmapAndCompressedByWidth(context,
                QR_CODE_RESOURCE,
                SHARE_BITMAP_MAX_WIDTH);

            // 合成图片
            final int expectWidth = withoutNavigationBitmap.getWidth() + SHARE_BITMAP_PADDING * 2;
            final int expectHeight = withoutNavigationBitmap.getHeight() + qrBitmap.getHeight() +
                SHARE_BITMAP_PADDING * 2;
            Bitmap expectBitmap = Bitmap.createBitmap(expectWidth, expectHeight,
                Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(expectBitmap);
            canvas.drawColor(0xff000000);
            canvas.drawBitmap(withoutNavigationBitmap, (float) SHARE_BITMAP_PADDING,
                (float) SHARE_BITMAP_PADDING, null);
            canvas.drawBitmap(qrBitmap, (float) SHARE_BITMAP_PADDING,
                (float) (SHARE_BITMAP_PADDING + withoutNavigationBitmap.getHeight()), null);

            withoutNavigationBitmap.recycle();
            qrBitmap.recycle();

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();

            // bitmap2Bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            expectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            expectBitmap.recycle();

            System.gc();

            Log.d(TAG, "[ShareBitmapCallable]\n   [endFutureTask]\n   [threadId] = " +
                Thread.currentThread().getId() + "\n   [time] = " + SystemClock.elapsedRealtime());
            return outputStream.toByteArray();
        }


        /**
         * @param context context
         * @return 屏幕高度，无 navigation
         */
        int getHeightWithoutNavigation(@NonNull final Context context) {
            WindowManager windowManager = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }


        /**
         * @param context context
         * @return 屏幕高度，有 navigation
         */
        int getHeightWithNavigation(@NonNull final Context context) {
            WindowManager windowManager = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(realDisplayMetrics);
            } else {
                try {
                    Method getRealMetrics = display.getClass().getDeclaredMethod("getRealMetrics");
                    getRealMetrics.setAccessible(true);
                    getRealMetrics.invoke(display, realDisplayMetrics);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return realDisplayMetrics.heightPixels;
        }

    }

}
