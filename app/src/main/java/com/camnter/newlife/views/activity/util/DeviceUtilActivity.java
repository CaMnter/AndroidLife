package com.camnter.newlife.views.activity.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.utils.DeviceUtil;


/**
 * Description：DeviceUtilActivity
 * Created by：CaMnter
 * Time：2015-10-13 17:48
 */
public class DeviceUtilActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.device_util);

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

        this.deviceIdTV.setText(DeviceUtil.getDeviceId(this));
        this.versionCodeTV.setText(DeviceUtil.getVersionCode(this));
        this.versionNameTV.setText(DeviceUtil.getVersionName(this));

        this.phoneBrandTV.setText(DeviceUtil.getPhoneBrand());
        this.phoneModelTV.setText(DeviceUtil.getPhoneModel());
        this.apiLevelTV.setText(DeviceUtil.getBuildLevel() + "");
        this.apiVersionTV.setText(DeviceUtil.getBuildVersion());
        this.appProcessIdTV.setText(DeviceUtil.getAppProcessId() + "");
        this.appNameTV.setText(DeviceUtil.getAppProcessName(this, DeviceUtil.getAppProcessId()));
        this.metaDataTV.setText(DeviceUtil.getMetaData(this,"DEBUG"));
    }
}
