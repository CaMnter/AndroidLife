package com.camnter.smartrouter.core;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public interface Core {

    void start(@NonNull final Context context);

    void startForResult(@NonNull final Activity activity,
                        final int requestCode);

    void startForResult(@NonNull final Fragment fragment,
                        final int requestCode);

    void startForResult(@NonNull final android.support.v4.app.Fragment fragment,
                        final int requestCode);

}
