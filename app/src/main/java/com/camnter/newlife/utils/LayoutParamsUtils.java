package com.camnter.newlife.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Description：LayoutParamsUtil
 * Created by：CaMnter
 * Time：2015-12-16 22:55
 */
public class LayoutParamsUtils {

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
     * dp 转化为 px
     *
     * @param context context
     * @param dpValue dpValue
     * @return int
     */
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                context.getResources().getDisplayMetrics());
    }


    /**
     * Set the marin value of view
     * 设置Marin值
     *
     * @param context context
     * @param view view
     * @param leftDpValue leftDpValue
     * @param topDpValue topDpValue
     * @param rightDpValue rightDpValue
     * @param bottomDpValue bottomDpValue
     */
    public static void setMargins(Context context, View view, int leftDpValue, int topDpValue, int rightDpValue, int bottomDpValue) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams
                    = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.setMargins(dp2px(context, leftDpValue), dp2px(context, topDpValue),
                    dp2px(context, rightDpValue), dp2px(context, bottomDpValue));
            view.requestLayout();
        }
    }


    /**
     * Set the width of view
     * 设置宽度
     *
     * @param context context
     * @param view view
     * @param widthDpValue widthDpValue
     */
    public static void setWidth(Context context, View view, int widthDpValue) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams
                    = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.width = dp2px(context, widthDpValue);
            view.requestLayout();
        }
    }


    /**
     * Set the height of view
     * 设置高度
     *
     * @param context context
     * @param view view
     * @param heightDpValue heightDpValue
     */
    public static void setHeight(Context context, View view, int heightDpValue) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams
                    = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.height = dp2px(context, heightDpValue);
            view.requestLayout();
        }
    }


    /**
     * Set the 设置padding值 value of view
     * 设置padding值
     *
     * @param context context
     * @param view view
     * @param leftDpValue leftDpValue
     * @param topDpValue topDpValue
     * @param rightDpValue rightDpValue
     * @param bottomDpValue bottomDpValue
     */
    public static void setPadding(Context context, View view, int leftDpValue, int topDpValue, int rightDpValue, int bottomDpValue) {
        view.setPadding(dp2px(context, leftDpValue), dp2px(context, topDpValue),
                dp2px(context, rightDpValue), dp2px(context, bottomDpValue));
    }
}
