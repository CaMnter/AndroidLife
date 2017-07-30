package com.camnter.newlife.ui.activity.smartrouter;

import android.os.Bundle;
import android.view.View;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.smartrouter.SmartRouters;

/**
 * @author CaMnter
 */

public class SmartRouterActivity extends BaseAppCompatActivity implements View.OnClickListener {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_router;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.findViewById(R.id.smart_router_simple).setOnClickListener(this);
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


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.smart_router_simple:
                SmartRouters.start(this,"routers://router-0x01");
                break;
        }
    }

}
