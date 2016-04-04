package com.camnter.newlife.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.camnter.newlife.R;
import com.camnter.newlife.component.broadcastreceiver.StaticReceiver;
import com.camnter.newlife.core.BaseAppCompatActivity;
import java.util.UUID;

/**
 * Description：StaticReceiverActivity
 * Created by：CaMnter
 * Time：2015-11-22 21:15
 */
public class StaticReceiverActivity extends BaseAppCompatActivity implements View.OnClickListener {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_static_broadcast_receiver;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {

    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.findViewById(R.id.static_broadcast_receiver_bt).setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.static_broadcast_receiver_bt:
                Intent intent = new Intent(StaticReceiver.INTENT_ACTION);
                intent.putExtra(StaticReceiver.STATIC_MESSAGE, UUID.randomUUID().toString());
                this.sendBroadcast(intent);
                break;
        }
    }
}
