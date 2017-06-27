package com.camnter.smartsave.adapter;

import android.content.Context;
import android.view.View;

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
        final Context context = target.getContext();
        if (context == null) return null;
        return context.getString(resId);
    }

}
