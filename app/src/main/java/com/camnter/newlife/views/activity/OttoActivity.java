package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.view.View;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ToastUtils;
import com.camnter.newlife.utils.otto.BusProvider;
import com.camnter.otto.Produce;
import com.camnter.otto.Subscribe;

/**
 * Description：OttoActivity
 * Created by：CaMnter
 * Time：2016-01-24 18:18
 */
public class OttoActivity extends BaseAppCompatActivity implements View.OnClickListener {

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.otto_send:
                BusProvider.getInstance().post(new OttoEvent("TextView"));
                break;
        }
    }


    @Override protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }


    /**
     * Dispatch onPause() to fragments.
     */
    @Override protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    /**
     * 订阅事件
     *
     * @param event event
     */
    @Subscribe public void hello(OttoEvent event) {
        ToastUtils.show(this, event.toString() +

                "\thello", ToastUtils.LENGTH_SHORT);
    }


    /**
     * 产生事件
     *
     * @return OttoEvent
     */
    @Produce public OttoEvent produceBySelf() {
        return new OttoEvent("Self");
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_otto;
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
        this.findViewById(R.id.otto_send).setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    private class OttoEvent {
        // No instances.
        private String msg;


        public OttoEvent(String msg) {
            this.msg = msg;
        }


        @Override public String toString() {
            return "OttoEvent{" +
                    "msg='" + msg + '\'' +
                    '}';
        }
    }
}
