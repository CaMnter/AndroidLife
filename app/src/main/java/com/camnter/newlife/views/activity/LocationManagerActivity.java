package com.camnter.newlife.views.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    private static final int REQUEST_ACCESS_FINE_LOCATION = 60;

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
    @Override protected int getLayoutId() {
        return R.layout.activity_location_manager;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.longitudeTV = (TextView) this.findViewById(R.id.location_longitude_tv);
        this.latitudeTV = (TextView) this.findViewById(R.id.location_latitude_tv);
        this.altitudeTV = (TextView) this.findViewById(R.id.location_altitude_tv);
        this.providersTV = (TextView) this.findViewById(R.id.location_providers_tv);
        this.bestProviderTV = (TextView) this.findViewById(R.id.location_best_provider_tv);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        REQUEST_ACCESS_FINE_LOCATION);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            this.setData();
        }
    }


    @SuppressLint("SetTextI18n") private void setData() {
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                /**
                 * 经度
                 * 纬度
                 * 海拔
                 */
                Log.i(TAG, "Longitude:" + Double.toString(location.getLongitude()));
                Log.i(TAG, "Latitude:" + Double.toString(location.getLatitude()));
                Log.i(TAG, "getAltitude:" + Double.toString(location.getAltitude()));
            }


            @Override public void onStatusChanged(String provider, int status, Bundle extras) {

            }


            @Override public void onProviderEnabled(String provider) {

            }


            @Override public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                locationListener);
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
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
