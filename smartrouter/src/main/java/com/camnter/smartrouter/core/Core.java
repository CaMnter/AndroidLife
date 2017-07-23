package com.camnter.smartrouter.core;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public interface Core {

    boolean start(@NonNull final Context context);

    boolean startForResult(@NonNull final Activity activity,
                           final int requestCode);

    boolean startForResult(@NonNull final Fragment fragment,
                           final int requestCode);

    boolean startForResult(@NonNull final android.support.v4.app.Fragment fragment,
                           final int requestCode);

}
