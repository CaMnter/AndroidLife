package com.camnter.newlife.ui.activity.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.utils.ToastUtils;
import com.camnter.newlife.utils.ipc.MessengerService;
import java.util.ArrayList;

import static com.camnter.newlife.utils.ipc.MessengerService.BUNDLE_SPARSE_PARCELABLE_BUNDLE;
import static com.camnter.newlife.utils.ipc.MessengerService.BUNDLE_SPARSE_PARCELABLE_BUNDLE_ARRAY_LIST;
import static com.camnter.newlife.utils.ipc.MessengerService.BUNDLE_SPARSE_PARCELABLE_BUNDLE_ID_INDEX;
import static com.camnter.newlife.utils.ipc.MessengerService.BUNDLE_SPARSE_PARCELABLE_BUNDLE_PATH_INDEX;
import static com.camnter.newlife.utils.ipc.MessengerService.CALLBACK_BUNDLE_ARRAY;
import static com.camnter.newlife.utils.ipc.MessengerService.CALLBACK_BUNDLE_ARRAY_LIST;
import static com.camnter.newlife.utils.ipc.MessengerService.CALLBACK_SPARSE_PARCELABLE_BUNDLE;

/**
 * @author CaMnter
 */

public class MessengerActivity extends BaseAppCompatActivity {

    private static final String TAG = MessengerActivity.class.getSimpleName();

    private Messenger sendMessenger;
    private ServiceConnection connection;

    private Messenger serviceCallBackMessenger;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_messenger;
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


    @Override protected void onDestroy() {
        this.unbindService(this.connection);
        super.onDestroy();
    }


