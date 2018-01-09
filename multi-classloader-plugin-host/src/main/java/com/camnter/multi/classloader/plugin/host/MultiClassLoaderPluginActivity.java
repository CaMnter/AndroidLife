package com.camnter.multi.classloader.plugin.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.multi.classloader.plugin.host.plugin.PluginInterface;

/**
 * @author CaMnter
 */

public class MultiClassLoaderPluginActivity extends BaseAppCompatActivity
    implements View.OnClickListener {

    final Handler handler = new Handler(Looper.getMainLooper());

    View startOneText;
    View startTwoText;
    View startThreeText;
    TextView threeMessageText;

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

        this.startOneText = this.findViewById(R.id.start_one_text);
        this.startTwoText = this.findViewById(R.id.start_two_text);
        this.startThreeText = this.findViewById(R.id.start_three_text);
        this.threeMessageText = (TextView) this.findViewById(R.id.three_message_text);
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        this.startOneText.setOnClickListener(this);
        this.startTwoText.setOnClickListener(this);
        this.startThreeText.setOnClickListener(this);
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
            case R.id.start_one_text:
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
                break;
            case R.id.start_two_text:
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
                break;
            case R.id.start_three_text:
                startThreeText.setEnabled(false);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Class<?> clazz = Class.forName(
                                "com.camnter.multi.classloader.plugin.three.PluginThreeImplement");
                            final PluginInterface pluginInterface
                                = (PluginInterface) clazz.newInstance();
                            final String pluginInfo = pluginInterface.getInfo();
                            final String message =
                                "[PluginThreeImplement ClassLoader] = " +
                                    clazz.getClassLoader() + "\n" +
                                    "[PluginInterface.class ClassLoader] = " +
                                    PluginInterface.class.getClassLoader() + "\n" +
                                    "[PluginInterface getInfo] = " +
                                    pluginInfo + "\n";
                            threeMessageText.setText(message);
                            ToastUtils.show(MultiClassLoaderPluginActivity.this,
                                "Success!\n" + pluginInfo,
                                Toast.LENGTH_LONG);
                        } catch (Exception e) {
                            threeMessageText.setText(e.toString());
                            ToastUtils.show(MultiClassLoaderPluginActivity.this, "Failure!",
                                Toast.LENGTH_LONG);
                            e.printStackTrace();
                        } finally {
                            startThreeText.setEnabled(true);
                        }
                    }
                });
                break;
        }
    }

}
