package com.camnter.newlife.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.camnter.newlife.R;

/**
 * Description：RemoteViewsAppWidgetProvider
 * Created by：CaMnter
 */

public class RemoteViewsAppWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "RemoteViewsAppWidgetProvider";
    public static final String ACTION = "com.camnter.newlife.widget.RemoteViewsAppWidgetProvider";


    public RemoteViewsAppWidgetProvider() {
        super();
    }


    @SuppressLint("LongLogTag") @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive : action = " + intent.getAction());

        // 这里判断是自己的 Action，做自己的事情，比如小工具被点击了要干啥，这里是做一个动画效果
        if (intent.getAction().equals(ACTION)) {
            Toast.makeText(context,
                "RemoteViewsAppWidgetProvider.Action = com.camnter.newlife.widget.RemoteViewsAppWidgetProvider",
                Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override public void run() {
                    Bitmap srcBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_camnter);
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    for (int i = 0; i < 37; i++) {
                        float degree = (i * 10) % 360;
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                            R.layout.appwidget);
                        remoteViews.setImageViewBitmap(R.id.appwidget_image,
                            rotateBitmap(srcBitmap, degree));
                        Intent clickIntent = new Intent();
                        clickIntent.setAction(ACTION);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                            clickIntent, 0);
                        remoteViews.setOnClickPendingIntent(R.id.appwidget_image, pendingIntent);
                        appWidgetManager.updateAppWidget(
                            new ComponentName(context, RemoteViewsAppWidgetProvider.class),
                            remoteViews);
                        SystemClock.sleep(60);
                    }

                }
            });
        }
    }


    /**
     * 每次窗口小部件被点击更新都调用一次该方法
     */
    @SuppressLint("LongLogTag") @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "onUpdate");
        final int counter = appWidgetIds.length;
        Log.i(TAG, "counter = " + counter);
        for (int appWidgetId : appWidgetIds) {
            this.onWidgetUpdate(context, appWidgetManager, appWidgetId);
        }

    }


    /**
     * 窗口小部件更新
     */
    @SuppressLint("LongLogTag") private void onWidgetUpdate(Context context,
                                                            AppWidgetManager appWidgetManger,
                                                            int appWidgetId) {

        Log.i(TAG, "appWidgetId = " + appWidgetId);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
            R.layout.appwidget);

        // "窗口小部件"点击事件发送的Intent广播
        Intent intentClick = new Intent();
        intentClick.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
            intentClick, 0);
        remoteViews.setOnClickPendingIntent(R.id.appwidget_image, pendingIntent);
        appWidgetManger.updateAppWidget(appWidgetId, remoteViews);
    }


    private Bitmap rotateBitmap(Bitmap srcBitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degree);
        return Bitmap.createBitmap(srcBitmap, 0, 0,
            srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }
}
