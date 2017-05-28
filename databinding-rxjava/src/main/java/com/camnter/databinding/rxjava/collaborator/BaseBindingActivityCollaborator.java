package com.camnter.databinding.rxjava.collaborator;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import com.camnter.databinding.rxjava.activity.BaseBindingActivity;

/**
 * @author CaMnter
 */

public class BaseBindingActivityCollaborator<BA extends BaseBindingActivity, VB extends ViewDataBinding>
    extends BaseActivityCollaborator<BA> {

    @NonNull
    protected VB binding;


    @SuppressWarnings("unchecked")
    public BaseBindingActivityCollaborator(@NonNull BA bindingActivity) {
        super(bindingActivity);
        this.binding = (VB) bindingActivity.getContentBinding();
    }

}
