package com.camnter.newlife.views.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：LocationManagerActivity
 * Created by：CaMnter
 * Time：2015-11-28 16:04
 */
public class LocationManagerActivity extends BaseAppCompatActivity {

    private static final String TAG = "LocationManagerActivity";

    private LocationManager locationManager;

    private TextView longitudeTV;
    private TextView latitudeTV;
    private TextView altitudeTV;
    private TextView providersTV;
    private TextView bestProviderTV;

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_location_manager;
    }

    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.longitudeTV = (TextView) this.findViewById(R.id.location_longitude_tv);
        this.latitudeTV = (TextView) this.findViewById(R.id.location_latitude_tv);
        this.altitudeTV = (TextView) this.findViewById(R.id.location_altitude_tv);
        this.providersTV = (TextView) this.findViewById(R.id.location_providers_tv);
        this.bestProviderTV = (TextView) this.findViewById(R.id.location_best_provider_tv);
    }

    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }

    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                /**
                 * 经度
                 * 纬度
                 * 海拔
                 */
                Log.i(TAG, "Longitude:" + Double.toString(location.getLongitude()));
                Log.i(TAG, "Latitude:" + Double.toString(location.getLatitude()));
                Log.i(TAG, "getAltitude:" + Double.toString(location.getAltitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            this.longitudeTV.setText(location.getLongitude() + "");
            this.latitudeTV.setText(location.getLatitude() + "");
            this.altitudeTV.setText(location.getAltitude() + "");
        }
        this.getProviders();
        this.getBestProvider();
    }

    /**
     * 获取全部的provider
     */
    private void getProviders() {
        String providers = "";
        for (String provider : this.locationManager.getAllProviders()) {
            providers += provider + " ";
        }
        this.providersTV.setText(providers);
    }

    /**
     * 获取以下条件下，最合适的provider
     */
    private void getBestProvider() {
        Criteria criteria = new Criteria();
        // 精度高
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 低消耗
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 海拔
        criteria.setAltitudeRequired(true);
        // 速度
        criteria.setSpeedRequired(true);
        // 费用
        criteria.setCostAllowed(false);
        String provider = locationManager.getBestProvider(criteria, false); //false是指不管当前适配器是否可用
        this.bestProviderTV.setText(provider);
    }

}
