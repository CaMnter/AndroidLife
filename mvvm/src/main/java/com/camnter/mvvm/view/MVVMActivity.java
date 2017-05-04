package com.camnter.mvvm.view;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Description：MVVMActivity
 * Created by：CaMnter
 */

public abstract class MVVMActivity extends AppCompatActivity {

    protected ViewDataBinding rootBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.onBeforeDataBinding(savedInstanceState);
        if (this.getLayoutId() == 0) return;
        try {
            if (autoSetContentView()) {
                this.rootBinding = DataBindingUtil.setContentView(this, this.getLayoutId());
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
    protected void onBeforeDataBinding(@NonNull final Bundle savedInstanceState) {
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
    protected abstract void onAfterDataBinding(@NonNull final Bundle savedInstanceState);

}
