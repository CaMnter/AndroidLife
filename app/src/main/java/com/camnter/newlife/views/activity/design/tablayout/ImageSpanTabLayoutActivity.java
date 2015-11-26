package com.camnter.newlife.views.activity.design.tablayout;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.camnter.newlife.R;
import com.camnter.newlife.adapter.tablayout.ImageSpanTabLayoutFragmentAdapter;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutFirstFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutFourthFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutSecondFragment;
import com.camnter.newlife.views.fragment.tablayoutfragment.TabLayoutThirdFragment;


/**
 * Description：
 * Created by：CaMnter
 * Time：2015-10-24 11:55
 */
public class ImageSpanTabLayoutActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ImageSpanTabLayoutFragmentAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tablayout_image_span_activity);

        this.viewPager = (ViewPager) this.findViewById(R.id.view_pager_vp);
        this.tabLayout = (TabLayout) this.findViewById(R.id.tab_layout_tl);
        this.initData();
    }

    private void initData() {
        int[] icons = {R.mipmap.icon_msg_unread, R.mipmap.icon_remark, R.mipmap.icon_time, R.mipmap.icon_feedback};
        String[] tabTitles = {"一次元", "二次元", "三次元", "四次元"};
        Fragment[] fragments = {
                TabLayoutFirstFragment.getInstance(),
                TabLayoutSecondFragment.getInstance(),
                TabLayoutThirdFragment.getInstance(),
                TabLayoutFourthFragment.getInstance()
        };
        this.fragmentAdapter = new ImageSpanTabLayoutFragmentAdapter(this, this.getSupportFragmentManager(), fragments, tabTitles, icons);
        this.viewPager.setAdapter(this.fragmentAdapter);
        this.tabLayout.setupWithViewPager(this.viewPager);
    }

}
