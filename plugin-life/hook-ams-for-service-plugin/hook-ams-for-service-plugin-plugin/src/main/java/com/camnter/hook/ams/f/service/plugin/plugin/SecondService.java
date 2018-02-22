package com.camnter.hook.ams.f.service.plugin.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author CaMnter
 */

public class SecondService extends Service {

    private static final String TAG = SecondService.class.getSimpleName();


    @Override
    public void onCreate() {
        Log.d(TAG, "[SecondService]   [onCreate]  called with");
        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG,
            "[SecondService]   [onStart]  called with intent = [" + intent + "], startId = [" +
                startId + "]");
        super.onStart(intent, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "[SecondService]   [onDestroy]  called with");
        super.onDestroy();
    }

}
