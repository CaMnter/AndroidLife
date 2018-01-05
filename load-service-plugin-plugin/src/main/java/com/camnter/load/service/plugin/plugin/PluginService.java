package com.camnter.load.service.plugin.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author CaMnter
 */

public class PluginService extends Service {

    private static final int ACTIVITY_MESSAGE = 0x2331;
    private static final int PLUGIN_SERVICE_MESSAGE = 0x2332;

    private Messenger serviceMessenger;
    private Messenger activityMessenger;

    private HandlerThread handlerThread;


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.handlerThread = new HandlerThread(PluginService.class.getName() + " # HandlerThread");
        this.handlerThread.start();

        final Handler handler = new Handler(this.handlerThread.getLooper()) {
            /**
             * Subclasses must implement this to receive messages.
             */
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ACTIVITY_MESSAGE:
                        activityMessenger = msg.replyTo;
                        final Bundle bundle = msg.getData();
                        final String info = bundle == null
                                            ? null
                                            : bundle.getString("ACTIVITY_INFO");
                        final Message message = createRequestMessage(String.valueOf(info));
                        try {
                            activityMessenger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };
        this.serviceMessenger = new Messenger(handler);
    }


    private Message createRequestMessage(@NonNull final String info) {
        Message message = Message.obtain(null, PLUGIN_SERVICE_MESSAGE);
        final Bundle bundle = new Bundle();
        bundle.putString("SERVICE_INFO",
            "Plugin service response info: There is Plugin Service, >> \"" + info + "\"");

        message.setData(bundle);
        message.replyTo = this.activityMessenger;
        return message;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.serviceMessenger.getBinder();
    }


    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                this.handlerThread.quitSafely();
            } else {
                this.handlerThread.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
