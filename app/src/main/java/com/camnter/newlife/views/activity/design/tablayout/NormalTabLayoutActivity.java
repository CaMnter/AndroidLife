package com.camnter.newlife.views.activity.design.tablayout;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.tablayout.NormalTabLayoutFragmentAdapter;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutFirstFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutFourthFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutSecondFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutThirdFragment;

/**
 * Description：NormalTabLayoutActivity
 * Created by：CaMnter
 * Time：2015-10-24 11:55
 */
public class NormalTabLayoutActivity extends BaseAppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.tablayout_normal_activity;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.viewPager = this.findView(R.id.view_pager_vp);
        this.tabLayout = this.findView(R.id.tab_layout_tl);
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
        String[] tabTitles = { "一次元", "二次元", "三次元", "四次元" };
        Fragment[] fragments = { TabLayoutFirstFragment.getInstance(),
                TabLayoutSecondFragment.getInstance(), TabLayoutThirdFragment.getInstance(),
                TabLayoutFourthFragment.getInstance() };
        NormalTabLayoutFragmentAdapter fragmentAdapter = new NormalTabLayoutFragmentAdapter(
                this.getSupportFragmentManager(), fragments, tabTitles);
        this.viewPager.setAdapter(fragmentAdapter);
        this.tabLayout.setupWithViewPager(this.viewPager);
    }
}
