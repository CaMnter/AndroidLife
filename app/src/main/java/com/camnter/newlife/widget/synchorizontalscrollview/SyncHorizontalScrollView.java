package com.camnter.newlife.widget.synchorizontalscrollview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Description：SyncHorizontalScrollView
 * Created by：CaMnter
 */

public class SyncHorizontalScrollView extends HorizontalScrollView {

    private View targetView;


    public SyncHorizontalScrollView(Context context) {
        super(context);
    }


    public SyncHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public SyncHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SyncHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * This is called in response to an internal scroll in this view (i.e., the
     * view scrolled its own contents). This is typically as a result of
     * {@link #scrollBy(int, int)} or {@link #scrollTo(int, int)} having been
     * called.
     *
     * @param l Current horizontal scroll origin.
     * @param t Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.targetView == null) return;
        this.targetView.scrollTo(l, t);
    }


    public void setTargetView(View targetView) {
        this.targetView = targetView;
    }

}
