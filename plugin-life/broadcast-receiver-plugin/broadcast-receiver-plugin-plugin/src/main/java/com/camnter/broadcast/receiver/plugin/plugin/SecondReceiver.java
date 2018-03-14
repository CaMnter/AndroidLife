package com.camnter.broadcast.receiver.plugin.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @author CaMnter
 */

public class SecondReceiver extends BroadcastReceiver {

    private static final String TAG = SecondReceiver.class.getSimpleName();

    public static final String INTENT_EXTRA_MESSAGE = "message";


    @Override
    public void onReceive(Context context, Intent intent) {
        final String message = intent.getStringExtra(INTENT_EXTRA_MESSAGE);
        Toast.makeText(context, TAG + ":" + message, Toast.LENGTH_SHORT).show();
    }

}
