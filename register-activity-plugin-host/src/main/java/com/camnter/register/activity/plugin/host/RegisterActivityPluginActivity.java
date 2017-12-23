package com.camnter.register.activity.plugin.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * @author CaMnter
 */

public class RegisterActivityPluginActivity extends BaseAppCompatActivity {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return 0;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // plugin activity
                    SmartApplication smartApplication
                        = (SmartApplication) RegisterActivityPluginActivity.this.getApplication();
                    final Intent intent = new Intent(RegisterActivityPluginActivity.this,
                        smartApplication
                            .getDexClassLoader()
                            .loadClass(
                                "com.camnter.register.activity.plugin.plugin.PluginActivity"));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
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
