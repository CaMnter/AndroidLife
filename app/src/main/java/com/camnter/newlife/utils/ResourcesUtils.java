package com.camnter.newlife.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

/**
 * Description：ResourcesUtil
 * Created by：CaMnter
 * Time：2015-12-04 23:06
 */
public class ResourcesUtils {

    public static final int[] colors = new int[6];
    public static final int[][] states = new int[6][];


    static {
        states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
        states[1] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
        states[2] = new int[] { android.R.attr.state_enabled };
        states[3] = new int[] { android.R.attr.state_focused };
        states[4] = new int[] { android.R.attr.state_window_focused };
        states[5] = new int[] {};
    }


    /**
     * 根据Android系统版本，调用版本API中的获取颜色方法
     * According to the Android version, calls the method for color of version API
     *
     * @param context context
     * @param resId resource id
     * @return color
     */
    public static int getColor(Context context, int resId) {
        return ContextCompat.getColor(context, resId);
    }


    /**
     * 根据Android系统版本，调用版本API中的获取Drawable方法
     * According to the Android version, calls the method for drawable of version API
     *
     * @param context context
     * @param resId resource id
     * @return color
     */
    public static Drawable getDrawable(Context context, int resId) {
        return ContextCompat.getDrawable(context, resId);
    }


    /**
     * 创建一个ColorStateList
     * create a ColorStateList instance
     *
     * @param normal normal
     * @param pressed pressed
     * @param focused focused
     * @param unable unable
     * @return ColorStateList
     */
    public static ColorStateList createColorStateList(
            @ColorInt int normal,
            @ColorInt int pressed, @ColorInt int focused, @ColorInt int unable) {
        colors[0] = pressed;
        colors[1] = focused;
        colors[2] = normal;
        colors[3] = focused;
        colors[4] = unable;
        colors[5] = normal;
        return new ColorStateList(states, colors);
    }


    /**
     * 创建一个ColorStateList
     * create a StateListDrawable instance
     *
     * @param context context
     * @param normalRes normalRes
     * @param pressedRes pressedRes
     * @param focusedRes focusedRes
     * @param unableRes unableRes
     * @return StateListDrawable
     */
    public static StateListDrawable createStateListDrawable(Context context,
                                                            @DrawableRes int normalRes,
                                                            @DrawableRes int pressedRes,
                                                            @DrawableRes int focusedRes,
                                                            @DrawableRes int unableRes) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(
                new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled },
                getDrawable(context, pressedRes));
        stateListDrawable.addState(
                new int[] { android.R.attr.state_enabled, android.R.attr.state_focused },
                getDrawable(context, focusedRes));
        stateListDrawable.addState(new int[] { android.R.attr.state_enabled },
                getDrawable(context, normalRes));
        stateListDrawable.addState(new int[] { android.R.attr.state_focused },
                getDrawable(context, focusedRes));
        stateListDrawable.addState(new int[] { android.R.attr.state_window_focused },
                getDrawable(context, unableRes));
        stateListDrawable.addState(new int[] {}, getDrawable(context, normalRes));
        return stateListDrawable;
    }


    /**
     * 根据mipmap文件的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getMipmapId(Context context, String name) {
        return context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
    }


    /**
     * 根据layout文件的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getLayoutId(Context context, String name) {
        return context.getResources().getIdentifier(name, "layout", context.getPackageName());
    }


    /**
     * 根据string的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getStringId(Context context, String name) {
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }


    /**
     * 根据drawable文件的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getDrawableId(Context context, String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }


    /**
     * 根据style的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getStyleId(Context context, String name) {
        return context.getResources().getIdentifier(name, "style", context.getPackageName());
    }


    /**
     * 根据id的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getId(Context context, String name) {
        return context.getResources().getIdentifier(name, "id", context.getPackageName());
    }


    /**
     * 根据color文件的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getColorId(Context context, String name) {
        return context.getResources().getIdentifier(name, "color", context.getPackageName());
    }


    /**
     * 根据array的名字取得id
     *
     * @param context context
     * @param name name
     * @return int
     */
    public static int getArrayId(Context context, String name) {
        return context.getResources().getIdentifier(name, "array", context.getPackageName());
    }
}