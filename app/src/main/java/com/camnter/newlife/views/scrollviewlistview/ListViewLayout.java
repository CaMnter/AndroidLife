package com.camnter.newlife.views.scrollviewlistview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * Description：ListViewLayout
 * Created by：CaMnter
 * Time：2015-09-28 17:13
 */
public class ListViewLayout extends LinearLayout {

    private BaseAdapter adapter;
    private OnClickListener onClickListener;


    public ListViewLayout(Context context) {
        super(context);
    }


    public ListViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ListViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListViewLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * 设置BaseAdapter和OnClickListener
     */
    public void setAdapterAndListener(BaseAdapter adapter, OnClickListener onClickListener) {
        this.init(adapter, onClickListener);
        int count = this.adapter.getCount();
        this.removeAllViews();

        // 初始化LinearLayout内容
        for (int i = 0; i < count; i++) {
            View v = this.adapter.getView(i, null, null);
            v.setOnClickListener(this.onClickListener);
            this.addView(v, i);
        }
    }


    private void init(BaseAdapter adapter, OnClickListener onClickListener) {
        this.adapter = adapter;
        this.onClickListener = onClickListener;
    }
}
