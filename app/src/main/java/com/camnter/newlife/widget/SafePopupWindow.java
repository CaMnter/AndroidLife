package com.camnter.newlife.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;
import java.lang.ref.WeakReference;

/**
 * Description：SafePopupWindow
 * Created by：CaMnter
 * Time：2016-03-21 15:23
 */
public class SafePopupWindow extends PopupWindow {

    private WeakReference<Context> mContext;


    public SafePopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = new WeakReference<>(context);
    }


    public SafePopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = new WeakReference<>(context);
    }


    public SafePopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = new WeakReference<>(context);
    }


    public SafePopupWindow(Context context) {
        super(context);
        this.mContext = new WeakReference<>(context);
    }


    public SafePopupWindow(View contentView) {
        super(contentView);
        this.mContext = new WeakReference<>(contentView.getContext());
    }


    public SafePopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
        this.mContext = new WeakReference<>(contentView.getContext());
    }


    public SafePopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        this.mContext = new WeakReference<>(contentView.getContext());
    }


    @Override public void showAtLocation(View parent, int gravity, int x, int y) {
        Context context = mContext.get();
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        super.showAtLocation(parent, gravity, x, y);
    }


    @Override public void showAsDropDown(View anchor) {
        Context context = mContext.get();
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        super.showAsDropDown(anchor);
    }


    @Override public void showAsDropDown(View anchor, int xoff, int yoff) {
        Context context = mContext.get();
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        super.showAsDropDown(anchor, xoff, yoff);
    }


    @Override public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        Context context = mContext.get();
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }
}
