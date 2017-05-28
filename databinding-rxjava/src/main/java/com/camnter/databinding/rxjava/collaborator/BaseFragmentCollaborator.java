package com.camnter.databinding.rxjava.collaborator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import io.reactivex.disposables.CompositeDisposable;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class BaseFragmentCollaborator<F extends Fragment> implements CollaboratorLifeCycle {

  protected final CompositeDisposable disposable;

  public static final String EMPTY_TEXT = "";

  @NonNull
  private final WeakReference<F> fragmentReference;


  private BaseFragmentCollaborator() {
    this.fragmentReference = new WeakReference<>(null);
    this.disposable = new CompositeDisposable();
  }


  public BaseFragmentCollaborator(@NonNull final F fragment) {
    this.fragmentReference = new WeakReference<>(fragment);
    this.disposable = new CompositeDisposable();
  }

  public WeakReference<F> getFragmentReference() {
    return this.fragmentReference;
  }

  @Nullable
  protected F getFragment() {
    return this.fragmentReference.get();
  }

  protected void clearReference() {
    this.fragmentReference.clear();
  }

  public String safelyGetText(String text) {
    return TextUtils.isEmpty(text) ? EMPTY_TEXT : text;
  }

  @Override
  public void onDestroy() {
    this.disposable.clear();
  }


}
