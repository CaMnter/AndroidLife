package com.camnter.newlife.ui.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.RemoteViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.BitmapUtils;

/**
 * Description：RemoteViewsActivity
 * Created by：CaMnter
 */

public class RemoteViewsActivity extends BaseAppCompatActivity {
    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_remote_views;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    @OnClick(R.id.remote_views_normal) public void onClick() {
        // 普通 Notification 的创建
        Notification notification = new NotificationCompat.Builder(this)
            .setLargeIcon(BitmapUtils.drawableToBitmap(
                ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_camnter,
                    this.getTheme())))
            .setSmallIcon(R.drawable.ic_send_light_small)
            .setTicker("Save you from anything")
            .setWhen(System.currentTimeMillis()).build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        // 跳转到 RemoteViewsActivity
        Intent intent = new Intent(this, RemoteViewsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[] { intent },
            PendingIntent.FLAG_UPDATE_CURRENT);
        // 自定义 Notification 的布局
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
            R.layout.notification_remote_normal);
        // 特定的 set 方法去修改规定的 View
        remoteViews.setImageViewResource(R.id.remote_icon, R.drawable.ic_camnter);
        remoteViews.setTextViewText(R.id.remote_text, "Save you from anything");
        // Notification 设置上 RemoteViews 和 PendingIntent
        notification.contentView = remoteViews;
        notification.contentIntent = pendingIntent;
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(2, notification);
    }

}
