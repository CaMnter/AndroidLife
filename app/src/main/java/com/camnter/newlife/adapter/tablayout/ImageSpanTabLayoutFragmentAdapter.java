package com.camnter.newlife.adapter.tablayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

/**
 * Description：ImageSpanTabLayoutFragmentAdapter
 * Created by：CaMnter
 * Time：2015-10-24 12:34
 */
public class ImageSpanTabLayoutFragmentAdapter extends FragmentPagerAdapter {

    private Context context;
    private int[] icons;
    private String[] tabTitles;
    private Fragment[] fragments;


    public ImageSpanTabLayoutFragmentAdapter(Context context, FragmentManager fm, Fragment[] fragments, String[] tabTitles, int[] icons) {
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
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = this.context.getResources().getDrawable(this.icons[position], null);
        } else {
            drawable = this.context.getResources().getDrawable(this.icons[position]);
        }
        if (drawable == null) return "";
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        // 这里多设置5个空格
        SpannableString spannableString = new SpannableString("     " + this.tabTitles[position]);
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        //这里图片的开始和结束设置为0-3，根据上述的5个空格减去3个，然后有2个空格之间距离
        spannableString.setSpan(imageSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}
