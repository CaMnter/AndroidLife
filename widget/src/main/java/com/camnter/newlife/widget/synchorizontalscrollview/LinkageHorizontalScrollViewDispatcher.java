package com.camnter.newlife.widget.synchorizontalscrollview;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：LinkageHorizontalScrollViewDispatcher
 * Created by：CaMnter
 */

public class LinkageHorizontalScrollViewDispatcher {

    // 记录 当前触摸的 ScrollView
    LinkageHorizontalScrollView touchView;

    @NonNull
    private final LinkageHorizontalScrollView titleScrollView;
    private final List<LinkageHorizontalScrollView> scrollViews;


    public LinkageHorizontalScrollViewDispatcher(
        @NonNull final LinkageHorizontalScrollView titleScrollView) {
        this.titleScrollView = titleScrollView;
        this.scrollViews = new ArrayList<>();
    }


    void onScrollChanged(int l, int t) {
        // title scroll view
        if (this.touchView != this.titleScrollView) {
            this.titleScrollView.smoothScrollTo(l, t);
        }
        // content scroll view
        for (LinkageHorizontalScrollView scrollView : this.scrollViews) {
            if (this.touchView != scrollView) {
                scrollView.smoothScrollTo(l, t);
            }
        }
    }


    public void addScrollView(
        @NonNull final LinkageHorizontalScrollView linkageHorizontalScrollView) {
        this.scrollViews.add(linkageHorizontalScrollView);
    }


    public void titleScrollViewScrollToProxy(final int x, final int y) {
        this.titleScrollView.scrollTo(x, y);
    }


    /**
     * 通过 titleScrollView
     * 矫正
     * 其他 ScrollView 的 X
     */
    public void recoverScrollLocationByTitleScrollView() {
        if (this.scrollViews.isEmpty()) return;
        final int scrollX = this.titleScrollView.getScrollX();
        for (final LinkageHorizontalScrollView scrollView : this.scrollViews) {
            if (scrollView == this.titleScrollView) continue;
            scrollView.scrollTo(scrollX, 0);
        }
    }


    public void recoverScrollLocationByTitleScrollView(
        @NonNull final LinkageHorizontalScrollView scrollView) {
        final int titleScrollX = this.titleScrollView.getScrollX();
        final int scrollX = scrollView.getScrollX();
        if (scrollX != titleScrollX) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(titleScrollX, 0);
                }
            });
        }
    }

}
