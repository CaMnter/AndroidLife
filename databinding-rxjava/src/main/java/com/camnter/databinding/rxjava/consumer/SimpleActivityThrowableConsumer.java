package com.camnter.databinding.rxjava.consumer;

import android.support.annotation.NonNull;
import android.util.Log;
import com.camnter.databinding.rxjava.activity.BaseBindingActivity;
import com.camnter.rxjava2.RxThrowable;
import io.reactivex.functions.Consumer;
import java.lang.ref.WeakReference;

/**
 * @author CaMnter
 */

public class SimpleActivityThrowableConsumer<A extends BaseBindingActivity>
    implements Consumer<Throwable> {

    private static final String TAG = SimpleActivityThrowableConsumer.class.getSimpleName();

    @NonNull
    private final WeakReference<A> activityReference;


    public SimpleActivityThrowableConsumer(@NonNull WeakReference<A> activityReference) {
        this.activityReference = activityReference;
    }


    /**
     * Consume the given value.
     *
     * @param throwable the value
     * @throws Exception on error
     */
    @Override
    public void accept(@NonNull Throwable throwable) throws Exception {
        final A activity = this.activityReference.get();
        if (activity == null) {
            Log.e(TAG, " activity == null");
            return;
        }
        String message;
        if (throwable instanceof RxThrowable) {
            RxThrowable rxThrowable = (RxThrowable) throwable;
            message = rxThrowable.getResponseMessage();
            activity.showToast(message);
            Log.e(TAG,
                "[activity] = " + activity.getClass().getSimpleName() +
                    "\n[message] = " + message);
            return;
        }
        message = throwable.getMessage();
        Log.e(TAG,
            "[activity] = " + activity.getClass().getSimpleName() + "\n[message] = " + message);
        activity.showToast(message);
    }

}
