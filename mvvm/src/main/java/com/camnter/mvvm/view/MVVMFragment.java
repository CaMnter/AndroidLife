package com.camnter.mvvm.view;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Description：MVVMFragment
 * Created by：CaMnter
 */

public abstract class MVVMFragment extends Fragment {

    protected View self;
    protected LayoutInflater inflater;

    protected ViewDataBinding rootBinding;


    @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        this.onBeforeDataBinding(savedInstanceState);
        final int layoutId = this.getLayoutId();
        if (layoutId == 0) return null;
        try {
            this.inflater = inflater;
            this.rootBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);
            this.self = this.rootBinding.getRoot();
            // binding success, but maybe this.contentViewBinding == null
            this.onCastingRootBinding(this.rootBinding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // cast success
        this.baseFragmentInit();
        this.onAfterDataBinding(savedInstanceState);
        // self may be null
        return this.self;
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    protected abstract int getLayoutId();

    /**
     * on casting root binding
     *
     * @param rootBinding rootBinding
     */
    protected abstract void onCastingRootBinding(
        @Nullable final ViewDataBinding rootBinding);


    /**
     * on before data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    protected void onBeforeDataBinding(@Nullable final Bundle savedInstanceState) {
        // Nothing to do
    }


    /**
     * baseFragment init
     */
    protected void baseFragmentInit() {
        // Nothing to do
    }


    /**
     * on after data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void onAfterDataBinding(@Nullable final Bundle savedInstanceState);

}
