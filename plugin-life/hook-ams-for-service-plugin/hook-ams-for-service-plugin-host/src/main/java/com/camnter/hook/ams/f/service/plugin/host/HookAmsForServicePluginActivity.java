package com.camnter.hook.ams.f.service.plugin.host;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * @author CaMnter
 */

public class HookAmsForServicePluginActivity extends BaseAppCompatActivity
    implements View.OnClickListener {

    View startFirstText;
    View startSecondText;
    View stopFirstText;
    View stopSecondText;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.startFirstText = this.findViewById(R.id.start_first_text);
        this.startSecondText = this.findViewById(R.id.start_second_text);
        this.stopFirstText = this.findViewById(R.id.stop_first_text);
        this.stopSecondText = this.findViewById(R.id.stop_second_text);
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        this.startFirstText.setOnClickListener(this);
        this.startSecondText.setOnClickListener(this);
        this.stopFirstText.setOnClickListener(this);
        this.stopSecondText.setOnClickListener(this);
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
            case R.id.start_first_text:
                this.startFirstText.setEnabled(false);
                this.startService(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.ams.f.service.plugin.plugin",
                        "com.camnter.hook.ams.f.service.plugin.plugin.FirstService")));
                this.startFirstText.setEnabled(true);
                break;
            case R.id.start_second_text:
                startSecondText.setEnabled(false);
                this.startService(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.ams.f.service.plugin.plugin",
                        "com.camnter.hook.ams.f.service.plugin.plugin.SecondService")));
                startSecondText.setEnabled(true);
                break;
            case R.id.stop_first_text:
                stopFirstText.setEnabled(false);
                this.stopService(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.ams.f.service.plugin.plugin",
                        "com.camnter.hook.ams.f.service.plugin.plugin.FirstService")));
                stopFirstText.setEnabled(true);
                break;
            case R.id.stop_second_text:
                stopSecondText.setEnabled(false);
                this.stopService(new Intent().setComponent(
                    new ComponentName("com.camnter.hook.ams.f.service.plugin.plugin",
                        "com.camnter.hook.ams.f.service.plugin.plugin.SecondService")));
                stopSecondText.setEnabled(true);
                break;
        }
    }

}
