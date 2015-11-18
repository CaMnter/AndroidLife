package com.camnter.newlife.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.util.UUID;

/**
 * Description：PushMessageService
 * Created by：CaMnter
 * Time：2015-11-17 15:44
 */
public class PushMessageService extends Service {

    private static final String TAG = "MessageService";

    private IPushMessageImpl binder;

    /**
     * AIDL implement
     */
    private class IPushMessageImpl extends IPushMessage.Stub {
        /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         *
         * @param anInt
         * @param aLong
         * @param aBoolean
         * @param aFloat
         * @param aDouble
         * @param aString
         */
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String onMessage() throws RemoteException {
            return UUID.randomUUID().toString();
        }

    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.binder = new IPushMessageImpl();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }
}
