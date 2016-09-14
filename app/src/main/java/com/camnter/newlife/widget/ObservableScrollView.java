package com.camnter.newlife.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import java.util.ArrayList;

public class ObservableScrollView extends ScrollView {

    private onScrollListener mOnScrollListener;
    private ArrayList<View> mUnControlViews = new ArrayList<View>();
    private Rect mRect = new Rect();


    public ObservableScrollView(Context context) {
        super(context);
    }


    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && !checkIfContainViews(ev)) {
            if (this.mOnScrollListener != null) {
                this.mOnScrollListener.onTouchDown();
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    public interface onScrollListener {

        void onTouchDown();
    }


    public void setOnScrollListener(onScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;

        View child = getChildAt(0);
        if (child != null) {
            child.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnScrollListener.onTouchDown();
                }
            });
        }

    }


    public void setUnControlViews(View... views) {
        this.mUnControlViews.clear();
        for (View view : views) {
            if (view == null) {
                continue;
            }
            this.mUnControlViews.add(view);
        }
    }


    private boolean checkIfContainViews(MotionEvent ev) {
        for (View view : this.mUnControlViews) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            this.mRect.set(location[0], location[1], location[0] + view.getMeasuredWidth(),
                location[1] + view.getMeasuredHeight());
            if (this.mRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                return true;
            }
        }
        return false;
    }

}