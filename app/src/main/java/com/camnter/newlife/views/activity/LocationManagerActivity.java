package com.camnter.newlife.views.activity;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.camnter.newlife.R;

/**
 * Description：LocationManagerActivity
 * Created by：CaMnter
 * Time：2015-11-28 16:04
 */
public class LocationManagerActivity extends AppCompatActivity {

    private static final String TAG = "LocationManagerActivity";

    private LocationManager locationManager;

    private TextView longitudeTV;
    private TextView latitudeTV;
    private TextView altitudeTV;
    private TextView providersTV;
    private TextView bestProviderTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_location_manager);
        this.initViews();
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
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.longitudeTV.setText(location.getLongitude() + "");
        this.latitudeTV.setText(location.getLatitude() + "");
        this.altitudeTV.setText(location.getAltitude() + "");

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


    private void initViews() {
        this.longitudeTV = (TextView) this.findViewById(R.id.location_longitude_tv);
        this.latitudeTV = (TextView) this.findViewById(R.id.location_latitude_tv);
        this.altitudeTV = (TextView) this.findViewById(R.id.location_altitude_tv);
        this.providersTV = (TextView) this.findViewById(R.id.location_providers_tv);
        this.bestProviderTV = (TextView) this.findViewById(R.id.location_best_provider_tv);
    }

}
