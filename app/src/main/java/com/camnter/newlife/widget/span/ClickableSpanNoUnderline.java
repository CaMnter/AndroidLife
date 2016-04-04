package com.camnter.newlife.widget.span;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

public class ClickableSpanNoUnderline extends ClickableSpan {

    private static final String TAG = "ClickableSpan";

    private static final int NO_COLOR = -206;
    private int color;

    private OnClickListener onClickListener;


    public ClickableSpanNoUnderline(int color, OnClickListener onClickListener) {
        super();
        this.color = color;
        this.onClickListener = onClickListener;
    }


    public ClickableSpanNoUnderline(OnClickListener onClickListener) {
        this(NO_COLOR, onClickListener);
    }


    /**
     * Makes the text underlined and in the link color.
     */
    @Override public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        // 设置文字颜色
        if (this.color == NO_COLOR) {
            ds.setColor(ds.linkColor);
        } else {
            ds.setColor(this.color);
        }
        ds.clearShadowLayer();
        // 去除下划线
        ds.setUnderlineText(false);
        ds.bgColor = Color.TRANSPARENT;
    }


    /**
     * Performs the click action associated with this span.
     *
     * @param widget widget
     */
    @Override public void onClick(View widget) {
        if (this.onClickListener != null) {
            this.onClickListener.onClick(widget, this);
        } else {
            Log.w(TAG, "listener was null");
        }
    }


    /**
     * 回调接口，回调自身的onClick事件
     * 告诉外部 是否被点击
     */
    public interface OnClickListener<T extends ClickableSpanNoUnderline> {
        /**
         * ClickableSpan被点击
         *
         * @param widget widget
         * @param span span
         */
        void onClick(View widget, T span);
    }
}