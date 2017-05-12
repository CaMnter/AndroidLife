package com.camnter.newlife.utils.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author CaMnter
 */

public class MessengerService extends Service {

    private static final String TAG = MessengerService.class.getSimpleName();

    public static final int ENCODING_REQUEST_TASK = 0x261;
    public static final String BUNDLE_KEY_PATH = "bundle_key_path";

    public static final int ENCODING_REQUEST_TASK_CALL_BACK = 0x262;
    public static final String BUNDLE_REQUEST_CODE = "bundle_request_code";
    public static final int ENCODING_REQUEST_TASK_SUCCESS = 0x1;
    public static final int ENCODING_REQUEST_TASK_FAILURE = 0x0;

    private Messenger messenger;
    // 子进程内的主线程
    private Handler mainHandler;


    @Override public void onCreate() {
        super.onCreate();
        this.mainHandler = new Handler(Looper.getMainLooper()) {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ENCODING_REQUEST_TASK:
                        Messenger replyMessenger = msg.replyTo;
                        Bundle bundle = msg.getData();
                        String path = bundle.getString(BUNDLE_KEY_PATH);
                        Log.e(TAG, "[path] = " + path);
                        mainHandler.postDelayed(() -> {
                            Message replyMessage = Message.obtain(null,
                                ENCODING_REQUEST_TASK_CALL_BACK);
                            Log.e(TAG, "[postDelayed]  [replyMessage] = " + replyMessage);
                            Log.e(TAG, "[postDelayed]  [replyMessenger] = " + replyMessenger);
                            Bundle replyBundle = new Bundle();
                            replyBundle.putInt(BUNDLE_REQUEST_CODE, ENCODING_REQUEST_TASK_SUCCESS);
                            replyMessage.setData(replyBundle);
                            try {
                                replyMessenger.send(replyMessage);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }, 3333);
                        break;
                }
            }
        };
        this.messenger = new Messenger(this.mainHandler);
    }


    @Nullable @Override public IBinder onBind(Intent intent) {
        return this.messenger.getBinder();
    }

}
