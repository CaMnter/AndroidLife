package com.camnter.newlife.core;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.camnter.newlife.utils.ToastUtils;

/**
 * Description：BaseAppCompatActivity
 * Created by：CaMnter
 * Time：2016-01-02 15:02
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getLayoutId() != 0) this.setContentView(this.getLayoutId());
        this.initViews(savedInstanceState);
        this.initData();
        this.initListeners();
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    protected abstract int getLayoutId();

    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState);

    /**
     * Initialize the View of the listener
     */
    protected abstract void initListeners();

    /**
     * Initialize the Activity data
     */
    protected abstract void initData();


    /**
     * Find the view by id
     *
     * @param id id
     * @param <V> V
     * @return V
     */
    @SuppressWarnings("unchecked") protected <V extends View> V findView(int id) {
        return (V) this.findViewById(id);
    }


    /**
     * @param intent The intent to start.
     * @throws ActivityNotFoundException
     * @see {@link #startActivity(Intent, Bundle)}
     * @see #startActivityForResult
     */
    @Override public void startActivity(Intent intent) {
        super.startActivity(intent);
        //        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    /**
     * @param intent The intent to start.
     * @param options Additional options for how the Activity should be started.
     * See {@link Context#startActivity(Intent, Bundle)
     * Context.startActivity(Intent, Bundle)} for more details.
     * @throws ActivityNotFoundException
     * @see {@link #startActivity(Intent)}
     * @see #startActivityForResult
     */
    @Override public void startActivity(Intent intent, Bundle options) {
        super.startActivity(intent, options);
        //        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    /**
     * @param intent The intent to start.
     * @param requestCode If >= 0, this code will be returned in
     * onActivityResult() when the activity exits.
     * @param options Additional options for how the Activity should be started.
     * See {@link Context#startActivity(Intent, Bundle)
     * Context.startActivity(Intent, Bundle)} for more details.
     * @throws ActivityNotFoundException
     * @see #startActivity
     */
    @Override public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        //        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    /**
     * @param intent intent
     * @param requestCode requestCode
     */
    @Override public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        //        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    /**
     * Call this when your activity is done and should be closed.  The
     * ActivityResult is propagated back to whoever launched you via
     * onActivityResult().
     */
    @Override public void finish() {
        super.finish();
        //        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    /*********
     * Toast *
     *********/

    public void showToast(String msg) {
        this.showToast(msg, Toast.LENGTH_SHORT);
    }


    public void showToast(String msg, int duration) {
        if (msg == null) return;
        if (duration == Toast.LENGTH_SHORT || duration == Toast.LENGTH_LONG) {
            ToastUtils.show(this, msg, duration);
        } else {
            ToastUtils.show(this, msg, ToastUtils.LENGTH_SHORT);
        }
    }


    public void showToast(int resId) {
        this.showToast(resId, Toast.LENGTH_SHORT);
    }


    public void showToast(int resId, int duration) {
        if (duration == Toast.LENGTH_SHORT || duration == Toast.LENGTH_LONG) {
            ToastUtils.show(this, resId, duration);
        } else {
            ToastUtils.show(this, resId, ToastUtils.LENGTH_SHORT);
        }
    }

    /*********
     * Toast *
     *********/

}
