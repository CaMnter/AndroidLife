package com.camnter.load.service.plugin.host;

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
import android.view.View;
import android.widget.Toast;

/**
 * @author CaMnter
 */

public class LoadServicePluginHostActivity extends BaseAppCompatActivity {

    private static final int ACTIVITY_MESSAGE = 0x2331;
    private static final int PLUGIN_SERVICE_MESSAGE = 0x2332;

    private Messenger serviceMessenger;
    private Messenger activityMessenger;
    private ServiceConnection connection;

    View startText;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.startText = this.findViewById(R.id.start_text);
        this.startText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startText.setEnabled(false);
                requestUploadService();
            }
        });
    }


    private void requestUploadService() {
        try {
            if (this.connection == null) {
                this.initCallbackMessenger();
                this.connection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        serviceMessenger = new Messenger(service);
                        try {
                            serviceMessenger.send(createRequestMessage());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        connection = null;
                    }
                };
                final Intent intent = new Intent(this,
                    Class.forName("com.camnter.load.service.plugin.plugin.PluginService"));
                intent.setAction("com.camnter.load.service.plugin");
                this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
            } else {
                try {
                    serviceMessenger.send(this.createRequestMessage());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Message createRequestMessage() {
        Message message = Message.obtain(null, ACTIVITY_MESSAGE);
        final Bundle bundle = new Bundle();
        bundle.putString("ACTIVITY_INFO", "There is Host Activity");

        message.setData(bundle);
        message.replyTo = this.activityMessenger;
        return message;
    }


    private void initCallbackMessenger() {
        this.activityMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                startText.setEnabled(true);
                final Bundle bundle = msg.getData();
                final String info = bundle == null ? null : bundle.getString("SERVICE_INFO");
                switch (msg.what) {
                    case PLUGIN_SERVICE_MESSAGE:
                        ToastUtils.show(LoadServicePluginHostActivity.this, String.valueOf(info),
                            Toast.LENGTH_LONG);
                        break;
                }
            }
        });

    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {

    }


    @Override
    protected void onDestroy() {
        if (this.connection != null) {
            this.unbindService(this.connection);
        }
        super.onDestroy();
    }

}
