package com.camnter.newlife.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseFragmentCollaborator<F extends Fragment> {

    @NonNull
    private final WeakReference<F> fragmentReference;


    private BaseFragmentCollaborator() {
        this.fragmentReference = new WeakReference<>(null);
    }


    public BaseFragmentCollaborator(@NonNull final F fragment) {
        this.fragmentReference = new WeakReference<>(fragment);
    }


    @Nullable
    protected F getFragment() {
        return this.fragmentReference.get();
    }


    protected void clearReference() {
        this.fragmentReference.clear();
    }

}
