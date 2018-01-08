package com.camnter.multi.classloader.plugin.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

/**
 * @author CaMnter
 */

public class MultiClassLoaderPluginActivity extends BaseAppCompatActivity {

    final Handler handler = new Handler(Looper.getMainLooper());

    View startOneText;
    View startTwoText;

    private ClassLoader pathClassLoader;


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
        final MultiClassLoaderApplication multiClassLoaderApplication
            = (MultiClassLoaderApplication) MultiClassLoaderPluginActivity.this.getApplication();
        this.pathClassLoader = multiClassLoaderApplication.getClassLoader();
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        this.startOneText = this.findViewById(R.id.start_one_text);
        this.startOneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOneText.setEnabled(false);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // plugin activity
                            final Intent intent = new Intent(MultiClassLoaderPluginActivity.this,
                                pathClassLoader.loadClass(
                                    "com.camnter.multi.classloader.plugin.one.PluginOneActivity"));
                            startActivity(intent);
                        } catch (Exception e) {
                            ToastUtils.show(MultiClassLoaderPluginActivity.this, e.getMessage(),
                                Toast.LENGTH_LONG);
                            e.printStackTrace();
                        } finally {
                            startOneText.setEnabled(true);
                        }
                    }
                });
            }
        });

        this.startTwoText = this.findViewById(R.id.start_two_text);
        this.startTwoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTwoText.setEnabled(false);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // plugin activity
                            final Intent intent = new Intent(MultiClassLoaderPluginActivity.this,
                                pathClassLoader.loadClass(
                                    "com.camnter.multi.classloader.plugin.two.PluginTwoActivity"));
                            startActivity(intent);
                        } catch (Exception e) {
                            ToastUtils.show(MultiClassLoaderPluginActivity.this, e.getMessage(),
                                Toast.LENGTH_LONG);
                            e.printStackTrace();
                        } finally {
                            startTwoText.setEnabled(true);
                        }
                    }
                });
            }
        });
    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {

    }

}
