package com.gradient.mapbox.mapboxgradient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Base activity holds methods, that can be used in many activities and that are tighted to activity events like onPermissionResult ..
 */
@SuppressLint("Registered")
class BaseActivity extends AppCompatActivity {
    private final static String TAG = BaseActivity.class.getSimpleName();

    // GPS API and settings
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private static final long FASTEST_INTERVAL = 2000; /* 2 sec */

    // GPS permission variables
    private GPSPermissionCallback gpsPermissionCallback;
    private static final int REQUEST_FINE_LOCATION = 12;
    private static final int  REQ_TURN_ON_GPS_PROVIDER5 = 13;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void startFragment(Fragment fragment, int fragmentContainer) {
        // check if there is a same fragment cust loaded in order to prevent backstack navigation to same fragment
        Fragment oldFragment = this.getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
        if (oldFragment != null) {
            this.getSupportFragmentManager().beginTransaction().remove(oldFragment).commit();
        }

        // Open fragment
        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(fragmentContainer, fragment, fragment.getClass().getSimpleName())
                .addToBackStack( fragment.getClass().getSimpleName() ).commit();
    }

    /**
     * A callback which holds the location listener
     */
    private LocationCallback locationProviderCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()");

        checkGPSPermissions(new GPSPermissionCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGrandeted() {
                // All permissions are fine, proceeding to get location upates

                // Create the location request to start receiving updates
                mLocationRequest = new LocationRequest();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

                // Create LocationSettingsRequest object using location request
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                builder.addLocationRequest(mLocationRequest);
                LocationSettingsRequest locationSettingsRequest = builder.build();

                // Check whether location settings are satisfied
                // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
                SettingsClient settingsClient = LocationServices.getSettingsClient(BaseActivity.this);
                settingsClient.checkLocationSettings(locationSettingsRequest);

                // new Google API SDK v11 uses getFusedLocationProviderClient(this)
                getFusedLocationProviderClient(BaseActivity.this)
                        .requestLocationUpdates(mLocationRequest, locationProviderCallback, Looper.myLooper());
            }

            @Override
            public void onRefused() {
                Toast.makeText(BaseActivity.this, R.string.gps_unavailable, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "location permissions not granted");
            }
        });
    }

    public void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()");

        if (locationProviderCallback != null) {
            getFusedLocationProviderClient(BaseActivity.this).removeLocationUpdates(locationProviderCallback);
        }
    }

    /**
     * Request last location, which would probably be a cached location some time ago
     */
    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // GPS location can be null if GPS is switched off
                    if (location != null) {
                        // Post location to the location listener
                        onLocationChanged(location);
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }



    /**
     * GPS permission and device GPS on/off handling methods
     */
    public interface GPSPermissionCallback {
        void onGrandeted();
        void onRefused();
    }
    public void checkGPSPermissions(GPSPermissionCallback callback) {
        Log.d(TAG, "checkGPSPermissions()");

        gpsPermissionCallback = callback;

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check GPS access permissions
            Log.d(TAG, "NO PERMISSION to access GPS, requesting");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION);
        } else {

            // Check if device GPS is turned ON
            LocationRequest mLocationRequest = LocationRequest.create();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest)
                    .setAlwaysShow(true);

            Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
            result.addOnCompleteListener(task -> {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location requests here.
                    Log.d(TAG, "GPS is on ");

                    if (gpsPermissionCallback != null) gpsPermissionCallback.onGrandeted();
                    else Log.e(TAG, "no location callbacks registered");

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                Log.d(TAG, "Darau dialoga GPS ijungimui");

                                startIntentSenderForResult(resolvable.getResolution().getIntentSender(), REQ_TURN_ON_GPS_PROVIDER5, null, 0, 0, 0, null);

                            } catch (ClassCastException e) {
                                // Ignore the error.
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            if (gpsPermissionCallback != null) gpsPermissionCallback.onRefused();
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode: " + requestCode);

        switch (requestCode) {
            case REQ_TURN_ON_GPS_PROVIDER5:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "RESULT_OK");
                        checkGPSPermissions(gpsPermissionCallback);
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.d(TAG, "Not given access");
                        if (gpsPermissionCallback != null)
                            gpsPermissionCallback.onRefused();

                        break;
                }

            default: super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() requestCode: " + requestCode);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "REQ_GPS_PERMISSION Granted");
                    startLocationUpdates();
                } else {
                    Log.d(TAG, "REQ_GPS_PERMISSION NOT Granted");
                    if (gpsPermissionCallback != null) gpsPermissionCallback.onRefused();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Method which receives the actual location data from location from locationProviderCallback.
     * This method should be overwritten in the actual activity to receive locations
     * @param location
     */
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged(): " + location);
    }

}
