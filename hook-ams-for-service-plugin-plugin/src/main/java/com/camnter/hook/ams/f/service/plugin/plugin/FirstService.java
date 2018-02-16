package com.camnter.hook.ams.f.service.plugin.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author CaMnter
 */

public class FirstService extends Service {

    private static final String TAG = FirstService.class.getSimpleName();


    @Override
    public void onCreate() {
        Log.d(TAG, "[FirstService]   [onCreate]  called with");
        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG,
            "[FirstService]   [onStart]  called with intent = [" + intent + "], startId = [" +
                startId + "]");
        super.onStart(intent, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "[FirstService]   [onDestroy]  called with");
        super.onDestroy();
    }

}
