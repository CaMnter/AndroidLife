package com.camnter.newlife.widget.scroller;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Scroller#getCurrX()
 * 获取 Scroller 当前水平滚动的位置
 *
 * Scroller#getCurrY()
 * 获取 Scroller 当前竖直滚动的位置
 *
 * Scroller#getFinalX()
 * 获取 Scroller 最终停止的水平位置
 *
 * Scroller#FinalY()
 * 获取 Scroller 最终停止的竖直位置
 *
 * Scroller#setFinalX(int newX)
 * 设置 Scroller 最终停留的水平位置，没有动画效果，直接跳到目标位置
 *
 * Scroller#setFinalY(int newY)
 * 设置 Scroller 最终停留的竖直位置，没有动画效果，直接跳到目标位置
 *
 * Scroller#getCurrY()
 * 获取 Scroller 当前竖直滚动的位置
 *
 * Scroller#startScroll(int startX, int startY, int dx, int dy)
 * Scroller#startScroll(int startX, int startY, int dx, int dy, int duration)
 * startX, startY 为开始滚动的位置
 * dx, dy 为滚动的偏移量
 * duration 为完成滚动的时间
 *
 *
 * Scroller#computeScrollOffset()
 * true 说明滚动尚未完成，false 说明滚动已经完成
 * 重要的方法，通常放在 View.computeScroll() 中，用来判断是否滚动是否结束
 *
 * @author CaMnter
 */

public class ScrollerLayout extends LinearLayout {

    private Scroller scroller;


    public ScrollerLayout(Context context) {
        super(context);
        this.scroller = new Scroller(context);
    }


    public ScrollerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.scroller = new Scroller(context);
    }


    public ScrollerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.scroller = new Scroller(context);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScrollerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.scroller = new Scroller(context);
    }


    public void smoothScrollTo(final int finalX, final int finalY) {
        final int dx = finalX - this.scroller.getFinalX();
        final int dy = finalY - this.scroller.getFinalY();
        this.smoothScrollBy(dx, dy);
    }


    public void smoothScrollBy(final int dx, final int dy) {
        this.scroller.startScroll(this.scroller.getFinalX(), this.scroller.getFinalY(), dx, dy);
        this.invalidate();
    }


    @Override
    public void computeScroll() {
        if (this.scroller.computeScrollOffset()) {
            this.scrollTo(this.scroller.getCurrX(), this.scroller.getCurrY());
            this.postInvalidate();
        }
        super.computeScroll();
    }

}
