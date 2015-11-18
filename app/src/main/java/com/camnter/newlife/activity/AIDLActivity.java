package com.camnter.newlife.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.aidl.IPushMessage;
import com.camnter.newlife.aidl.PushMessageService;

/**
 * Description：AIDLActivity
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
            /*
             * 这里的 service 不是 Binder对象
             * 而是 BinderProxy对象
             * 不能 直接转为Binder（ (Binder)service ），是错误的。
             */
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
        Intent intent = new Intent(this, PushMessageService.class);
        this.startService(intent);
        this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        this.unbindService(this.connection);
        super.onDestroy();
    }

}
