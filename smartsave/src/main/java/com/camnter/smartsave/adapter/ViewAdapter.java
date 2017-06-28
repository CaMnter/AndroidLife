package com.camnter.smartsave.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.camnter.smartsave.support.v4.ContextCompat;

/**
 * @author CaMnter
 */

public class ViewAdapter implements Adapter<View> {

    @Override
    public View findViewById(View target, int resId) {
        return target.findViewById(resId);
    }


    @Override
    public String getString(View target, int resId) {
        Context context = target.getContext();
        if (context == null) return null;
        return context.getString(resId);
    }


    @Override
    public Drawable getDrawable(View target, int resId) {
        Context context = target.getContext();
        if (context == null) return null;
        return ContextCompat.getDrawable(context, resId);
    }


    @Override
    public int getColor(View target, int resId) {
        Context context = target.getContext();
        if (context == null) return 0;
        return ContextCompat.getColor(context, resId);
    }

}
