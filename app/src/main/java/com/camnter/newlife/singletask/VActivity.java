package com.camnter.newlife.singletask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.camnter.newlife.R;


/**
 * Description：
 * Created by：CaMnter
 * Time：2015-09-23 15:29
 */
public class VActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_v);
        this.startBT = (Button) this.findViewById(R.id.start_bt);
        this.startBT.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_bt:{
                this.startActivity(new Intent(this,SingleTaskActivity.class));
                break;
            }
        }
    }
}
