package com.camnter.smartrouter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.camnter.smartrouter.core.Core;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CaMnter
 */

public class BaseActivityRouter implements Core {

    @NonNull
    private final String host;

    @NonNull
    private final Map<String, String> params;


    public BaseActivityRouter(@NonNull final String host) {
        this.host = host;
        this.params = new HashMap<>();
    }


    private String createUrl() {
        final StringBuilder builder = new StringBuilder();
        builder
            .append(SmartRouters.getScheme())
            .append("://")
            .append(this.host);

        final int maxIndex = this.params.size() - 1;
        int index = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                continue;
            }
            if (index == 0) {
                builder.append('?');
            }
            try {
                builder
                    .append(key)
                    .append('=')
                    .append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (index < maxIndex) {
                builder.append('&');
            }
            index++;
        }
        return builder.toString();
    }


    @Override
    public boolean start(@NonNull final Context context) {
        return SmartRouters.start(context, this.createUrl());
    }


    @Override
    public boolean startForResult(@NonNull final Activity activity,
                                  final int requestCode) {
        return SmartRouters.startForResult(activity, this.createUrl(), requestCode);
    }


    @Override
    public boolean startForResult(@NonNull final Fragment fragment,
                                  final int requestCode) {
        return SmartRouters.startForResult(fragment, this.createUrl(), requestCode);
    }


    @Override
    public boolean startForResult(@NonNull final android.support.v4.app.Fragment fragment,
                                  final int requestCode) {
        return SmartRouters.startForResult(fragment, this.createUrl(), requestCode);
    }


    protected String put(@NonNull final String key, @NonNull final String value) {
        return params.put(key, value);
    }


    protected String put(@NonNull final String key, final double value) {
        return params.put(key, String.valueOf(value));
    }


    protected String put(@NonNull final String key, final float value) {
        return params.put(key, String.valueOf(value));
    }


    protected String put(@NonNull final String key, final int value) {
        return params.put(key, String.valueOf(value));
    }


    protected String put(@NonNull final String key, final boolean value) {
        return params.put(key, String.valueOf(value));
    }

}
