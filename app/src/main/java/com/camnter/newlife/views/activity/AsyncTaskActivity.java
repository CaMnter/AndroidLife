package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.asynctask.ProgressBarAsyncTask;

/**
 * Description：AsyncTaskActivity
 * Created by：CaMnter
 * Time：2015-09-17 14:12
 */
public class AsyncTaskActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private TextView textview;
    private Button start;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_asynctask;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.textview = (TextView) this.findViewById(R.id.textview);
        this.progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        this.start = (Button) this.findViewById(R.id.start);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.start.setOnClickListener(this);
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
            case R.id.start: {
                ProgressBarAsyncTask asyncTask = new ProgressBarAsyncTask(this.progressBar,
                        this.textview);
                asyncTask.execute("%");
                break;
            }
        }
    }
}
