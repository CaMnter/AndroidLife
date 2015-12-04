package com.camnter.newlife.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Description：ResourcesUtil
 * Created by：CaMnter
 * Time：2015-12-04 23:06
 */
public class ResourcesUtil {

    /**
     * 根据Android系统版本，调用版本API中的获取颜色方法
     * According to the Android version, calls the method for color of version API
     *
     * @param activity activity
     * @param resId    resource id
     * @return color
     */
    public static int getColor(Activity activity, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.getResources().getColor(resId, activity.getTheme());
        } else {
            return activity.getResources().getColor(resId);
        }
    }

    /**
     * 根据Android系统版本，调用版本API中的获取Drawable方法
     * According to the Android version, calls the method for drawable of version API
     *
     * @param activity activity
     * @param resId    resource id
     * @return color
     */
    public static Drawable getDrawable(Activity activity, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return activity.getTheme().getDrawable(resId);
        } else {
            return activity.getResources().getDrawable(resId);
        }
    }

    /**
     * 根据mipmap文件的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getMipmapId(Context context, String name) {
        return context.getResources().getIdentifier(name, "mipmap",
                context.getPackageName());
    }

    /**
     * 根据layout文件的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getLayoutId(Context context, String name) {
        return context.getResources().getIdentifier(name, "layout",
                context.getPackageName());
    }

    /**
     * 根据string的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getStringId(Context context, String name) {
        return context.getResources().getIdentifier(name, "string",
                context.getPackageName());
    }

    /**
     * 根据drawable文件的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getDrawableId(Context context, String name) {
        return context.getResources().getIdentifier(name,
                "drawable", context.getPackageName());
    }

    /**
     * 根据style的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getStyleId(Context context, String name) {
        return context.getResources().getIdentifier(name,
                "style", context.getPackageName());
    }

    /**
     * 根据id的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getId(Context context, String name) {
        return context.getResources().getIdentifier(name, "id", context.getPackageName());
    }

    /**
     * 根据color文件的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getColorId(Context context, String name) {
        return context.getResources().getIdentifier(name,
                "color", context.getPackageName());
    }

    /**
     * 根据array的名字取得id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getArrayId(Context context, String name) {
        return context.getResources().getIdentifier(name,
                "array", context.getPackageName());
    }

} 