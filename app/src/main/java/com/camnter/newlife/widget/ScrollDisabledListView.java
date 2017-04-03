package com.camnter.newlife.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Description：ScrollDisabledListView
 * http://stackoverflow.com/questions/7611085/disable-scrolling-in-listview
 * Created by：CaMnter
 */

public class ScrollDisabledListView extends ListView {

    private int position;
    private boolean scrollAble = true;


    public ScrollDisabledListView(Context context) {
        super(context);
    }


    public ScrollDisabledListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public ScrollDisabledListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScrollDisabledListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setScrollAble(final boolean scrollAble) {
        this.scrollAble = scrollAble;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /*
         * 可滚动
         */
        if (this.scrollAble) return super.dispatchTouchEvent(ev);

        /*
         * 不可滚动
         */
        final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            // 记录手指按下时的位置
            position = pointToPosition((int) ev.getX(), (int) ev.getY());
            return super.dispatchTouchEvent(ev);
        }
        if (actionMasked == MotionEvent.ACTION_MOVE) {
            // 最关键的地方，忽略MOVE 事件
            // ListView onTouch 获取不到 MOVE 事件所以不会发生滚动处理
            return true;
        }
        // 手指抬起时
        if (actionMasked == MotionEvent.ACTION_UP
            || actionMasked == MotionEvent.ACTION_CANCEL) {
            // 手指按下与抬起都在同一个视图内，交给父控件处理，这是一个点击事件
            if (pointToPosition((int) ev.getX(), (int) ev.getY()) == position) {
                super.dispatchTouchEvent(ev);
            } else {
                // 如果手指已经移出按下时的 Item，说明是滚动行为，清理 Item pressed 状态
                setPressed(false);
                invalidate();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
