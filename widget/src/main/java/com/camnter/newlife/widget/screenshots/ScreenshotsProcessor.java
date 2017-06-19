package com.camnter.newlife.widget.screenshots;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import com.camnter.utils.BitmapUtils;
import com.camnter.utils.DeviceUtils;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * @author CaMnter
 */

public class ScreenshotsProcessor {

    private static final String TAG = ScreenshotsProcessor.class.getSimpleName();
    private static final float EXPECT_HEIGHT_SCREEN_RATIO = 0.4453125f;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService processExecutor;
    private final String cacheDir;
    private WeakReference<Context> contextWeakReference;


    private ScreenshotsProcessor(@NonNull final Context context) {
        this.processExecutor = Executors.newCachedThreadPool();
        this.contextWeakReference = new WeakReference<>(context);

        this.cacheDir = context.getFilesDir().getAbsolutePath() + "/screenshots/";
        File cacheDirFile = new File(this.cacheDir);
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }
    }


    public static ScreenshotsProcessor newInstance(@NonNull final Context context) {
        return new ScreenshotsProcessor(context);
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
                decodeFile(imagePath, options);
                originalWidth = options.outWidth;

                Context context;
                if ((context = contextWeakReference.get()) == null) {
                    return;
                }

                int expectWidth = DeviceUtils.dp2px(context, expectWidthDp);

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
                final String expectPath = cacheDir + UUID.randomUUID() + ".jpg";
                BitmapUtils.save(expectPath, expectBitmap);
                // 回收上一张无用的 bitmap
                expectBitmap.recycle();

                Log.d(TAG, "[expectPath] = " + expectPath);
                wrapperRunnable.setImagePath(imagePath);
                wrapperRunnable.setExpectPath(expectPath);
                mainHandler.post(wrapperRunnable);
            }
        });
    }


    public abstract static class WrapperRunnable implements Runnable {

        private String expectPath;
        private String imagePath;


        public void setImagePath(@NonNull final String imagePath) {
            this.imagePath = imagePath;
        }


        void setExpectPath(@NonNull final String expectPath) {
            this.expectPath = expectPath;
        }


        @Override
        public void run() {
            this.onProcessSuccess(this.imagePath, this.expectPath);
        }


        protected abstract void onProcessSuccess(@NonNull final String imagePath, @NonNull final String expectPath);

    }

}
