package com.camnter.databinding.view;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Description：BindingActivity
 * @author CaMnter
 */

public abstract class BindingActivity extends AppCompatActivity {

    protected ViewDataBinding rootBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.onBeforeDataBinding(savedInstanceState);
        final int layoutId = this.getLayoutId();
        if (layoutId == 0) return;
        try {
            if (this.autoSetContentView()) {
                this.rootBinding = DataBindingUtil.setContentView(this, layoutId);
            }
            // binding success, but maybe this.contentViewBinding == null
            this.onCastingRootBinding(this.rootBinding);
            // cast success
            this.baseActivityInit();
            this.onAfterDataBinding(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * default true
     *
     * @return auto ?
     */
    protected boolean autoSetContentView() {
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
     * baseActivity init
     */
    protected void baseActivityInit() {
        // Nothing to do
    }


    /**
     * on after data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void onAfterDataBinding(@Nullable final Bundle savedInstanceState);

}
