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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getLayoutId() == 0) return;
        ViewDataBinding binding = DataBindingUtil.setContentView(this, this.getLayoutId());
        this.onAfterDataBinding(binding, savedInstanceState);
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    protected abstract int getLayoutId();

    /**
     * on after data binding
     *
     * @param binding binding
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void onAfterDataBinding(
        @NonNull final ViewDataBinding binding, @NonNull final Bundle savedInstanceState);

}
