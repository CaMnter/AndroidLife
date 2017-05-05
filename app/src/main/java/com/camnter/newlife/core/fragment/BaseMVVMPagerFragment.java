package com.camnter.newlife.core.fragment;

/**
 * Description：BaseMVVMPagerFragment
 * Created by：CaMnter
 */

public abstract class BaseMVVMPagerFragment extends BaseMVVMFragment {

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
