package com.camnter.databinding.rxjava.collaborator;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.reactivex.disposables.CompositeDisposable;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public abstract class BaseActivityCollaborator<A extends Activity>
    implements CollaboratorLifeCycle {

    protected final CompositeDisposable disposable;

    public static final String EMPTY_TEXT = "";

    @NonNull
    private final WeakReference<A> activityReference;


    private BaseActivityCollaborator() {
        this.activityReference = new WeakReference<>(null);
        this.disposable = new CompositeDisposable();
    }


    public BaseActivityCollaborator(@NonNull final A activity) {
        this.activityReference = new WeakReference<>(activity);
        this.disposable = new CompositeDisposable();
    }


    @NonNull
    public WeakReference<A> getActivityReference() {
        return this.activityReference;
    }


    @Nullable
    protected A getActivity() {
        return this.activityReference.get();
    }


    protected void clearReference() {
        this.activityReference.clear();
    }


    public String safelyGetText(String text) {
        return TextUtils.isEmpty(text) ? EMPTY_TEXT : text;
    }


    @Override
    public void onDestroy() {
        this.disposable.clear();
    }

}

