package com.camnter.newlife.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.aidl.IPushMessage;
import com.camnter.newlife.aidl.PushMessageService;

/**
 * Description：
 * Created by：CaMnter
 * Time：2015-11-17 16:28
 */
public class AIDLActivity extends AppCompatActivity {

    private static final String TAG = "AIDLActivity";

    private TextView aidlTV;

    private String pushMessage;
    private IPushMessage iPushMessage;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AIDLActivity.this.iPushMessage = IPushMessage.Stub.asInterface(service);
            try {
                AIDLActivity.this.pushMessage = AIDLActivity.this.iPushMessage.onMessage();
                AIDLActivity.this.aidlTV.setText(AIDLActivity.this.pushMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            AIDLActivity.this.iPushMessage = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_aidl);
        this.aidlTV = (TextView) this.findViewById(R.id.aidl_tv);
        this.bindService(new Intent(this, PushMessageService.class), this.connection, Context.BIND_AUTO_CREATE);
    }

}
