package com.camnter.newlife.widget.synchorizontalscrollview;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Description：ListViewAndHeadViewTouchListener
 * Created by：CaMnter
 */

public class ListViewAndHeadViewTouchListener implements View.OnTouchListener {

    private final HorizontalScrollView horizontalScrollView;


    public ListViewAndHeadViewTouchListener(
        @NonNull final HorizontalScrollView horizontalScrollView) {
        this.horizontalScrollView = horizontalScrollView;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.horizontalScrollView.onTouchEvent(event);
        return false;
    }

}

