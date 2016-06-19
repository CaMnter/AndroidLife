package com.camnter.newlife.adapter.tablayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Description：CustomViewTabLayoutFragmentAdapter
 * Created by：CaMnter
 * Time：2015-10-24 12:34
 */
public class CustomViewTabLayoutFragmentAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments;


    public CustomViewTabLayoutFragmentAdapter(FragmentManager fm, Fragment[] fragments) {
        super(fm);
        this.fragments = fragments;
    }


    /**
     * Return the Fragment associated with a specified position.
     */
    @Override public Fragment getItem(int position) {
        return this.fragments[position];
    }


    /**
     * Return the number of views available.
     */
    @Override public int getCount() {
        return this.fragments.length;
    }
}
