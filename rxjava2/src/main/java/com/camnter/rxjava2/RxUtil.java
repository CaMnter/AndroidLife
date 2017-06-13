
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
public final class RxUtil {

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


    private static <T> FlowableTransformer<T, T> createMainToMainThreadScheduler() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(
                        Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


    private static <T> FlowableTransformer<T, T> createIOToIOThreadScheduler() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(
                        Schedulers.computation())
                    .observeOn(Schedulers.io());
            }
        };
    }


    public static <T> FlowableTransformer<T, T> applyIOToMainThreadSchedulers() {
        return createIOToMainThreadScheduler();
    }


    public static <T> FlowableTransformer<T, T> applyMainToMainThreadSchedulers() {
        return createMainToMainThreadScheduler();
    }


    public static <T> FlowableTransformer<T, T> applyIOToIOThreadSchedulers() {
        return createIOToIOThreadScheduler();
    }

}
