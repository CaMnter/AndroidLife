package com.camnter.newlife.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.camnter.newlife.R;
import com.camnter.newlife.component.broadcastreceiver.DynamicReceiver;
import com.camnter.newlife.core.BaseAppCompatActivity;
import java.util.UUID;

/**
 * Description：DynamicReceiverActivity
 * Created by：CaMnter
 * Time：2015-11-22 21:43
 */
public class DynamicReceiverActivity extends BaseAppCompatActivity implements View.OnClickListener {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_dynamic_broadcast_receiver;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        DynamicReceiver.register(this);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.findViewById(R.id.dynamic_broadcast_receiver_bt).setOnClickListener(this);
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
            case R.id.dynamic_broadcast_receiver_bt:
                Intent intent = new Intent(DynamicReceiver.INTENT_ACTION);
                intent.putExtra(DynamicReceiver.DYNAMIC_MESSAGE, UUID.randomUUID().toString());
                this.sendBroadcast(intent);
                break;
        }
    }
}
