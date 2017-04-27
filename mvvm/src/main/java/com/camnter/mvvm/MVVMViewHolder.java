package com.camnter.mvvm;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Description：MVVMViewHolder
 *
 * Refer to the
 * {@link "https://github.com/markzhai/DataBindingAdapter/blob/master/adapter/src/main/java/com/github/markzhai/recyclerview/BindingViewHolder.java"}
 *
 * Created by：CaMnter
 */

public class MVVMViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {

    @NonNull
    private final T binding;


    public MVVMViewHolder(@NonNull final T binding) {
        // binding.getRoot() = itemView
        super(binding.getRoot());
        this.binding = binding;
    }


    @NonNull
    public T getBinding() {
        return this.binding;
    }

}