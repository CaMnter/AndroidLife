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
     * 实现AIDL生成静态抽象类 IPushMessage.Stub
     */
    private class IPushMessageImpl extends IPushMessage.Stub {
        /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         *
         * @param anInt anInt
         * @param aLong aLong
         * @param aBoolean aBoolean
         * @param aFloat aFloat
         * @param aDouble aDouble
         * @param aString aString
         */
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString)
                throws RemoteException {

        }


        @Override public String onMessage() throws RemoteException {
            return UUID.randomUUID().toString();
        }
    }


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override public void onCreate() {
        super.onCreate();
        this.binder = new IPushMessageImpl();
    }


    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable @Override public IBinder onBind(Intent intent) {
        return this.binder;
    }
}
