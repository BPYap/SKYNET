// How to use this API:
// Create a LocationCallback object
// Get instance of LocationFetcher by mLocationFetcher = LocationFetcher.getInstance(MainActivity, locationCallback)
// Get location update by calling mLocationFetcher.get_location_update(MainActivity)
package com.skynet.location;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public final class LocationFetcher {
    private static LocationFetcher single_instance = null;

    // request code
    public static final int LOCATION_PERMISSION_REQUEST = 0;
    public static final int LOCATION_SETTING_REQUEST = 1;

    // Location settings
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder location_setting_request;
    private SettingsClient settings_client;
    private LocationCallback locationCallback;
    private Location location;

    // Location provider and callback handler
    private FusedLocationProviderClient mFusedLocationClient;

    private LocationFetcher(){}

    private LocationFetcher(AppCompatActivity mainActivity) {
        // Initialize LocationRequest object
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize settings request
        location_setting_request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        settings_client = LocationServices.getSettingsClient(mainActivity);

        // Setup FusedLocationClient and define locationCallback
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    // code to handle null location
                    return;
                }
                location = locationResult.getLastLocation();
                Log.i("Lat:",Double.toString(location.getLatitude()));
                Log.i("Long:",Double.toString(location.getLongitude()));
            }
        };
    }

    public static LocationFetcher getInstance(AppCompatActivity mainActivity)
    {
        if (single_instance == null)
            single_instance = new LocationFetcher(mainActivity);

        return single_instance;
    }

    private void get_location_permission(AppCompatActivity mainActivity) {
        ActivityCompat.requestPermissions(mainActivity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    private void check_location_settings(final AppCompatActivity mainActivity) {
        Task<LocationSettingsResponse> task = settings_client.checkLocationSettings(location_setting_request.build());

        task.addOnSuccessListener(mainActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i("LocationFetcher", "Location settings are satisfied");
                Log.i("LocationFetcher", "Requesting location info from provider");
                try {
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
                catch (SecurityException SecEx){
                    Log.e("LocationFetcher", "Unexpected error occurred!", SecEx);
                }
            }
        });

        task.addOnFailureListener(mainActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Log.i("LocationFetcher", "Location settings not satisfied");
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(mainActivity, LOCATION_SETTING_REQUEST);
                    }
                    catch (IntentSender.SendIntentException sendEx) {}
                }
            }
        });
    }

    public void get_location_update(AppCompatActivity mainActivity) {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            Log.i("LocationFetcher", "No location permission");
            get_location_permission(mainActivity);
        } else {
            Log.i("LocationFetcher", "Location permission obtained");
            Log.i("LocationFetcher", "Checking location settings");
            check_location_settings(mainActivity);
        }
    }

    public double getLatitude() {return location.getLatitude();}
    public  double getLongitude() {return location.getLongitude();}
}
