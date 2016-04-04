package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.camnter.easyslidingtabs.widget.EasySlidingTabs;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.easyslidingtabs.EasySlidingTabsFragmentAdapter;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.views.fragment.easyslidingtabsfragment.EasyFirstFragment;
import com.camnter.newlife.views.fragment.easyslidingtabsfragment.EasyFourthFragment;
import com.camnter.newlife.views.fragment.easyslidingtabsfragment.EasySecondFragment;
import com.camnter.newlife.views.fragment.easyslidingtabsfragment.EasyThirdFragment;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：EasySlidingTabsActivity
 * Created by：CaMnter
 * Time：2015-10-17 12:02
 */
public class EasySlidingTabsActivity extends BaseAppCompatActivity {

    private EasySlidingTabs easySlidingTabs;
    private ViewPager easyVP;
    List<Fragment> fragments;

    public static final String[] titles = { "一次元", "二次元", "三次元", "四次元" };


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.easy_sliding_tabs_activity;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.easySlidingTabs = (EasySlidingTabs) this.findViewById(R.id.easy_sliding_tabs);
        this.easyVP = (ViewPager) this.findViewById(R.id.easy_vp);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    @Override protected void initData() {
        this.fragments = new LinkedList<>();
        EasyFirstFragment first = EasyFirstFragment.getInstance();
        EasySecondFragment second = EasySecondFragment.getInstance();
        EasyThirdFragment third = EasyThirdFragment.getInstance();
        EasyFourthFragment fourth = EasyFourthFragment.getInstance();
        this.fragments.add(first);
        this.fragments.add(second);
        this.fragments.add(third);
        this.fragments.add(fourth);

        EasySlidingTabsFragmentAdapter adapter = new EasySlidingTabsFragmentAdapter(
                this.getSupportFragmentManager(), titles, this.fragments);
        this.easyVP.setAdapter(adapter);
        this.easySlidingTabs.setViewPager(this.easyVP);
    }
}
