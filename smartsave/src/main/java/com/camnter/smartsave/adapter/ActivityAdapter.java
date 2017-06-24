package com.camnter.smartsave.adapter;

import android.app.Activity;
import android.view.View;

/**
 * @author CaMnter
 */

public class ActivityAdapter implements Adapter<Activity> {

    @Override
    public View findViewById(Activity target, int resId) {
        return target.findViewById(resId);
    }

}
