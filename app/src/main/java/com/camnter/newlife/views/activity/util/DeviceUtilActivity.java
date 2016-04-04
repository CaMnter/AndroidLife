package com.camnter.newlife.views.activity.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.DeviceUtils;

/**
 * Description：DeviceUtilActivity
 * Created by：CaMnter
 * Time：2015-10-13 17:48
 */
public class DeviceUtilActivity extends BaseAppCompatActivity {

    private TextView deviceIdTV;
    private TextView versionCodeTV;
    private TextView versionNameTV;

    private TextView phoneBrandTV;
    private TextView phoneModelTV;
    private TextView apiLevelTV;
    private TextView apiVersionTV;

    private TextView appProcessIdTV;
    private TextView appNameTV;

    private TextView metaDataTV;

    public static final int REQUEST_READ_PHONE_STATE = 61;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.device_util;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.deviceIdTV = (TextView) this.findViewById(R.id.device_id_tv);
        this.versionCodeTV = (TextView) this.findViewById(R.id.version_code_tv);
        this.versionNameTV = (TextView) this.findViewById(R.id.version_name_tv);

        this.phoneBrandTV = (TextView) this.findViewById(R.id.phone_brand_tv);
        this.phoneModelTV = (TextView) this.findViewById(R.id.phone_model_tv);
        this.apiLevelTV = (TextView) this.findViewById(R.id.phone_api_level_tv);
        this.apiVersionTV = (TextView) this.findViewById(R.id.phone_api_version_tv);

        this.appProcessIdTV = (TextView) this.findViewById(R.id.app_process_id_tv);
        this.appNameTV = (TextView) this.findViewById(R.id.app_name_tv);

        this.metaDataTV = (TextView) this.findViewById(R.id.meta_data_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_PHONE_STATE },
                        REQUEST_READ_PHONE_STATE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            this.setData();
        }
    }


    @SuppressLint("SetTextI18n") private void setData() {
        this.deviceIdTV.setText(DeviceUtils.getDeviceId(this));
        this.versionCodeTV.setText(DeviceUtils.getVersionCode(this));
        this.versionNameTV.setText(DeviceUtils.getVersionName(this));
        this.phoneBrandTV.setText(DeviceUtils.getPhoneBrand());
        this.phoneModelTV.setText(DeviceUtils.getPhoneModel());
        this.apiLevelTV.setText(DeviceUtils.getBuildLevel() + "");
        this.apiVersionTV.setText(DeviceUtils.getBuildVersion());
        this.appProcessIdTV.setText(DeviceUtils.getAppProcessId() + "");
        this.appNameTV.setText(DeviceUtils.getAppProcessName(this, DeviceUtils.getAppProcessId()));
        this.metaDataTV.setText(DeviceUtils.getMetaData(this, "DEBUG"));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    this.setData();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    this.showToast("没有权限访问");
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
