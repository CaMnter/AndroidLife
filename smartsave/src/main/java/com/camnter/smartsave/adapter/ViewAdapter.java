package com.camnter.smartsave.adapter;

import android.view.View;

/**
 * @author CaMnter
 */

public class ViewAdapter implements Adapter<View> {

    @Override
    public View findViewById(View target, int resId) {
        return target.findViewById(resId);
    }

}
