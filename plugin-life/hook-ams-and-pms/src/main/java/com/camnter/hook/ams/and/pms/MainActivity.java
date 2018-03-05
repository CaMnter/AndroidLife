package com.camnter.hook.ams.and.pms;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * @author CaMnter
 */

public class MainActivity extends BaseAppCompatActivity implements View.OnClickListener {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        try {
            AMSHooker.hookActivityManagerNative();
            PMSHooker.hookPackageManagerService(newBase);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.attachBaseContext(newBase);
        }
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.findViewById(R.id.ams_text).setOnClickListener(this);
        this.findViewById(R.id.pms_text).setOnClickListener(this);
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
            case R.id.ams_text:
                final Uri uri = Uri.parse("https://camnter.com");
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                this.startActivity(intent);
                break;
            case R.id.pms_text:
                this.getPackageManager().getInstalledApplications(0);
                break;
        }
    }

}
