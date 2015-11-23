package com.camnter.newlife.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.camnter.newlife.service.DownloadIntentService;


/**
 * Description：DownloadReceiver
 * Created by：CaMnter
 * Time：2015-11-22 22:25
 */
public class DownloadReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = "com.camnter.android.intent.download";
    public static final String INTENT_TYPE = "type";
    public static final String INTENT_DATA_IMAGE_URL = "image_url";
    public static final int TYPE_DOWNLOAD_START = 2061;
    public static final int TYPE_DOWNLOAD_SUCCESS = 2062;
    public static final int TYPE_DOWNLOAD_FAILURE = 2063;

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra(INTENT_TYPE, -1);
        if (type == -1) return;
        switch (type) {
            case TYPE_DOWNLOAD_START: {
                String url = intent.getStringExtra(INTENT_DATA_IMAGE_URL);
                DownloadIntentService.startActionDownload(context, url);
                break;
            }
            case TYPE_DOWNLOAD_SUCCESS: {
                break;
            }
            case TYPE_DOWNLOAD_FAILURE: {
                break;
            }
        }
    }

}
