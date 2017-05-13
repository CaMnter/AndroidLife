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
import android.util.Log;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.utils.ipc.MessengerService;

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


    @OnClick(R.id.messenger_send_text) public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.messenger_send_text:
                this.requestMessengerService();
                break;
        }
    }


    private void requestMessengerService() {
        if (this.connection == null) {
            this.bindMessengerService();
            Intent intent = new Intent(this, MessengerService.class);
            intent.setAction("com.camnter.ipc.messenger");
            this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
        } else {
            try {
                this.sendMessenger.send(this.createRequestMessage());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private Message createRequestMessage() {
        Message message = Message.obtain(null,
            MessengerService.ENCODING_REQUEST_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(MessengerService.BUNDLE_KEY_PATH,
            "https://www.camnter.com");
        message.setData(bundle);

        message.replyTo = this.serviceCallBackMessenger;
        return message;
    }


    private void bindMessengerService() {
        Handler mainHandler = new Handler(Looper.getMainLooper()) {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessengerService.ENCODING_REQUEST_TASK_CALL_BACK:
                        Bundle bundle = msg.getData();
                        Log.e(TAG, "[BUNDLE_REQUEST_CODE] = " +
                            bundle.getInt(MessengerService.BUNDLE_REQUEST_CODE));
                        break;
                }
            }
        };
        this.serviceCallBackMessenger = new Messenger(mainHandler);

        this.connection = new ServiceConnection() {
            @Override public void onServiceConnected(ComponentName name, IBinder service) {
                sendMessenger = new Messenger(service);
                try {
                    sendMessenger.send(createRequestMessage());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }


            @Override public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

}
