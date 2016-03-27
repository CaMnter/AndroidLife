package me.drakeet.newlife;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by drakeet(http://drakeet.me)
 * Date: 16/3/27 12:12
 */
public final class RxBus {

    private static RxBus sBus;


    public static synchronized RxBus getInstance() {
        if (sBus == null) {
            sBus = new RxBus();
        }
        return sBus;
    }


    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());


    public RxBus() {
        // No instances.
    }


    public void send(Object o) {
        _bus.onNext(o);
    }


    public Observable<Object> toObserverable() {
        return _bus;
    }


    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}