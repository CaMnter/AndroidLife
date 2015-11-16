package com.camnter.newlife.robotlegs4android.view.fragment;

import com.camnter.newlife.R;
import com.camnter.robotlegs4android.views.RobotlegsFragment;


/**
 * Description：TabLayoutSecondFragment
 * Created by：CaMnter
 * Time：2015-10-17 12:15
 */
public class RobotlegsSecondFragment extends RobotlegsFragment {

    private static RobotlegsSecondFragment instance;

    private RobotlegsSecondFragment() {
    }

    public static RobotlegsSecondFragment getInstance() {
        if (instance == null) instance = new RobotlegsSecondFragment();
        return instance;
    }


    /**
     * Please set the fragment layout id
     * 请设置Fragment的布局Id
     *
     * @return
     */
    @Override
    public int getLayoutId() {
        return R.layout.robotlegs_second_fragment;
    }
}
