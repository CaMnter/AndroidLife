package com.camnter.newlife.widget.listener;

import android.app.ActivityManager;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
public class ScreenShotListener {

    private static final String TAG = "ScreenShotListener";

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


    private ScreenShotListener(@Nullable final Context context) {
        if (context == null) {
            throw new IllegalArgumentException(
                "[" + TAG + "]   [ScreenShotListener]   [Context] = null");
        }
        this.context = context;

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


    public static ScreenShotListener newInstance(@NonNull final Context context) {
        assertInMainThread();
        return new ScreenShotListener(context);
    }


    /**
     * 启动监听
     */
    public void start() {
        assertInMainThread();
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
            int widthIndex = -1;
            int heightIndex = -1;
            if (Build.VERSION.SDK_INT >= 16) {
                widthIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
                heightIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
            }

            // 获取行数据
            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);
            int width;
            int height;
            if (widthIndex >= 0 && heightIndex >= 0) {
                width = cursor.getInt(widthIndex);
                height = cursor.getInt(heightIndex);
            } else {
                // API 16 之前, 宽高要手动获取
                Point size = getImageSize(data);
                width = size.x;
                height = size.y;
            }

            // 处理获取到的第一行数据
            this.handleMediaRowData(data, dateTaken, width, height);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    private Point getImageSize(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new Point(options.outWidth, options.outHeight);
    }


    /**
     * 处理获取到的一行数据
     */
    private void handleMediaRowData(String data, long dateTaken, int width, int height) {
        if (checkScreenShot(data, dateTaken, width, height)) {
            Log.d(TAG, "[handleMediaRowData]   \n[path] = " + data + "   \n[width] = " + width +
                "   \n[height] = " + height
                + "   \n[date] = " + dateTaken);
            if (this.listener != null && !checkCallback(data)) {
                this.listener.onShot(data);
            }
        } else {
            // 如果在观察区间媒体数据库有数据改变，又不符合截屏规则，则输出到 log 待分析
            Log.w(TAG,
                "[handleMediaRowData]   Media content changed, but not screenshot:   \n[path] = " +
                    data
                    + "   \n[width] = " + width + "   \n[height] = " + height + "   \n[date] = " +
                    dateTaken);
        }
    }


    private boolean isScreenShotRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(
            Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos
            = activityManager.getRunningServices(200);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServiceInfos) {
            if (runningServiceInfo.process.equals("com.android.systemui:screenshot")) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断指定的数据行是否符合截屏条件
     */
    private boolean checkScreenShot(String data, long dateTaken, int width, int height) {

        // 截图进程判断
        if (isScreenShotRunning(this.context)) {
            Log.e(TAG, "[isScreenShotRunning] = false");
            return false;
        }

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
         * 尺寸判断
         */
        if (this.screenRealSize != null) {
            // 如果图片尺寸超出屏幕, 则认为当前没有截屏
            if (!((width <= this.screenRealSize.x && height <= this.screenRealSize.y) ||
                (height <= this.screenRealSize.x && width <= this.screenRealSize.y))) {
                Log.e(TAG, "[checkScreenShot]   screenRealSize invalid");
                return false;
            }
        }

        /*
         * 路径判断
         */
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG, "[checkScreenShot]   data (path) = null");
            return false;
        }
        data = data.toLowerCase();

        // 截屏判断
        for (String keyword : KEYWORDS) {
            if (data.contains(keyword)) {
                return true;
            }
        }
        Log.e(TAG, "[checkScreenShot]   data (path) invalid");

        // TODO 轮询 + 延迟策略
        return false;
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
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    screenSize.set(
                        (Integer) mGetRawW.invoke(defaultDisplay),
                        (Integer) mGetRawH.invoke(defaultDisplay)
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


    private static void assertInMainThread() {
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
            handleMediaContentChange(contentUri);
        }
    }

}