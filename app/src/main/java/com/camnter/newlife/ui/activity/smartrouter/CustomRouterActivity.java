package com.camnter.newlife.ui.activity.smartrouter;

import android.os.Bundle;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.smartrouter.SmartRouters;

/**
 * @author CaMnter
 */

public class CustomRouterActivity extends BaseAppCompatActivity {

    public String exampleBoxedString;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_router_sample;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        SmartRouters.setFieldValue(this);
        ((TextView) this.findViewById(R.id.smart_router_sample_example_text))
            .setText("" + "exampleBoxedString = " + this.exampleBoxedString);
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
}
