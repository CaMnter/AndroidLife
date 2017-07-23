package com.camnter.smartrouter.core;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public interface Filter {

    String map(@NonNull final String url);

    boolean start(Context context, String url);

    boolean startForResult(Activity activity, String url, int requestCode);

    boolean startForResult(@NonNull final Fragment fragment, String url, int requestCode);

    boolean startForResult(android.support.v4.app.Fragment fragment, String mapUrl, int requestCode);
}
