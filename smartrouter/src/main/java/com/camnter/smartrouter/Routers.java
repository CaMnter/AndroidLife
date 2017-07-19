package com.camnter.smartrouter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public final class Routers {

    private static final String scheme = "routers";


    public static String getScheme() {
        return scheme;
    }


    public static void start(@NonNull final Context context,
                             @NonNull final String url) {
        // TODO
    }


    public static void startForResult(@NonNull final Activity activity,
                                      @NonNull final String url,
                                      final int requestCode) {
        // TODO
    }


    public static void startForResult(@NonNull final Fragment fragment,
                                      @NonNull final String url,
                                      final int requestCode) {
        // TODO
    }


    public static void startForResult(@NonNull final android.support.v4.app.Fragment fragment,
                                      @NonNull final String url,
                                      final int requestCode) {
        // TODO
    }

}
