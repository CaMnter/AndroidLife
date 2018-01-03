package com.camnter.single.resources.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

/**
 * @author CaMnter
 */

public class SingleResourcesActivity extends BaseAppCompatActivity {

    final Handler handler = new Handler(Looper.getMainLooper());

    View startText;


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
        this.startText = this.findViewById(R.id.start_text);
        this.startText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startText.setEnabled(false);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // plugin activity
                            final SmartApplication smartApplication
                                = (SmartApplication) SingleResourcesActivity.this.getApplication();
                            final Intent intent = new Intent(SingleResourcesActivity.this,
                                smartApplication
                                    .getDexClassLoader()
                                    .loadClass(
                                        "com.camnter.load.plugin.resources.plugin.PluginActivity"));
                            startActivity(intent);
                        } catch (Exception e) {
                            ToastUtils.show(SingleResourcesActivity.this, e.getMessage(),
                                Toast.LENGTH_LONG);
                            e.printStackTrace();
                        } finally {
                            startText.setEnabled(true);
                        }
                    }
                });
            }
        });
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
