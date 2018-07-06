package com.camnter.newlife.ui.activity.asm;

import android.os.Bundle;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;

/**
 * @author CaMnter
 */

public class AsmActivity extends BaseAppCompatActivity {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_asm;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        ActivityTimeManger.onCreateStart(this);
        System.out.println("「AsmActivity」   「onCreate」");
        ActivityTimeManger.onCreateEnd(this);
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
