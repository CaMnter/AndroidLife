package com.camnter.newlife.ui.activity.hotfix;

import android.os.Bundle;
import android.view.View;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：HotFixActivity
 * Created by：CaMnter
 */

public final class HotFixActivity extends BaseAppCompatActivity {

    private FixCall fixCall;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_hot_fix;
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

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        this.fixCall = new FixCall();
    }


    public void onClick(View v) {
        this.showToast(this.fixCall.call());
    }
}
