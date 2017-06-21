package com.camnter.newlife.widget.screenshots;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;
import com.camnter.utils.BitmapUtils;
import com.camnter.utils.DeviceUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);
                originalWidth = options.outWidth;

                Context context;
                if ((context = contextWeakReference.get()) == null) {
                    return;
                }
                int expectWidth = DeviceUtils.dp2px(context, expectWidthDp);

                // --> > > > 异步构造一张用于分享的图
                shareBitmapTask = new FutureTask<>(
                    new ShareBitmapCallable(imagePath, originalWidth));
                processExecutor.submit(shareBitmapTask);

                Log.d(TAG, "[process]\n   [startSmallBitmap]\n   [threadId] = " +
                    Thread.currentThread().getId() + "\n   [time] = " + SystemClock
                    .elapsedRealtime());
                // 加载宽度接近一张小图
                options.inJustDecodeBounds = false;
                options.inSampleSize = (originalWidth / expectWidth) + 1;
                Bitmap smallBitmap = BitmapFactory.decodeFile(imagePath, options);

                // 再一次缩放到准确的小图
                Bitmap accurateBitmap = BitmapUtils.getBitmapCompressedByWidth(smallBitmap,
                    expectWidth);
                // 回收上一张无用的 bitmap
                smallBitmap.recycle();

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
    private class ShareBitmapCallable implements Callable<byte[]> {

        private static final int SHARE_BITMAP_MAX_WIDTH = 720;

        private final String imagePath;
        private final int originalWidth;


        private ShareBitmapCallable(@NonNull final String imagePath, final int originalWidth) {
            this.imagePath = imagePath;
            this.originalWidth = originalWidth;
        }


        @Override
        public byte[] call() throws Exception {
            Log.d(TAG, "[ShareBitmapCallable]\n   [startFutureTask]\n   [threadId] = " +
                Thread.currentThread().getId() + "\n   [time] = " + SystemClock.elapsedRealtime());
      /*
       * 宽度压到 720
       */
            final boolean compressCreate = this.originalWidth > SHARE_BITMAP_MAX_WIDTH;
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap expectBitmap;
            if (compressCreate) {
                // 压缩到最接近 宽度 720 的 bitmap
                options.inSampleSize = (this.originalWidth / SHARE_BITMAP_MAX_WIDTH) + 1;
                Bitmap smallBitmap = BitmapFactory.decodeFile(imagePath, options);

                // 压缩到 720
                expectBitmap = BitmapUtils.getBitmapCompressedByWidth(smallBitmap,
                    SHARE_BITMAP_MAX_WIDTH);
                smallBitmap.recycle();
            } else {
                expectBitmap = BitmapFactory.decodeFile(this.imagePath, options);
            }
            // bitmap2Bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            expectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            expectBitmap.recycle();
            Log.d(TAG, "[ShareBitmapCallable]\n   [endFutureTask]\n   [threadId] = " +
                Thread.currentThread().getId() + "\n   [time] = " + SystemClock.elapsedRealtime());
            return outputStream.toByteArray();
        }

    }

}
