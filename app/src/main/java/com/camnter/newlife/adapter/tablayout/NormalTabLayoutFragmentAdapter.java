package com.camnter.newlife.adapter.tablayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Description：NormalTabLayoutFragmentAdapter
 * Created by：CaMnter
 * Time：2015-10-24 12:34
 */
public class NormalTabLayoutFragmentAdapter extends FragmentPagerAdapter {

    private String[] tabTitles;
    private Fragment[] fragments;


    public NormalTabLayoutFragmentAdapter(FragmentManager fm, Fragment[] fragments, String[] tabTitles) {
        super(fm);
        this.fragments = fragments;
        this.tabTitles = tabTitles;
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


    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Override public CharSequence getPageTitle(int position) {
        return this.tabTitles[position];
    }
}
