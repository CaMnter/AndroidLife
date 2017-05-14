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
import android.util.SparseArray;
import java.util.ArrayList;

/**
 * @author CaMnter
 */

public class MessengerService extends Service {

    private static final String TAG = MessengerService.class.getSimpleName();

    public static final int ENCODING_REQUEST_TASK = 0x261;
    public static final String BUNDLE_KEY_PATH = "bundle_key_path";

    public static final int ARRAY_REQUEST_TASK = 0x271;
    public static final String BUNDLE_ARRAY = "bundle_array";
    public static final String BUNDLE_ARRAY_LIST = "bundle_array_list";

    public static final int SPARSE_PARCELABLE_BUNDLE_TASK = 0x281;
    public static final String BUNDLE_SPARSE_PARCELABLE_BUNDLE = "bundle_sparse_parcelable_array";
    public static final int BUNDLE_SPARSE_PARCELABLE_BUNDLE_PATH_INDEX = 0;
    public static final int BUNDLE_SPARSE_PARCELABLE_BUNDLE_ID_INDEX = 1;
    public static final String BUNDLE_SPARSE_PARCELABLE_BUNDLE_ARRAY_LIST
        = "bundle_sparse_parcelable_array_list";

    // CallBack

    public static final int ENCODING_REQUEST_TASK_CALL_BACK = 0x262;
    public static final String BUNDLE_REQUEST_CODE = "bundle_request_code";
    public static final int ENCODING_REQUEST_TASK_SUCCESS = 0x11;

    public static final int ARRAY_REQUEST_TASK_CALL_BACK = 0x272;
    public static final String CALLBACK_BUNDLE_ARRAY = "callback_bundle_array";
    public static final String CALLBACK_BUNDLE_ARRAY_LIST = "callback_bundle_array_list";

    public static final int SPARSE_PARCELABLE_BUNDLE_REQUEST_TASK_CALL_BACK = 0x282;
    public static final String CALLBACK_SPARSE_PARCELABLE_BUNDLE = "call_sparse_parcelable_array";

    private Messenger messenger;
    // 子进程内的主线程
    private Handler mainHandler;


    @Override public void onCreate() {
        super.onCreate();
        this.mainHandler = new Handler(Looper.getMainLooper()) {
            @Override public void handleMessage(Message msg) {
                Messenger replyMessenger;
                Bundle bundle;
                switch (msg.what) {
                    case ENCODING_REQUEST_TASK:
                        replyMessenger = msg.replyTo;
                        bundle = msg.getData();
                        String path = bundle.getString(BUNDLE_KEY_PATH);
                        Log.e(TAG, "[path] = " + path);
                        mainHandler.postDelayed(() -> {
                            Message replyMessage = Message.obtain(null,
                                ENCODING_REQUEST_TASK_CALL_BACK);
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
                    case ARRAY_REQUEST_TASK:
                        replyMessenger = msg.replyTo;
                        bundle = msg.getData();
                        String[] stringArray = bundle.getStringArray(BUNDLE_ARRAY);
                        ArrayList<String> stringList = bundle.getStringArrayList(
                            BUNDLE_ARRAY_LIST);
                        Log.e(TAG, "[stringArray] = " + stringArray.length);
                        Log.e(TAG, "[stringList] = " + stringList);
                        mainHandler.postDelayed(() -> {
                            Message replyMessage = Message.obtain(null,
                                ARRAY_REQUEST_TASK_CALL_BACK);
                            Bundle replyBundle = new Bundle();
                            replyBundle.putStringArray(CALLBACK_BUNDLE_ARRAY, stringArray);
                            replyBundle.putStringArrayList(CALLBACK_BUNDLE_ARRAY_LIST, stringList);
                            replyMessage.setData(replyBundle);
                            try {
                                replyMessenger.send(replyMessage);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }, 3333);
                        break;
                    case SPARSE_PARCELABLE_BUNDLE_TASK:
                        replyMessenger = msg.replyTo;
                        bundle = msg.getData();
                        SparseArray<Bundle> sparseArray = bundle.getSparseParcelableArray(
                            BUNDLE_SPARSE_PARCELABLE_BUNDLE);
                        if (sparseArray == null) {
                            Log.e(TAG, "[sparseArray] = null");
                            return;
                        }
                        Bundle pathBundle = sparseArray.get(
                            BUNDLE_SPARSE_PARCELABLE_BUNDLE_PATH_INDEX);
                        ArrayList<String> pathList = pathBundle.getStringArrayList(
                            BUNDLE_SPARSE_PARCELABLE_BUNDLE_ARRAY_LIST);
                        Bundle idBundle = sparseArray.get(BUNDLE_SPARSE_PARCELABLE_BUNDLE_ID_INDEX);
                        ArrayList<String> idList = idBundle.getStringArrayList(
                            BUNDLE_SPARSE_PARCELABLE_BUNDLE_ARRAY_LIST);
                        Log.e(TAG, "[idList] = " + idList);
                        Log.e(TAG, "[pathList] = " + pathList);
                        mainHandler.postDelayed(() -> {
                            Message replyMessage = Message.obtain(null,
                                SPARSE_PARCELABLE_BUNDLE_REQUEST_TASK_CALL_BACK);
                            Bundle replyBundle = new Bundle();
                            replyBundle.putSparseParcelableArray(CALLBACK_SPARSE_PARCELABLE_BUNDLE,
                                sparseArray);
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


    @Override public int onStartCommand(Intent intent,
                                        int flags, int startId) {
        Log.e(TAG, "[onStartCommand]  [intent] = " + intent.getAction());
        Log.e(TAG, "[onStartCommand]  [flags] = " + flags);
        Log.e(TAG, "[onStartCommand]  [startId] = " + startId);
        return super.onStartCommand(intent, flags, startId);
    }
}
