package com.camnter.newlife.widget.tab;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Description：ViewPagerTab
 * Created by：CaMnter
 */

public class ViewPagerTab extends ViewGroup {

    private PageListener pageListener = new PageListener();

    private int width;
    private int mHeight;
    private int itemCount;
    private Scroller scroller;
    // 120 px
    private int tabLineSize = 120;


    public ViewPagerTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
    }


    public void setItemCount(int count) {
        this.itemCount = count;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.getChildCount() <= 0) return;
        this.getChildAt(0).layout(
            (width / itemCount - tabLineSize) / 2,
            0,
            (width / itemCount - tabLineSize) / 2 + tabLineSize,
            mHeight
        );
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }


    public void setViewPager(ViewPager viewPager) {
        ViewPager mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(pageListener);
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }


    public interface OnPageSelected {
        public abstract void onPageSelected(int position);
    }


    private OnPageSelected listener = null;


    public void setOnPageSelectedListener(OnPageSelected listener) {
        this.listener = listener;
    }


    private class PageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            scrollToProxyByOnPageChangeListener(position, positionOffset);
        }


        @Override
        public void onPageSelected(int position) {

            if (listener != null) {
                listener.onPageSelected(position);
            }
        }


        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

    }


    public void scrollToProxyByOnPageChangeListener(final int position,
                                                    final float positionOffset) {
        this.scrollTo(-position * this.width / this.itemCount -
            Math.round(positionOffset * this.width / this.itemCount), 0);
    }


    public PageListener getPagerListener() {
        return pageListener;
    }


    public void setPagerListener(PageListener listener) {
        this.pageListener = listener;
    }


    public void setViewTabWidth(int tab_line_width) {
        this.tabLineSize = tab_line_width;
    }


    public void setTabLineSize(int tabLineSize) {
        this.tabLineSize = tabLineSize;
    }

}
