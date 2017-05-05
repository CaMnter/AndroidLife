package com.camnter.mvvm.view;

import android.app.Activity;
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
    protected Activity activity;

    protected LayoutInflater inflater;
    protected ViewGroup container;

    protected ViewDataBinding rootBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this.getActivity();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.onBeforeDataBinding(savedInstanceState);
        final int layoutId = this.getLayoutId();
        if (layoutId == 0) return null;
        try {
            this.inflater = inflater;
            if (this.autoInflateView()) {
                this.rootBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);
            }
            // binding success, but maybe this.contentViewBinding == null
            this.onCastingRootBinding(this.rootBinding);
            if (this.rootBinding != null) {
                this.self = this.rootBinding.getRoot();
            }
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
     * default true
     *
     * @return auto ?
     */
    protected boolean autoInflateView() {
        return true;
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
