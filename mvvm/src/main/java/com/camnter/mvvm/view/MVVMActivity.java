package com.camnter.mvvm.view;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Description：MVVMActivity
 * Created by：CaMnter
 */

public abstract class MVVMActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.onBeforeDataBinding(savedInstanceState);
        if (this.getLayoutId() == 0) return;
        try {
            ViewDataBinding binding = DataBindingUtil.setContentView(this, this.getLayoutId());
            // binding success
            this.onCastingBinding(binding);
            // cast success
            this.baseActivityInit();
            this.onAfterDataBinding(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    protected abstract int getLayoutId();

    /**
     * on casting binding
     *
     * @param binding binding
     */
    protected abstract void onCastingBinding(@NonNull final ViewDataBinding binding);


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
