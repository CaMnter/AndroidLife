package com.camnter.newlife.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseActivityCollaborator<A extends Activity> {

    @NonNull
    private final WeakReference<A> activityReference;


    private BaseActivityCollaborator() {
        this.activityReference = new WeakReference<>(null);
    }


    public BaseActivityCollaborator(@NonNull final A activity) {
        this.activityReference = new WeakReference<>(activity);
    }


    @Nullable
    protected A getActivity() {
        return this.activityReference.get();
    }


    protected void clearReference() {
        this.activityReference.clear();
    }

}
