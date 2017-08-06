package com.camnter.smartrouter;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author CaMnter
 */

public class RouterCenterActivity extends Activity {

    private static final String TAG = RouterCenterActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Uri uri = this.getIntent().getData();
        if (uri != null) {
            final String scheme = SmartRouters.getScheme();
            final String host = SmartRouters.getHost();
            String url = getIntent().getDataString();
            if (uri.getScheme().equals("smart-routers")) {
                if (!TextUtils.isEmpty(host) &&
                    host.equals(uri.getHost())) {
                    url = url.replaceFirst("http", scheme).replace(host + "/", "");
                }
                Log.i(TAG, "[RouterCenterActivity]   [url] = " + url);
                SmartRouters.start(this, url);
            }
        }
        finish();
    }

}