    @OnClick({ R.id.messenger_send_text, R.id.messenger_send_array_text,
                 R.id.messenger_send_sparse_array })
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.messenger_send_text:
                this.requestMessengerService("https://www.camnter.com");
                break;
            case R.id.messenger_send_array_text:
                this.requestMessengerService(new String[] { "Save", "you", "from", "anything" },
                    new ArrayList<String>() {
                        {
                            this.add("Save");
                            this.add("you");
                            this.add("from");
                            this.add("anything");
                        }
                    });
                break;
            case R.id.messenger_send_sparse_array:
                this.requestMessengerService(
                    new ArrayList<String>() {
                        {
                            this.add("https://www.camnter.com/1");
                            this.add("https://www.camnter.com/2");
                            this.add("https://www.camnter.com/3");
                            this.add("https://www.camnter.com/4");
                        }
                    },
                    new ArrayList<String>() {
                        {
                            this.add("id: 2233");
                            this.add("id: 2333");
                        }
                    }
                );
                break;
        }
    }


    private void requestMessengerService(@NonNull final String path) {
        if (this.connection == null) {
            this.initCallbackMessenger();
            this.connection = new ServiceConnection() {
                @Override public void onServiceConnected(ComponentName name, IBinder service) {
                    sendMessenger = new Messenger(service);
                    try {
                        sendMessenger.send(createRequestMessage("https://www.camnter.com"));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }


                @Override public void onServiceDisconnected(ComponentName name) {

                }
            };
            Intent intent = new Intent(this, MessengerService.class);
            intent.setAction("com.camnter.ipc.messenger");
            this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
        } else {
            try {
                this.sendMessenger.send(this.createRequestMessage(path));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private void requestMessengerService(@NonNull final String[] stringArray,
                                         @NonNull final ArrayList<String> arrayList) {
        if (this.connection == null) {
            this.initCallbackMessenger();
            this.connection = new ServiceConnection() {
                @Override public void onServiceConnected(ComponentName name, IBinder service) {
                    sendMessenger = new Messenger(service);
                    try {
                        sendMessenger.send(createRequestMessage(stringArray, arrayList));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }


                @Override public void onServiceDisconnected(ComponentName name) {

                }
            };
            Intent intent = new Intent(this, MessengerService.class);
            intent.setAction("com.camnter.ipc.messenger");
            this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
        } else {
            try {
                this.sendMessenger.send(this.createRequestMessage(stringArray, arrayList));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private void requestMessengerService(@NonNull final ArrayList<String> pathList,
                                         @NonNull final ArrayList<String> idList) {
        if (this.connection == null) {
            this.initCallbackMessenger();
            this.connection = new ServiceConnection() {
                @Override public void onServiceConnected(ComponentName name, IBinder service) {
                    sendMessenger = new Messenger(service);
                    try {
                        sendMessenger.send(createRequestMessage(pathList, idList));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }


                @Override public void onServiceDisconnected(ComponentName name) {

                }
            };
            Intent intent = new Intent(this, MessengerService.class);
            intent.setAction("com.camnter.ipc.messenger");
            this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
        } else {
            try {
                this.sendMessenger.send(this.createRequestMessage(pathList, idList));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private Message createRequestMessage(@NonNull final String path) {
        Message message = Message.obtain(null,
            MessengerService.ENCODING_REQUEST_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(MessengerService.BUNDLE_KEY_PATH,
            path);
        message.setData(bundle);

        message.replyTo = this.serviceCallBackMessenger;
        return message;
    }


    private Message createRequestMessage(@NonNull final ArrayList<String> pathList,
                                         @NonNull final ArrayList<String> idList) {
        Message message = Message.obtain(null,
            MessengerService.SPARSE_PARCELABLE_BUNDLE_TASK);
        Bundle bundle = new Bundle();

        Bundle pathBundle = new Bundle();
        pathBundle.putStringArrayList(BUNDLE_SPARSE_PARCELABLE_BUNDLE_ARRAY_LIST, pathList);
        Bundle idBundle = new Bundle();
        idBundle.putStringArrayList(BUNDLE_SPARSE_PARCELABLE_BUNDLE_ARRAY_LIST, idList);
        SparseArray<Bundle> sparseArray = new SparseArray<>();
        sparseArray.put(BUNDLE_SPARSE_PARCELABLE_BUNDLE_PATH_INDEX, pathBundle);
        sparseArray.put(BUNDLE_SPARSE_PARCELABLE_BUNDLE_ID_INDEX, idBundle);

        bundle.putSparseParcelableArray(BUNDLE_SPARSE_PARCELABLE_BUNDLE, sparseArray);

        message.setData(bundle);

        message.replyTo = this.serviceCallBackMessenger;
        return message;
    }


    private Message createRequestMessage(@NonNull final String[] stringArray,
                                         @NonNull final ArrayList<String> arrayList) {
        Message message = Message.obtain(null,
            MessengerService.ARRAY_REQUEST_TASK);
        Bundle bundle = new Bundle();
        bundle.putStringArray(MessengerService.BUNDLE_ARRAY, stringArray);
        bundle.putStringArrayList(MessengerService.BUNDLE_ARRAY_LIST, arrayList);
        message.setData(bundle);

        message.replyTo = this.serviceCallBackMessenger;
        return message;
    }


    private void initCallbackMessenger() {
        this.serviceCallBackMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override public void handleMessage(Message msg) {
                Bundle bundle;
                switch (msg.what) {
                    case MessengerService.ENCODING_REQUEST_TASK_CALL_BACK:
                        bundle = msg.getData();
                        ToastUtils.show(MessengerActivity.this, "[BUNDLE_REQUEST_CODE] = " +
                                bundle.getInt(MessengerService.BUNDLE_REQUEST_CODE),
                            Toast.LENGTH_LONG);
                        break;
                    case MessengerService.ARRAY_REQUEST_TASK_CALL_BACK:
                        bundle = msg.getData();
                        String[] stringArray = bundle.getStringArray(CALLBACK_BUNDLE_ARRAY);
                        ArrayList<String> stringList = bundle.getStringArrayList(
                            CALLBACK_BUNDLE_ARRAY_LIST);
                        String stringText = stringArray != null
                                            ? String.valueOf(stringArray.length)
                                            : "null";
                        ToastUtils.show(MessengerActivity.this, "[CALLBACK_BUNDLE_ARRAY] = " +
                                stringText + "  [CALLBACK_BUNDLE_ARRAY_LIST] = " +
                                stringList,
                            Toast.LENGTH_LONG);
                        break;
                    case MessengerService.SPARSE_PARCELABLE_BUNDLE_REQUEST_TASK_CALL_BACK:
                        bundle = msg.getData();
                        SparseArray<Bundle> sparseArray = bundle.getSparseParcelableArray(
                            CALLBACK_SPARSE_PARCELABLE_BUNDLE);
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
                        ToastUtils.show(MessengerActivity.this, "[pathList] = " +
                                pathList + "  [idList] = " +
                                idList,
                            Toast.LENGTH_LONG);
                        break;
                }
            }
        });
    }

}
