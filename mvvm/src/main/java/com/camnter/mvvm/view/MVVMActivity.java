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
        try {
            this.castingBinding(binding);
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
     * @param binding binding
     */
    protected abstract void castingBinding(@NonNull final ViewDataBinding binding);

    /**
     * on after data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void onAfterDataBinding(@NonNull final Bundle savedInstanceState);

}
