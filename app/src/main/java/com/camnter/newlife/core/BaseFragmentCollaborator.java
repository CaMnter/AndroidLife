package com.camnter.newlife.core;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseFragmentCollaborator<F extends Fragment> {

    @NonNull
    protected final WeakReference<F> fragmentReference;


    private BaseFragmentCollaborator() {
        this.fragmentReference = new WeakReference<>(null);
    }


    public BaseFragmentCollaborator(@NonNull final F fragment) {
        this.fragmentReference = new WeakReference<>(fragment);
    }

}
