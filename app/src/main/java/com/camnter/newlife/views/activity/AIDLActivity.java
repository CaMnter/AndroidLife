package com.camnter.newlife.views.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.aidl.IPushMessage;
import com.camnter.newlife.aidl.PushMessageService;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：AIDLActivity
 * Created by：CaMnter
 * Time：2015-11-17 16:28
 */
public class AIDLActivity extends BaseAppCompatActivity {

    private static final String TAG = "AIDLActivity";

    private TextView aidlTV;

    private String pushMessage;
    private IPushMessage iPushMessage;
    private ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
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


        @Override public void onServiceDisconnected(ComponentName name) {
            AIDLActivity.this.iPushMessage = null;
        }
    };


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_aidl;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.aidlTV = (TextView) this.findViewById(R.id.aidl_tv);
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
        Intent intent = new Intent(this, PushMessageService.class);
        this.startService(intent);
        this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
    }


    @Override protected void onDestroy() {
        this.unbindService(this.connection);
        super.onDestroy();
    }
}
