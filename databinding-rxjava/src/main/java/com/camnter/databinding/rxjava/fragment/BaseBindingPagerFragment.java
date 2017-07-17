package com.camnter.databinding.rxjava.fragment;

import android.databinding.ViewDataBinding;

/**
 * Created by：CaMnter
 */

public abstract class BaseBindingPagerFragment<VB extends ViewDataBinding>
    extends BaseBindingFragment<VB> {

    protected boolean isBaseVisibleToUser;


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isBaseVisibleToUser = isVisibleToUser;
        if (isVisibleToUser) {
            this.isVisibleToUserToDoing();
        }
    }


    protected void isVisibleToUserToDoing() {
        // Nothing to do
    }

}
