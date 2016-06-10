package com.google.android.agera;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import java.lang.ref.WeakReference;

/**
 * Shared per-thread worker Handler behind internal logic of various Agera classes.
 *
 * Agera 内的工作 Handler
 */
final class WorkerHandler extends Handler {
    /*
     * BaseObservable.observableActivated()
     */
    static final int MSG_FIRST_ADDED = 0;
    /*
     * BaseObservable.observableDeactivated()
     */
    static final int MSG_LAST_REMOVED = 1;
    /*
     * BaseObservable.sendUpdate()
     */
    static final int MSG_UPDATE = 2;
    /*
     * Updatable.update()
     */
    static final int MSG_CALL_UPDATABLE = 3;
    /*
     * CompiledRepository.maybeStartFlow()
     */
    static final int MSG_CALL_MAYBE_START_FLOW = 4;
    /*
     * CompiledRepository.acknowledgeCancel()
     */
    static final int MSG_CALL_ACKNOWLEDGE_CANCEL = 5;

    /*
     * ThreadLocal 存放
     * 当前线程 的一个 WorkerHandler 的弱引用
     */
    private static final ThreadLocal<WeakReference<WorkerHandler>> handlers = new ThreadLocal<>();

    /*
     * 定义一个 IdentityMultiMap
     * 用于存放 观察者，和该观察者 对应的  被观察者的 token
     */
    @NonNull
    private final IdentityMultiMap<Updatable, Object> updatableObservable;


    /**
     * 从 ThreadLocal 取出 当前线程的 一个 WorkerHandler 的弱引用
     * 没有的话，进行实例化，添加到 ThreadLocal 中存储，并返回
     *
     * @return WorkerHandler
     */
    @NonNull
    static WorkerHandler workerHandler() {
        final WeakReference<WorkerHandler> handlerReference = handlers.get();
        WorkerHandler handler = handlerReference != null ? handlerReference.get() : null;
        if (handler == null) {
            handler = new WorkerHandler();
            handlers.set(new WeakReference<>(handler));
        }
        return handler;
    }


    private WorkerHandler() {
        this.updatableObservable = new IdentityMultiMap<>();
    }


    /**
     * 删除 一个 观察者
     *
     * @param updatable 观察者
     * @param token 观察者 对应的 被观察者 的 token
     */
    synchronized void removeUpdatable(@NonNull final Updatable updatable,
                                      @NonNull final Object token) {
        updatableObservable.removeKeyValuePair(updatable, token);
    }


    /**
     * 只有 updatableObservable 内不存在 此次的 updatable 作为 key
     * 就通知观察者 Updatable.update()
     *
     * @param updatable updatable
     * @param token 观察者 对应的 被观察者 的 token
     */
    synchronized void update(@NonNull final Updatable updatable, @NonNull final Object token) {
        if (updatableObservable.addKeyValuePair(updatable, token)) {
            obtainMessage(WorkerHandler.MSG_CALL_UPDATABLE, updatable).sendToTarget();
        }
    }


    /**
     * 分发消息
     *
     * @param message message
     */
    @Override
    public void handleMessage(final Message message) {
        switch (message.what) {
            case MSG_UPDATE:
                ((BaseObservable) message.obj).sendUpdate();
                break;
            case MSG_FIRST_ADDED:
                ((BaseObservable) message.obj).observableActivated();
                break;
            case MSG_LAST_REMOVED:
                ((BaseObservable) message.obj).observableDeactivated();
                break;
            case MSG_CALL_UPDATABLE:
                final Updatable updatable = (Updatable) message.obj;
                updatableObservable.removeKey(updatable);
                updatable.update();
                break;
            case MSG_CALL_MAYBE_START_FLOW:
                ((CompiledRepository) message.obj).maybeStartFlow();
                break;
            case MSG_CALL_ACKNOWLEDGE_CANCEL:
                ((CompiledRepository) message.obj).acknowledgeCancel();
                break;
            default:
        }
    }
}
