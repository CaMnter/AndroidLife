package com.camnter.databinding.rxjava.collaborator;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import com.camnter.databinding.rxjava.fragment.BaseBindingFragment;

/**
 * @author CaMnter
 */

public class BaseBindingFragmentCollaborator<BF extends BaseBindingFragment, VB extends ViewDataBinding>
    extends BaseFragmentCollaborator<BF> {

    @NonNull
    protected VB binding;


    @SuppressWarnings("unchecked")
    public BaseBindingFragmentCollaborator(@NonNull BF bindingFragment) {
        super(bindingFragment);
        this.binding = (VB) bindingFragment.getContentBinding();
    }

}
