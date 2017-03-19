package com.camnter.newlife.widget.listener;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Description：SmartSoftKeyboardListener
 * Created by：CaMnter
 */

public class SmartSoftKeyboardListener implements ViewTreeObserver.OnGlobalLayoutListener {

    private boolean first;
    private int height;

    @NonNull
    private final View rootLayout;
    @NonNull
    private final Listener listener;


    public SmartSoftKeyboardListener(@NonNull final View rootLayout,
                                     @NonNull final Listener listener) {
        this.rootLayout = rootLayout;
        this.listener = listener;
    }


    /**
     * Callback method to be invoked when the global layout state or the visibility of views
     * within the view tree changes
     */
    @Override
    public void onGlobalLayout() {

    /*
     * 判断 Listener 的类型
     * 如果 是 HeightListener，那么只计算一次
     * 其他，Listener or ShowListener，计算多次
     */
        if (this.listener instanceof HeightListener && !this.first) {
            return;
        }
        final Rect rect = new Rect();
        this.rootLayout.getWindowVisibleDisplayFrame(rect);
        final int screenHeight = this.rootLayout.getRootView().getHeight();
        final int heightDifference = screenHeight - rect.bottom;
        // 是否弹出
        boolean visible = heightDifference > screenHeight / 3;
        if (visible) {
            // 高度 只 保留一次，也只回调一次
            if (this.first) {
                this.height = heightDifference;
                this.first = false;
                if (this.listener instanceof HeightListener) {
                    this.listener.getSoftKeyboardHeight(this.height);
                }
            }
            if (!(this.listener instanceof HeightListener)) {
                this.listener.hasShow(true);
            }
        } else {
            if (!(this.listener instanceof HeightListener)) {
                this.listener.hasShow(false);
            }
        }
    }


    public int getHeight() {
        return this.height;
    }


    public interface Listener {
        void getSoftKeyboardHeight(int height);

        void hasShow(boolean show);
    }


    public static abstract class HeightListener implements Listener {
        /**
         * 禁掉 hasShow
         *
         * @param show show
         * @param height height
         * @Override public void hasShow(boolean show) {
         * // Nothing to do
         * }
         * }
         *
         *
         * public static abstract class ShowListener implements Listener {
         * /**
         * 禁掉 getSoftKeyboardHeight
         */
        @Override
        public void getSoftKeyboardHeight(int height) {
            // Nothing to do
        }
    }

}
