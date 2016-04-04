package com.camnter.newlife.views.fragment.tablayoutfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseFragment;

/**
 * Description：TabLayoutThirdFragment
 * Created by：CaMnter
 * Time：2015-10-17 12:15
 */
public class TabLayoutThirdFragment extends BaseFragment {

    private View self;

    private static TabLayoutThirdFragment instance;


    @SuppressLint("ValidFragment") private TabLayoutThirdFragment() {
    }


    public static TabLayoutThirdFragment getInstance() {
        if (instance == null) instance = new TabLayoutThirdFragment();
        return instance;
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.tablayout_third_fragment;
    }


    /**
     * Initialize the view in the layout
     *
     * @param self self
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(View self, Bundle savedInstanceState) {

    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }
}
