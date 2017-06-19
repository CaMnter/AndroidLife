package com.camnter.newlife.widget.listener;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * // Requires Permission: android.permission.READ_EXTERNAL_STORAGE
 *
 * ScreenShotListenManager manager = ScreenShotListenManager.newInstance(context);
 *
 * manager.setListener(
 * -  new OnScreenShotListener() {
 * -      public void onShot(String imagePath) {
 * -          // do something
 * }
 * -  }
 * );
 *
 * manager.start();
 * ...
 * manager.stop();
 *
 * @author CaMnter
 */
public class ScreenshotsListener {

    private static final String TAG = "ScreenShotListener";

    /**
     * 最大轮询时间
     */
    private static final int MAX_POLLING_DECODE_TIME = 500;
    /**
     * 轮询间隔
     */
    private static final int POLLING_DECODE_STEP = 100;

    /**
     * 读取媒体数据库时需要读取的列
     */
    private static final String[] MEDIA_PROJECTIONS = {
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
    };

    /**
     * 读取媒体数据库时需要读取的列, 其中 WIDTH 和 HEIGHT 字段在 API 16 以后才有
     */
    private static final String[] MEDIA_PROJECTIONS_API_16 = {
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.WIDTH,
        MediaStore.Images.ImageColumns.HEIGHT,
    };

    /**
     * 可能存在的 截图 文件夹
     * 因为机型太多
     */
    private static final String[] KEYWORDS = {
        "screenshot", "screen_shot", "screen-shot", "screen shot",
        "screencapture", "screen_capture", "screen-capture", "screen capture",
        "screencap", "screen_cap", "screen-cap", "screen cap"
    };

    private Point screenRealSize;

    /**
     * 已回调过的路径
     */
    private final List<String> hasCallbackPaths = new ArrayList<>();

    private Context context;

    private OnScreenShotListener listener;

    private long startListenTime;

    /**
     * 内部存储器内容观察者
     */
    private MediaContentObserver internalObserver;

    /**
     * 外部存储器内容观察者
     */
    private MediaContentObserver externalObserver;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService processExecutor;

    private boolean isListen = true;


    @UiThread
    private ScreenshotsListener(@Nullable final Context context) {
        this.assertInMainThread();
        if (context == null) {
            throw new IllegalArgumentException(
                "[" + TAG + "]   [ScreenShotListener]   [Context] = null");
        }
        this.context = context;
        this.processExecutor = Executors.newCachedThreadPool();
        // 获取屏幕真实的分辨率
        if (screenRealSize == null) {
            screenRealSize = getRealScreenSize();
            if (screenRealSize != null) {
                Log.d(TAG, "[screenRealSize.x] = " + screenRealSize.x + "   [screenRealSize.y] = " +
                    screenRealSize.y);
            } else {
                Log.d(TAG, "[screenRealSize] = null");
            }
        }
    }


    public static ScreenshotsListener newInstance(@NonNull final Context context) {
        return new ScreenshotsListener(context);
    }


    public void setListen(final boolean listen) {
        this.isListen = listen;
    }


