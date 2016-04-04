package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment;

import android.annotation.SuppressLint;
import com.camnter.newlife.R;
import com.camnter.robotlegs4android.views.RobotlegsFragment;

/**
 * Description：RobotlegsFourthFragment
 * Created by：CaMnter
 * Time：2015-10-17 12:15
 */
public class RobotlegsFourthFragment extends RobotlegsFragment {

    private static RobotlegsFourthFragment instance;


    @SuppressLint("ValidFragment") private RobotlegsFourthFragment() {
    }


    public static RobotlegsFourthFragment getInstance() {
        if (instance == null) instance = new RobotlegsFourthFragment();
        return instance;
    }


    /**
     * Please set the fragment layout id
     * 请设置Fragment的布局Id
     */
    @Override public int getLayoutId() {
        return R.layout.robotlegs_fourth_fragment;
    }
}
