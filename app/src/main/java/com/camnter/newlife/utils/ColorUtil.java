package com.camnter.newlife.utils;

import android.app.Activity;
import android.graphics.Color;
import java.util.regex.Pattern;

/**
 * Description：ColorUtil
 * Created by：CaMnter
 * Time：2015-09-29 11:13
 */
public class ColorUtil {

    /**
     * 根据颜色资源Id，取得颜色
     *
     * @param colorId colorId
     * @return color
     */
    public static int getResourcesColor(Activity activity, int colorId) {
        int color = 0x00ffffff;
        try {
            color = ResourcesUtils.getColor(activity, colorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return color;
    }


    /**
     * 将十六进制 颜色代码 转换为 int
     *
     * @return color
     */
    public static int HextoColor(String color) {
        // #00000000 - #ffffffff
        String reg = "#[a-f0-9A-F]{8}";
        if (!Pattern.matches(reg, color)) {
            color = "#ffffffff";
        }
        return Color.parseColor(color);
    }


    /**
     * 设置颜色透明度
     *
     * @param color color
     * @param alpha alpha
     * @return color
     */
    public static int setColorAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
