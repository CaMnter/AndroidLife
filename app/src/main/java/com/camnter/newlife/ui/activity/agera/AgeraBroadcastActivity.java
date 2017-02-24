package com.camnter.newlife.ui.activity.agera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.utils.agera.AgeraBroadcastObservable;
import com.google.android.agera.Updatable;
import java.util.UUID;

/**
 * Description：AgeraBroadcastActivity
 * Created by：CaMnter
 * Time：2016-05-31 22:37
 */
public class AgeraBroadcastActivity extends BaseAppCompatActivity
    implements Updatable, View.OnClickListener {

    private static final String AGERA_BROADCAST_ACTION = "agera";

    @BindView(R.id.send_text) TextView sendText;
    @BindView(R.id.send_button) Button sendButton;

    private AgeraBroadcastObservable observable;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_agera_broadcast;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        this.observable = new AgeraBroadcastObservable(this, AGERA_BROADCAST_ACTION);
        this.observable.addUpdatable(this);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.sendButton.setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    /**
     * Called when an event has occurred.
     */
    @SuppressLint("SetTextI18n") @Override public void update() {
        sendText.setText("AgeraBroadcastObservable: update() -> " + UUID.randomUUID().toString());
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:
                this.sendBroadcast(new Intent(AGERA_BROADCAST_ACTION));
                break;
        }
    }
}
