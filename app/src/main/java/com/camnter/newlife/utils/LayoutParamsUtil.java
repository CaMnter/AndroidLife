package com.camnter.newlife.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

/**
 * Description：LayoutParamsUtil
 * Created by：CaMnter
 * Time：2015-12-16 22:55
 */
public class LayoutParamsUtil {

    public static DisplayMetrics dm;

    /**
     * Get the width of the screen
     * 屏幕宽度
     *
     * @param activity activity
     * @return widthPixels
     */
    public static int getScreenWidth(Activity activity) {
        if (dm == null) {
            dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }

    /**
     * Get the height of the screen
     * 屏幕高度
     *
     * @param activity activity
     * @return heightPixels
     */
    public static int getScreenHeigh(Activity activity) {
        if (dm == null) {
            dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }

    /**
     * Get the display metrics
     * 获取显示信息（DisplayMetrics）
     *
     * @param activity activity
     * @return DisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        if (dm == null) {
            dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        return dm;
    }

    /**
     * Set the marin value of view
     * 设置Marin值
     *
     * @param context       context
     * @param view          view
     * @param leftDpValue   leftDpValue
     * @param topDpValue    topDpValue
     * @param rightDpValue  rightDpValue
     * @param bottomDpValue bottomDpValue
     */
    public static void setMargins(Context context, View view, int leftDpValue, int topDpValue, int rightDpValue, int bottomDpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            int leftPxValue = (int) (leftDpValue * scale + 0.5f);
            int topPxValue = (int) (topDpValue * scale + 0.5f);
            int rightPxValue = (int) (rightDpValue * scale + 0.5f);
            int bottomPxValue = (int) (bottomDpValue * scale + 0.5f);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.setMargins(leftPxValue, topPxValue, rightPxValue, bottomPxValue);
            view.requestLayout();
        }
    }

}
