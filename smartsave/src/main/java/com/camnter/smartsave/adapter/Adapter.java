package com.camnter.smartsave.adapter;

import android.view.View;

/**
 * @author CaMnter
 */

public interface Adapter<T> {

    View findViewById(T target, int resId);

    String getString(T target, int resId);

}
