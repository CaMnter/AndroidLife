package com.camnter.databinding.rxjava.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by：CaMnter
 */

public abstract class BaseBindingLazyPagerFragment extends BaseBindingPagerFragment {

    private static final String EMPTY_VIEW_TEXT_NULL_DATA = "暂无数据";

    private boolean hasLoaded = false;
    private boolean isCreateView = false;


    /**
     * baseFragment init
     */
    @Override
    protected void baseFragmentInit() {
        super.baseFragmentInit();
        this.isCreateView = true;
        this.lazyLoading();
    }


    @Override
    protected void isVisibleToUserToDoing() {
        super.isVisibleToUserToDoing();
        this.preparedLazyLoading();
    }


    private void preparedLazyLoading() {
        this.lazyLoading();
    }


    /**
     * 懒加载真正的地方实质上还是 onCreatedView
     */
    private void lazyLoading() {
        if (!this.isBaseVisibleToUser ||
            this.hasLoaded ||
            !this.isCreateView) {
            return;
        }
        this.lazyToDoing();
        this.hasLoaded = true;
    }


    /**
     * on after data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onAfterDataBinding(@Nullable Bundle savedInstanceState) {
        // Nothing to do
    }


    /**
     * lazy to do what you want to do
     */
    protected abstract void lazyToDoing();

}
