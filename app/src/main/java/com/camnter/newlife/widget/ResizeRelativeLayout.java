package com.camnter.newlife.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Description：ResizeRelativeLayout
 * Created by：CaMnter
 */

public class ResizeRelativeLayout extends RelativeLayout {

    private OnResizeListener mListener;


    public ResizeRelativeLayout(Context context) {
        super(context);
    }


    public ResizeRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mListener != null) {
            this.mListener.OnResize(w, h, oldw, oldh);
        }
    }


    public void setOnResizeListener(OnResizeListener onResizeListener) {
        this.mListener = onResizeListener;
    }


    public interface OnResizeListener {
        void OnResize(int w, int h, int oldw, int oldh);
    }

}
