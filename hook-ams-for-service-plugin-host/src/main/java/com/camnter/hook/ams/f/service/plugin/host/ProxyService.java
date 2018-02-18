package com.camnter.hook.ams.f.service.plugin.host;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Refer form http://weishu.me/2016/05/11/understand-plugin-framework-service/
 *
 * @author CaMnter
 */

public class ProxyService extends Service {

    private static final String TAG = ProxyService.class.getSimpleName();


    @Override
    public void onCreate() {
        Log.d(TAG, "[ProxyService]   [onCreate]");
        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "[ProxyService]   [onStart]");
        // 分发Service
        ProxyServiceManager.getInstance().onStart(intent, startId);
        super.onStart(intent, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "[ProxyService]   [onBind]");
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "[ProxyService]   [onDestroy]");
        super.onDestroy();
    }

}
