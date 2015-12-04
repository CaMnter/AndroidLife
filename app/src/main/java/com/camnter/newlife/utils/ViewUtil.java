package com.camnter.newlife.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Description：ViewUtil
 * Created by：CaMnter
 * Time：2015-12-04 22:33
 */
public class ViewUtil {
    /**
     * Set view alpha
     * 设置透明度
     *
     * @param view view
     * @param alpha alpha value
     */
    public static void setAlpha(View view, int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setAlpha(alpha / 255.0f);
        } else {
            Drawable drawable = view.getBackground();
            if (drawable != null) {
                drawable.setAlpha(alpha);
            }
        }
    }
}
