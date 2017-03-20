package com.camnter.newlife.widget.synchorizontalscrollview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Description：LinkageHorizontalScrollView
 * Created by：CaMnter
 */

public class LinkageHorizontalScrollView extends HorizontalScrollView {

    private LinkageHorizontalScrollViewDispatcher scrollDispatcher;


    public LinkageHorizontalScrollView(Context context) {
        super(context);
    }


    public LinkageHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public LinkageHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LinkageHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 保存 当前触摸的 ScrollView
        this.scrollDispatcher.touchView = this;
        return super.onTouchEvent(ev);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        if (this.scrollDispatcher == null) {
            super.onScrollChanged(l, t, oldL, oldT);
            return;
        }
        if (this.scrollDispatcher.touchView == this) {
            // 通知 其他
            this.scrollDispatcher.onScrollChanged(l, t);
        } else {
            // 被通知
            super.onScrollChanged(l, t, oldL, oldT);
        }
    }


    @Override
    public void fling(int velocityX) {
        super.fling(velocityX / 20);
    }


    public void setScrollDispatcher(
        @NonNull final LinkageHorizontalScrollViewDispatcher scrollDispatcher) {
        this.scrollDispatcher = scrollDispatcher;
    }

}

