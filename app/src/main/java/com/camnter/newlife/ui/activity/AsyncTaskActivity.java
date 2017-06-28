package com.camnter.newlife.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.utils.asynctask.ProgressBarAsyncTask;
import com.camnter.smartsave.SmartSave;
import com.camnter.smartsave.annotation.Save;
import com.camnter.smartsave.annotation.SaveOnClick;
import com.camnter.utils.wrapper.SmartToastWrapper;

/**
 * Description：AsyncTaskActivity
 * Created by：CaMnter
 * Time：2015-09-17 14:12
 */
public class AsyncTaskActivity extends BaseAppCompatActivity {

    @Save(R.id.textview)
    TextView textview;
    @Save(R.id.progressBar)
    ProgressBar progressBar;

    private SmartToastWrapper smartToastWrapper;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_asynctask;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        SmartSave.save(this);
        this.smartToastWrapper = new SmartToastWrapper() {
            @Override
            protected Toast getToast() {
                return Toast.makeText(AsyncTaskActivity.this, "", Toast.LENGTH_LONG);
            }
        };
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


    @SaveOnClick({ R.id.start, R.id.progressBar })
    public void onSmartClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                ProgressBarAsyncTask asyncTask = new ProgressBarAsyncTask(this.progressBar,
                    this.textview);
                asyncTask.execute("%");
                break;
            case R.id.progressBar:
                this.smartToastWrapper.show(this.progressBar.getProgress() + "%");
                break;
        }
    }

}
