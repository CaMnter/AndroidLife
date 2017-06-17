package com.camnter.newlife.utils.permissions;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import com.camnter.newlife.utils.permissions.EasyPermissions.PermissionCallbacks;
import com.camnter.newlife.utils.permissions.helper.PermissionHelper;
import java.util.Arrays;

/**
 * Click listener for either {@link RationaleDialogFragment} or {@link
 * RationaleDialogFragmentCompat}.
 */
class RationaleDialogClickListener implements Dialog.OnClickListener {

    private Object mHost;
    private RationaleDialogConfig mConfig;
    private PermissionCallbacks mCallbacks;


    RationaleDialogClickListener(RationaleDialogFragmentCompat compatDialogFragment,
                                 RationaleDialogConfig config,
                                 PermissionCallbacks callbacks) {

        mHost = compatDialogFragment.getParentFragment() != null
                ? compatDialogFragment.getParentFragment()
                : compatDialogFragment.getActivity();

        mConfig = config;
        mCallbacks = callbacks;
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    RationaleDialogClickListener(RationaleDialogFragment dialogFragment,
                                 RationaleDialogConfig config,
                                 PermissionCallbacks callbacks) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mHost = dialogFragment.getParentFragment() != null ?
                    dialogFragment.getParentFragment() :
                    dialogFragment.getActivity();
        } else {
            mHost = dialogFragment.getActivity();
        }

        mConfig = config;
        mCallbacks = callbacks;
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            if (mHost instanceof Fragment) {
                PermissionHelper.newInstance((Fragment) mHost).directRequestPermissions(
                    mConfig.requestCode, mConfig.permissions);
            } else if (mHost instanceof android.app.Fragment) {
                PermissionHelper.newInstance((android.app.Fragment) mHost).directRequestPermissions(
                    mConfig.requestCode, mConfig.permissions);
            } else if (mHost instanceof Activity) {
                PermissionHelper.newInstance((Activity) mHost).directRequestPermissions(
                    mConfig.requestCode, mConfig.permissions);
            } else {
                throw new RuntimeException("Host must be an Activity or Fragment!");
            }
        } else {
            notifyPermissionDenied();
        }
    }


    private void notifyPermissionDenied() {
        if (mCallbacks != null) {
            mCallbacks.onPermissionsDenied(mConfig.requestCode,
                Arrays.asList(mConfig.permissions));
        }
    }
}
