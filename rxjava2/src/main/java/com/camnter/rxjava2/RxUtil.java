
package com.camnter.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

/**
 * @author CaMnter
 */
public class RxUtil {

    private static <T> FlowableTransformer<T, T> createIOToMainThreadScheduler() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(
                        Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


    public static <T> FlowableTransformer<T, T> applyIOToMainThreadSchedulers() {
        return createIOToMainThreadScheduler();
    }

}
