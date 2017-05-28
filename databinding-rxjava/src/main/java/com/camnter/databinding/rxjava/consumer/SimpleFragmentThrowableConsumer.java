package com.camnter.databinding.rxjava.consumer;

import android.support.annotation.NonNull;
import android.util.Log;
import com.camnter.databinding.rxjava.fragment.BaseBindingFragment;
import com.camnter.rxjava2.RxThrowable;
import io.reactivex.functions.Consumer;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class SimpleFragmentThrowableConsumer<F extends BaseBindingFragment>
    implements Consumer<Throwable> {

    private static final String TAG = SimpleFragmentThrowableConsumer.class.getSimpleName();

    @NonNull
    private final WeakReference<F> fragmentReference;


    public SimpleFragmentThrowableConsumer(@NonNull final WeakReference<F> fragmentReference) {
        this.fragmentReference = fragmentReference;
    }


    /**
     * Consume the given value.
     *
     * @param throwable the value
     * @throws Exception on error
     */
    @Override
    public void accept(@NonNull Throwable throwable) throws Exception {
        final F fragment = this.fragmentReference.get();
        if (fragment == null) {
            Log.e(TAG, " fragment == null");
            return;
        }
        String message;
        if (throwable instanceof RxThrowable) {
            RxThrowable rxThrowable = (RxThrowable) throwable;
            message = rxThrowable.getResponseMessage();
            fragment.showToast(message);
            Log.e(TAG,
                "[fragment] = " + fragment.getClass().getSimpleName() +
                    "\n[message] = " + message);
            return;
        }
        message = throwable.getMessage();
        Log.e(TAG,
            "[fragment] = " + fragment.getClass().getSimpleName() + "\n[message] = " + message);
        fragment.showToast(message);
    }

}
