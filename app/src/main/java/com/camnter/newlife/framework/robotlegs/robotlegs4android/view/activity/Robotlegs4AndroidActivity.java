package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.activity;

import android.os.Bundle;
import com.camnter.newlife.R;
import com.camnter.robotlegs4android.views.RobotlegsFragmentActivity;

public class Robotlegs4AndroidActivity extends RobotlegsFragmentActivity {

    /**
     * Please set the fragment layout id
     * 请设置Fragment的布局Id
     */
    @Override public int getLayoutId() {
        return R.layout.activity_robotlegs;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
