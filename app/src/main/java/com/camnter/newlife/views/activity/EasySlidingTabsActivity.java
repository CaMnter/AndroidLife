package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.camnter.easyslidingtabs.widget.EasySlidingTabs;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.easyslidingtabs.EasySlidingTabsFragmentAdapter;
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
public class EasySlidingTabsActivity extends AppCompatActivity {

    private EasySlidingTabs easySlidingTabs;
    private ViewPager easyVP;
    private EasySlidingTabsFragmentAdapter adapter;
    List<Fragment> fragments;

    public static final String[] titles = {"一次元", "二次元", "三次元", "四次元"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.easy_sliding_tabs_activity);
        this.easySlidingTabs = (EasySlidingTabs) this.findViewById(R.id.easy_sliding_tabs);
        this.easyVP = (ViewPager) this.findViewById(R.id.easy_vp);
        this.initData();
    }

    private void initData() {
        this.fragments = new LinkedList<>();
        EasyFirstFragment first = EasyFirstFragment.getInstance();
        EasySecondFragment second = EasySecondFragment.getInstance();
        EasyThirdFragment third = EasyThirdFragment.getInstance();
        EasyFourthFragment fourth = EasyFourthFragment.getInstance();
        this.fragments.add(first);
        this.fragments.add(second);
        this.fragments.add(third);
        this.fragments.add(fourth);

        this.adapter = new EasySlidingTabsFragmentAdapter(this.getSupportFragmentManager(), titles, this.fragments);
        this.easyVP.setAdapter(this.adapter);
        this.easySlidingTabs.setViewPager(this.easyVP);
    }

}
