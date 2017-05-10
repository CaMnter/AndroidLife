package com.camnter.newlife.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseActivityCollaborator<A extends Activity> {

    @NonNull
    protected final WeakReference<A> activityRefrence;


    private BaseActivityCollaborator() {
        this.activityRefrence = new WeakReference<>(null);
    }


    public BaseActivityCollaborator(@NonNull final A activity) {
        this.activityRefrence = new WeakReference<>(activity);
    }

}
