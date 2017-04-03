package com.camnter.newlife.widget.listener;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Description：SmartStatusBarListener
 * Created by：CaMnter
 */

public abstract class SmartStatusBarListener implements ViewTreeObserver.OnGlobalLayoutListener {

    @NonNull
    private final View targetView;
    @NonNull
    private final Activity activity;

    private int statusBarHeight;


    public SmartStatusBarListener(@NonNull final Activity activity,
                                  @NonNull final View targetView) {
        this.activity = activity;
        this.targetView = targetView;
    }


    @Override
    public void onGlobalLayout() {
        this.removeGlobalLayoutListener(this.targetView, this);
        Rect frame = new Rect();
        this.activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        this.statusBarHeight = frame.top;
        this.getStatusBatHeight(this.statusBarHeight);
    }


    private void removeGlobalLayoutListener(@NonNull final View view,
                                            @NonNull
                                            final ViewTreeObserver.OnGlobalLayoutListener layoutListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
        }
    }


    public int getStatusBarHeight() {
        return this.statusBarHeight;
    }


    protected abstract void getStatusBatHeight(final int statusBarHeight);

}