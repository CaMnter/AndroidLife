package com.camnter.newlife.widget.listener;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Description：SmartViewSizeListener
 * Created by：CaMnter
 */

public abstract class SmartViewSizeListener implements ViewTreeObserver.OnGlobalLayoutListener {

    @NonNull
    private final View targetView;


    public SmartViewSizeListener(@NonNull final View targetView) {
        this.targetView = targetView;
    }


    @Override public void onGlobalLayout() {
        // 执行一次就移除
        this.removeOnGlobalLayoutListener(this.targetView, this);
        this.measured(this.targetView.getMeasuredWidth(), this.targetView.getMeasuredHeight());
    }


    private void removeOnGlobalLayoutListener(@NonNull final View view,
                                              @NonNull
                                              final ViewTreeObserver.OnGlobalLayoutListener layoutListener) {
        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnGlobalLayoutListener(layoutListener);
        } else {
            viewTreeObserver.removeGlobalOnLayoutListener(layoutListener);
        }
    }


    protected abstract void measured(final int measuredWidth, final int measuredHeight);

}
