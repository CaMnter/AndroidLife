package com.camnter.hook.ams.f.service.plugin.host;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.camnter.hook.ams.f.service.plugin.host.hook.AMSHooker;

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
        // 分发 Service Action
        final Intent rawIntent = intent.getParcelableExtra(AMSHooker.EXTRA_TARGET_INTENT);
        if (rawIntent == null) {
            Log.d(TAG, "[ProxyService]   [onStart]   no rawIntent");
            super.onStart(intent, startId);
            return;
        }
        final String action = rawIntent.getStringExtra(AMSHooker.EXTRA_TARGET_INTENT_ACTION);
        if (!TextUtils.isEmpty(action)) {
            Log.d(TAG, "[ProxyService]   [onStart]   [Action] = " + action);
            if (AMSHooker.INTENT_ACTION_STOP.equals(action)) {
                ProxyServiceManager.getInstance().onStop(rawIntent);
            } else if (AMSHooker.INTENT_ACTION_START.equals(action)) {
                ProxyServiceManager.getInstance().onStart(rawIntent, startId);
            }
        } else {
            Log.d(TAG, "[ProxyService]   [onStart]   no action");
        }
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
