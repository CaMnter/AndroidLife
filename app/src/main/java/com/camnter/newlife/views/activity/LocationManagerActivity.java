package com.camnter.newlife.views.activity;

import android.content.Context;
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

    private TextView longitudeTV;
    private TextView latitudeTV;
    private TextView altitudeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_location_manager);
        this.initViews();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.longitudeTV.setText(location.getLongitude() + "");
        this.latitudeTV.setText(location.getLatitude() + "");
        this.altitudeTV.setText(location.getAltitude() + "");
    }

    private void initViews() {
        this.longitudeTV = (TextView) this.findViewById(R.id.location_longitude_tv);
        this.latitudeTV = (TextView) this.findViewById(R.id.location_latitude_tv);
        this.altitudeTV = (TextView) this.findViewById(R.id.location_altitude_tv);
    }

}
