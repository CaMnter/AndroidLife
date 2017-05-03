package com.camnter.newlife.widget.alphaview;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：TabAlphaIndicator
 * Created by：CaMnter
 */

public class TabAlphaIndicator extends LinearLayout {

    private ViewPager viewPager;
    private List<TabAlphaView> tabAlphaViewList = new ArrayList<>();

    private int childCount;
    private int currentPosition;


    public TabAlphaIndicator(Context context) {
        super(context);
    }


    public TabAlphaIndicator(Context context,
                             @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public TabAlphaIndicator(Context context,
                             @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TabAlphaIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setViewPager(@NonNull final ViewPager viewPager) {
        this.viewPager = viewPager;

        this.childCount = this.getChildCount();
        if (this.viewPager.getAdapter().getCount() != this.childCount) {
            throw new IllegalArgumentException("The ViewPager adapter count != child view count");
        }
        for (int i = 0; i < this.childCount; i++) {
            if (this.getChildAt(i) instanceof TabAlphaView) {
                final TabAlphaView tabAlphaView = (TabAlphaView) this.getChildAt(i);
                this.tabAlphaViewList.add(tabAlphaView);
                tabAlphaView.setOnClickListener(new TabViewOnClickListener(i));
            }
        }
        this.viewPager.addOnPageChangeListener(new OnPageChangeListener());
        this.tabAlphaViewList.get(this.currentPosition).setIconAlpha(1.0f);
    }


    @Override public int getChildCount() {
        return this.childCount;
    }


    private class OnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            if (positionOffset > 0) {
                tabAlphaViewList.get(position).setIconAlpha(1 - positionOffset);
                tabAlphaViewList.get(position + 1).setIconAlpha(positionOffset);
            }
            currentPosition = position;
        }
    }


    private class TabViewOnClickListener implements OnClickListener {

        private int position;


        TabViewOnClickListener(int position) {
            this.position = position;
        }


        @Override public void onClick(View v) {
            resetState();
            tabAlphaViewList.get(this.position).setIconAlpha(1.0f);
            viewPager.setCurrentItem(this.position, false);
        }

    }


    private void resetState() {
        for (TabAlphaView tabAlphaView : this.tabAlphaViewList) {
            tabAlphaView.setIconAlpha(0.0f);
        }
    }


    private static final String STATE_SUPER_STATE = "state_super_state";
    private static final String STATE_CURRENT_POSITION = "state_current_position";


    @Override protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(STATE_CURRENT_POSITION, this.currentPosition);
        return bundle;
    }


    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.currentPosition = bundle.getInt(STATE_CURRENT_POSITION);
            resetState();
            this.tabAlphaViewList.get(this.currentPosition).setIconAlpha(1.0f);
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

}
