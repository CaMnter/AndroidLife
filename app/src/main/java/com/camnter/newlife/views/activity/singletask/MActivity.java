package com.camnter.newlife.views.activity.singletask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：MActivity
 * Created by：CaMnter
 * Time：2015-09-23 15:29
 */
public class MActivity extends BaseAppCompatActivity implements View.OnClickListener {
    private Button startBT;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_m;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.startBT = (Button) this.findViewById(R.id.start_bt);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.startBT.setOnClickListener(this);
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
            case R.id.start_bt: {
                this.startActivity(new Intent(this, SingleTaskActivity.class));
                break;
            }
        }
    }
}
