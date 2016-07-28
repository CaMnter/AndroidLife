package com.camnter.effective.test.thread.communication;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description：LooperThread
 * Created by：CaMnter
 */

public class CustomThread extends Thread {

    private AtomicBoolean start = new AtomicBoolean(true);

    private static final ThreadLocal<CopyOnWriteArrayList<Message>> messages = new ThreadLocal<>();


    public CustomThread() {

    }


    public CustomThread(CopyOnWriteArrayList<Message> copyOnWriteArrayList) {
        if (messages.get() == null) {
            messages.set(copyOnWriteArrayList);
        }
    }


    public CopyOnWriteArrayList<Message> getMessages() {
        if (messages.get() == null) {
            messages.set(new CopyOnWriteArrayList<>());
        }
        return messages.get();
    }


    private synchronized void dispatch() {
        for (int i = 0; i < messages.get().size(); i++) {
            Message message = messages.get().get(i);
            message.h.callback(message.msg);
        }
        messages.get().clear();
    }


    @Override public void run() {
        if (messages.get() == null) {
            messages.set(new CopyOnWriteArrayList<>());
        }
        while (true) {
            if (!this.start.get()) {
                return;
            }
            this.dispatch();
        }
    }


    public void quit() {
        this.start.set(false);
        this.interrupt();
    }

}
