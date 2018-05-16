package com.camnter.utils.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public final class HandlerThreadFactory {

    private HandlerThreadFactory() {

    }


    public static HandlerThreadWrapper getHandlerThread(@NonNull final String threadName) {
        return new HandlerThreadWrapper(threadName);
    }


    public static class HandlerThreadWrapper {

        private Handler handler;


        public HandlerThreadWrapper(@NonNull final String threadName) {
            final HandlerThread handlerThread = new HandlerThread(threadName);
            handlerThread.start();
            this.handler = new Handler(handlerThread.getLooper());
        }


        public Handler getHandler() {
            return handler;
        }

    }

}
