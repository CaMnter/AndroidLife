package com.camnter.newlife.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.camnter.newlife.R;
import com.camnter.newlife.component.broadcastreceiver.StaticReceiver;

import java.util.UUID;

/**
 * Description：StaticReceiverActivity
 * Created by：CaMnter
 * Time：2015-11-22 21:15
 */
public class StaticReceiverActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_static_broadcast_receiver);
        this.findViewById(R.id.static_broadcast_receiver_bt).setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.static_broadcast_receiver_bt:
                Intent intent = new Intent(StaticReceiver.INTENT_ACTION);
                intent.putExtra(StaticReceiver.STATIC_MESSAGE, UUID.randomUUID().toString());
                this.sendBroadcast(intent);
                break;
        }
    }
}
