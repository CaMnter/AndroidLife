package com.camnter.newlife.component.broadcastreceiver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.Toast;
import com.camnter.newlife.R;

/**
 * Description：StaticReceiver
 * Created by：CaMnter
 * Time：2015-11-22 20:52
 */
public class StaticReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = "com.camnter.android.intent.static";
    public static final String STATIC_MESSAGE = "message";


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra(STATIC_MESSAGE);
        Intent data = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, data,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context).setContentTitle(
                "StaticBroadcastReceiver")
                                                                     .setContentText(message)
                                                                     .setSmallIcon(
                                                                             R.mipmap.ic_mm_normal)
                                                                     .setLargeIcon(
                                                                             BitmapFactory.decodeResource(
                                                                                     context.getResources(),
                                                                                     R.mipmap.ic_mm_normal))
                                                                     .setContentIntent(
                                                                             pendingIntent)
                                                                     .build();
        notificationManager.notify(206, notification);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
