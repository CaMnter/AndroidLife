package com.camnter.smartsave.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.camnter.smartsave.support.v4.ContextCompat;

/**
 * @author CaMnter
 */

public class ActivityAdapter implements Adapter<Activity> {

    @Override
    public View findViewById(Activity target, int resId) {
        return target.findViewById(resId);
    }


    @Override
    public String getString(Activity target, int resId) {
        return target.getString(resId);
    }


    @Override
    public Drawable getDrawable(Activity target, int resId) {
        return ContextCompat.getDrawable(target, resId);
    }


    @Override
    public int getColor(Activity target, int resId) {
        return ContextCompat.getColor(target, resId);
    }

}
