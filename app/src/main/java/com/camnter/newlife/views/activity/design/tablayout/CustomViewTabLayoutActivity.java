package com.camnter.newlife.views.activity.design.tablayout;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.tablayout.CustomViewTabLayoutFragmentAdapter;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutFirstFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutFourthFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutSecondFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutThirdFragment;

/**
 * Description：CustomViewTabLayoutActivity
 * Created by：CaMnter
 * Time：2015-10-24 11:55
 */
public class CustomViewTabLayoutActivity extends BaseAppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.tablayout_image_span_activity;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.viewPager = (ViewPager) this.findViewById(R.id.view_pager_vp);
        this.tabLayout = (TabLayout) this.findViewById(R.id.tab_layout_tl);
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
        int[] icons = { R.mipmap.icon_clean, R.mipmap.icon_remark, R.mipmap.icon_time,
                R.mipmap.icon_feedback };
        String[] tabTitles = { "一次元", "二次元", "三次元", "四次元" };
        Fragment[] fragments = { TabLayoutFirstFragment.getInstance(),
                TabLayoutSecondFragment.getInstance(), TabLayoutThirdFragment.getInstance(),
                TabLayoutFourthFragment.getInstance() };
        CustomViewTabLayoutFragmentAdapter fragmentAdapter = new CustomViewTabLayoutFragmentAdapter(
                this.getSupportFragmentManager(), fragments);
        this.viewPager.setAdapter(fragmentAdapter);
        this.tabLayout.setupWithViewPager(this.viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_icon_tab_layout, null);
            TextView tabTV = (TextView) view.findViewById(R.id.tab_layout_title_tv);
            ImageView tabLeftIV = (ImageView) view.findViewById(R.id.tab_layout_title_left_iv);
            ImageView tabRightIV = (ImageView) view.findViewById(R.id.tab_layout_title_right_iv);
            tabTV.setText(tabTitles[i]);
            tabLeftIV.setImageResource(icons[i]);
            tabRightIV.setImageResource(icons[i]);
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab == null) continue;
            tab.setCustomView(view);
        }
    }
}
