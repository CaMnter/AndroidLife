package com.camnter.hook.loadedapk.classloader.host;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * @author CaMnter
 */

public class HookLoadedApkForActivityPluginActivity extends BaseAppCompatActivity
    implements View.OnClickListener {

    View startFirstText;
    View startSecondText;


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
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        this.startFirstText.setOnClickListener(this);
        this.startSecondText.setOnClickListener(this);
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
                try {
                    this.startActivity(new Intent().setComponent(
                        new ComponentName("com.camnter.hook.loadedapk.classloader.plugin",
                            "com.camnter.hook.loadedapk.classloader.plugin.FirstPluginActivity")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.startFirstText.setEnabled(true);
                break;
            case R.id.start_second_text:
                this.startSecondText.setEnabled(false);
                try {
                    this.startActivity(new Intent().setComponent(
                        new ComponentName("com.camnter.hook.loadedapk.classloader.plugin",
                            "com.camnter.hook.loadedapk.classloader.plugin.SecondPluginActivity")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.startSecondText.setEnabled(true);
                break;
        }
    }

}
