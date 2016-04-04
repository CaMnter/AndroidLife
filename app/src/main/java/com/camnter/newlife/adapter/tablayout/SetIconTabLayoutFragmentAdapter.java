package com.camnter.newlife.adapter.tablayout;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Description：SetIconTabLayoutFragmentAdapter
 * Created by：CaMnter
 * Time：2015-10-24 12:34
 */
public class SetIconTabLayoutFragmentAdapter extends FragmentPagerAdapter {

    private Context context;
    private int[] icons;
    private String[] tabTitles;
    private Fragment[] fragments;


    public SetIconTabLayoutFragmentAdapter(Context context, FragmentManager fm, Fragment[] fragments, String[] tabTitles, int[] icons) {
        super(fm);
        this.context = context;
        this.icons = icons;
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
        // 多返回三个空格，作为padding的作用，挤开图片
        return "  " + this.tabTitles[position];
    }
}
