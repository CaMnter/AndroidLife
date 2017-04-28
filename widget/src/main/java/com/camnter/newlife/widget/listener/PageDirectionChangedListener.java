package com.camnter.newlife.widget.listener;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.view.ViewPager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 粗略计算方向
 * 为防止有问题
 * 还是得 Override onPageSelected 写对应逻辑
 *
 * 数据显示：如果滑动过程中
 * 比如 1 >> 2 那么
 * 会有无数次 position == 1，最后一次 position == 2
 * 这样在方向判断上有存在误差
 * 需要过滤最后一次 position == 2 的这个数据
 * 这次的方向也是存在误差的，也要排除
 *
 * Description：PageDirectionChangedListener
 * Created by：CaMnter
 */

public abstract class PageDirectionChangedListener extends SmartPageChangeListener {

    private static final int INVALID_POSITION = -1;
    private static final int RECORD_THRESHOLD = 3;

    protected static final String LEFT = "left";
    protected static final String RIGHT = "right";

    private int recordCount = 0;

    private int accuratePosition = INVALID_POSITION;
    private int currentPosition = 0;
    @Direction
    private String accurateDirection;


    @StringDef({ LEFT, RIGHT })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface Direction {

    }


    private float lastValue;
    private boolean isScrolling;


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.recordAccuratePosition(position);
        this.currentPosition = position;
        if (this.isScrolling) {
            if (this.lastValue > positionOffset) {
                // 递减，从左向右滑动
                this.onPreparePageScrolled(LEFT, position, this.accuratePosition, positionOffset,
                    positionOffsetPixels);
            } else if (this.lastValue < positionOffset) {
                // 递增，从右向左滑动
                this.onPreparePageScrolled(RIGHT, position, this.accuratePosition, positionOffset,
                    positionOffsetPixels);
            }
        }
        this.lastValue = positionOffset;
        this.isScrolling = true;
    }


    private void recordAccuratePosition(final int position) {
        if (position == this.currentPosition) {
            this.recordCount++;
        }
        if (this.accuratePosition == INVALID_POSITION) {
            this.accuratePosition = position;
        }
        if (recordCount == RECORD_THRESHOLD) {
            this.accuratePosition = position;
            this.recordCount = 0;
        }
    }


    private void recordAccurateDirection(@Direction @NonNull final String direction) {
        if (this.accurateDirection == null) {
            this.accurateDirection = direction;
        }
    }


    private boolean checkDirection(@Direction @NonNull final String direction) {
        return direction.equals(this.accurateDirection);
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (this.isScrolling) {
            this.isScrolling = false;
        }
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            this.accuratePosition = INVALID_POSITION;
            this.accurateDirection = null;
            this.onSmartPageScrollIdle(this.currentPosition);
        }
    }


    private void onPreparePageScrolled(@Direction @NonNull final String direction,
                                       final int position,
                                       final int accuratePosition,
                                       final float positionOffset,
                                       final int positionOffsetPixels) {
        this.recordAccurateDirection(direction);
        this.onSmartPageScrolled(position, positionOffset,
            positionOffsetPixels);
        if (checkDirection(direction)) {
            this.onSmartPageDirectionChanged(
                direction,
                direction.equals(LEFT) ? accuratePosition + 1 : accuratePosition,
                direction.equals(LEFT) ? 1.0f - positionOffset : positionOffset,
                positionOffsetPixels);
        }
    }


    protected abstract void onSmartPageScrollIdle(final int position);

    protected abstract void onSmartPageScrolled(final int position,
                                                final float positionOffset,
                                                final int positionOffsetPixels);

    protected abstract void onSmartPageDirectionChanged(@Direction @NonNull final String direction,
                                                        final int accuratePosition,
                                                        final float positionOffset,
                                                        final int positionOffsetPixels);

}
