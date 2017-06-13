package com.camnter.rxjava2;

import android.support.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * @author CaMnter
 */


/**
 * RxBus.get().post(Integer)
 * RxBus.get()
 * .receive(Integer.class)
 * .compose(RxUtil.<Integer>applyMainToMainThreadSchedulers())
 * .subscribe(...)
 *
 * RxBus.get().post(OtherObject)
 * RxBus.get()
 * .receive(OtherObject.class)
 * .compose(RxUtil.<OtherObject>applyMainToMainThreadSchedulers())
 * .subscribe(...)
 */
public final class RxBus {

    private final FlowableProcessor<Object> bus;


    private static class Instance {
        private static final RxBus BUS = new RxBus();
    }


    private RxBus() {
        this.bus = PublishProcessor.create().toSerialized();
    }


    public static RxBus get() {
        return Instance.BUS;
    }


    public void post(@NonNull final Object event) {
        this.bus.onNext(event);
    }


    public <T> Flowable<T> receive(Class<T> targetClass) {
        return this.bus.ofType(targetClass);
    }


    public boolean hasSubscribers() {
        return this.bus.hasSubscribers();
    }

}