    /**
     * 启动监听
     */
    @UiThread
    public void start() {
        this.assertInMainThread();
        this.hasCallbackPaths.clear();
        this.startListenTime = System.currentTimeMillis();
        this.internalObserver = new MediaContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI,
            this.mainHandler);
        this.externalObserver = new MediaContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            this.mainHandler);
        this.context.getContentResolver().registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI,
            false,
            this.internalObserver
        );
        this.context.getContentResolver().registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            false,
            this.externalObserver
        );
    }


    @UiThread
    public void stop() {
        assertInMainThread();
        if (this.internalObserver != null) {
            try {
                this.context.getContentResolver().unregisterContentObserver(internalObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.internalObserver = null;
        }
        if (this.externalObserver != null) {
            try {
                this.context.getContentResolver().unregisterContentObserver(externalObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.externalObserver = null;
        }
        this.startListenTime = 0;
        this.hasCallbackPaths.clear();
    }


    @WorkerThread
    private void handleMediaContentChange(@NonNull final Uri contentUri) {
        Cursor cursor = null;
        try {
            // 数据改变时查询数据库中最后加入的一条数据
            cursor = this.context.getContentResolver().query(
                contentUri,
                Build.VERSION.SDK_INT < 16 ? MEDIA_PROJECTIONS : MEDIA_PROJECTIONS_API_16,
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
            );

            if (cursor == null) {
                Log.d(TAG, "[handleMediaContentChange]   [cursor] = null");
                return;
            }
            if (!cursor.moveToFirst()) {
                Log.d(TAG, "[handleMediaContentChange]   Cursor no data");
                return;
            }

            // 获取各列的索引
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);

            // 获取行数据
            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);
            // 处理获取到的第一行数据
            this.handleMediaRowData(data, dateTaken, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    /**
     * 处理获取到的一行数据
     */
    @WorkerThread
    private void handleMediaRowData(@NonNull final String data,
                                    final long dateTaken,
                                    @NonNull final Cursor cursor) {
        if (checkScreenShot(data, dateTaken, cursor)) {
            if (!checkCallback(data)) {
                // 切换到主线程
                this.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onShot(data);
                        }
                    }
                });
            }
        } else {
            // 如果在观察区间媒体数据库有数据改变，又不符合截屏规则
            int[] widthHeight = this.getWidthHeightByCursor(cursor);
            Log.w(TAG,
                "[handleMediaRowData]   Media content changed, but not screenshot:   \n[path] = " +
                    data
                    + "   \n[width] = " + widthHeight[0] + "   \n[height] = " + widthHeight[1] +
                    "   \n[date] = " +
                    dateTaken);
        }
    }


    private int[] getWidthHeightByCursor(@NonNull final Cursor cursor) {
        int widthIndex = -1;
        int heightIndex = -1;
        if (Build.VERSION.SDK_INT >= 16) {
            widthIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
            heightIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
        }
        return new int[] { widthIndex, heightIndex };
    }


    /**
     * 判断指定的数据行是否符合截屏条件
     */
    @WorkerThread
    private boolean checkScreenShot(@NonNull String data,
                                    final long dateTaken,
                                    @NonNull final Cursor cursor) {

        /*
         * 时间判断
         */
        // 如果加入数据库的时间在开始监听之前, 或者与当前时间相差大于 10 秒, 则认为当前没有截屏
        if (dateTaken < this.startListenTime ||
            (System.currentTimeMillis() - dateTaken) > 10 * 1000) {
            Log.e(TAG, "[checkScreenShot]   date invalid");
            return false;
        }

        /*
         * 路径判断
         */
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG, "[checkScreenShot]   data (path) = null");
            return false;
        }

        // 文件夹判断
        final String tempData = data.toLowerCase();
        boolean exists = false;
        for (String keyword : KEYWORDS) {
            if (tempData.contains(keyword)) {
                exists = true;
            }
        }
        if (!exists) {
            Log.e(TAG, "[checkScreenShot]   data (path) invalid");
            return false;
        }

        //  轮询 + 延迟策略 等待文件写入完成，才视为截屏事件
        this.pollingDecode(data, 0L);


        /*
         * 轮询 + 延迟 后，如果 文件生成了
         * 尺寸判断
         */
        int widthIndex = -1;
        int heightIndex = -1;
        if (Build.VERSION.SDK_INT >= 16) {
            widthIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
            heightIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
        }
        int width;
        int height;
        if (widthIndex >= 0 && heightIndex >= 0) {
            width = cursor.getInt(widthIndex);
            height = cursor.getInt(heightIndex);
        } else {
            // API 16 之前, 宽高要手动获取
            final Point size = this.getImageSize(data);
            width = size.x;
            height = size.y;
        }
        if (this.screenRealSize != null) {
            // 如果图片尺寸超出屏幕, 则认为当前没有截屏
            if (!((width <= this.screenRealSize.x && height <= this.screenRealSize.y) ||
                (height <= this.screenRealSize.x && width <= this.screenRealSize.y))) {
                Log.e(TAG, "[checkScreenShot]   screenRealSize invalid");
                return false;
            }
        }

        Log.d(TAG, "[checkScreenShot]   \n[path] = " + data + "   \n[width] = " + width +
            "   \n[height] = " + height
            + "   \n[date] = " + dateTaken);
        return true;
    }


    /**
     * 轮询 + 延迟 500ms
     *
     * @param data path
     */
    @WorkerThread
    private void pollingDecode(@NonNull final String data, long duration) {
        Log.d(TAG, "[pollingDecode]   [duration] = " + duration + "   [threadId] = " +
            Thread.currentThread().getId());
        while (!this.isFileAvailable(data) &&
            duration <= MAX_POLLING_DECODE_TIME) {
            try {
                Log.d(TAG,
                    "[isFileAvailable] = false   [data] = " + data + "   [duration] = " + duration);
                duration += POLLING_DECODE_STEP;
                Thread.sleep(POLLING_DECODE_STEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 判断文件是否存在
     *
     * @param data path
     * @return 文件是否存在
     */
    private boolean isFileAvailable(@NonNull final String data) {
        final Point point = this.getImageSize(data);
        if (point.x > 0 && point.y > 0) {
            Log.d(TAG,
                "[isFileAvailable] = true   [data] = " + data);
            return true;
        }
        return false;
    }


    private Point getImageSize(@NonNull final String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new Point(options.outWidth, options.outHeight);
    }


    /**
     * 判断是否已回调过, 某些手机 ROM 截屏一次会发出多次内容改变的通知
     * 删除一个图片也会发通知, 同时防止删除图片时误将上一张符合截屏规则的图片当做是当前截屏.
     */
    private boolean checkCallback(String imagePath) {
        if (this.hasCallbackPaths.contains(imagePath)) {
            Log.e(TAG, "[checkCallback]   imagePath invalid");
            return true;
        }
        // 大概缓存 不超过 20 条
        if (this.hasCallbackPaths.size() >= 20) {
            for (int i = 0; i < 5; i++) {
                this.hasCallbackPaths.remove(0);
            }
        }
        this.hasCallbackPaths.add(imagePath);
        return false;
    }


    /**
     * 获取屏幕分辨率
     */
    private Point getRealScreenSize() {
        Point screenSize = null;
        try {
            screenSize = new Point();
            WindowManager windowManager = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                defaultDisplay.getRealSize(screenSize);
            } else {
                try {
                    Method getRawWidth = Display.class.getMethod("getRawWidth");
                    Method getRawHeight = Display.class.getMethod("getRawHeight");
                    screenSize.set(
                        (Integer) getRawWidth.invoke(defaultDisplay),
                        (Integer) getRawHeight.invoke(defaultDisplay)
                    );
                } catch (Exception e) {
                    screenSize.set(defaultDisplay.getWidth(), defaultDisplay.getHeight());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenSize;
    }


    /**
     * 设置截屏监听器
     */
    public void setListener(OnScreenShotListener listener) {
        this.listener = listener;
    }


    public interface OnScreenShotListener {
        void onShot(@NonNull final String imagePath);
    }


    private void assertInMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            String methodMessage = null;
            if (elements != null && elements.length >= 4) {
                methodMessage = elements[3].toString();
            }
            throw new IllegalArgumentException(
                "[" + TAG + "]  Call the method must be in main thread: " + methodMessage);
        }
    }


    private class MediaContentObserver extends ContentObserver {

        private Uri contentUri;


        MediaContentObserver(Uri contentUri, Handler handler) {
            super(handler);
            this.contentUri = contentUri;
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.e(TAG,
                "[MediaContentObserver]   [onChange]   [contentUri] = " + contentUri.toString());
            if (isListen) {
                processExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        handleMediaContentChange(contentUri);
                    }
                });
            }
        }
    }

}
