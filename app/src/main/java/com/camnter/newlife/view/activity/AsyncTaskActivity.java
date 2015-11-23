package com.camnter.newlife.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.utils.asynctask.ProgressBarAsyncTask;


/**
 * Description：AsyncTaskActivity
 * Created by：CaMnter
 * Time：2015-09-17 14:12
 */
public class AsyncTaskActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asynctask);
        this.textview = (TextView) this.findViewById(R.id.textview);
        this.progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        Button start = (Button) this.findViewById(R.id.start);
        start.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start: {
                ProgressBarAsyncTask asyncTask = new ProgressBarAsyncTask(this.progressBar, this.textview);
                asyncTask.execute("%");
                break;
            }
        }
    }
}
