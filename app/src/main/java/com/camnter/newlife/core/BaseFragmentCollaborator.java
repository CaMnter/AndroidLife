package com.camnter.newlife.core;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseFragmentCollaborator<F extends Fragment> {

    @NonNull
    private final WeakReference<F> fragmentReference;


    public BaseFragmentCollaborator(@NonNull final F fragment) {
        this.fragmentReference = new WeakReference<>(fragment);
    }

}
