package com.camnter.newlife.utils.permissions.helper;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import com.camnter.newlife.utils.permissions.RationaleDialogFragmentCompat;

/**
 * Implementation of {@link pub.devrel.easypermissions.helper.PermissionHelper} for Support Library
 * host classes.
 */
public abstract class BaseSupportPermissionsHelper<T>
    extends PermissionHelper<T> {

    public BaseSupportPermissionsHelper(@NonNull T host) {
        super(host);
    }


    public abstract FragmentManager getSupportFragmentManager();


    @Override
    @SuppressLint("NewApi")
    public void showRequestPermissionRationale(@NonNull String rationale,
                                               int positiveButton,
                                               int negativeButton,
                                               int requestCode,
                                               @NonNull String... perms) {
        RationaleDialogFragmentCompat
            .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
            .show(getSupportFragmentManager(), RationaleDialogFragmentCompat.TAG);
    }
}
