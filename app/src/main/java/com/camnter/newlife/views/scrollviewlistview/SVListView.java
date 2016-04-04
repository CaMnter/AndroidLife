package com.camnter.newlife.views.scrollviewlistview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Description：SVListView
 * Created by：CaMnter
 * Time：2015-09-28 17:35
 */
public class SVListView extends ListView {

    public SVListView(Context context) {
        super(context);
    }


    public SVListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public SVListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SVListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * 重新算高度，适应ScrollView的效果
     */
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
