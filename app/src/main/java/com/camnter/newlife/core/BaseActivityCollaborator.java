package com.camnter.newlife.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseActivityCollaborator<A extends Activity> {

    @NonNull
    private final WeakReference<A> activityRefrence;


    public BaseActivityCollaborator(@NonNull final A activity) {
        this.activityRefrence = new WeakReference<>(activity);
    }

}
