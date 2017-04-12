package com.camnter.newlife.widget.listener;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;

/**
 * Description：SmartListViewListener
 * Created by：CaMnter
 */

public abstract class SmartListViewListener implements AbsListView.OnScrollListener {

    private static final String TAG = SmartListViewListener.class.getSimpleName();


    protected abstract void onScrollToBottom();


    protected void onScrollToTop() {
    }


    protected void onTouchScroll() {
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG,
            "[ListViewScrollToBottomListener]    [onScrollStateChanged]    [scrollState] = " +
                scrollState);
        // 当不滚动时
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 判断是否滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                this.onScrollToBottom();
            } else if (view.getFirstVisiblePosition() == 0) {
                final View firstView = view.getChildAt(0);
                if (firstView.getTop() == view.getListPaddingTop()) {
                    // 顶部
                    this.onScrollToTop();
                }
            }
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            this.onTouchScroll();
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Nothing to do
    }


    protected void hiddenKeyBoard(@NonNull final Activity activity) {
        View focusView = activity.getCurrentFocus();
        if (focusView == null) return;
        ((InputMethodManager) activity
            .getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(focusView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
