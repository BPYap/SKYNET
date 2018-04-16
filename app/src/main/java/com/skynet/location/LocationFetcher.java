/* How to use this package:
1. Add the following permission under AndroidManifest.xml <manifest>:
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
2. Copy the following code below under AndroidManifest.xml <application>:
        <activity
            android:theme="@android:style/Theme.Translucent.NoTitleBar"
            android:name="com.skynet.location.LocationHelper"
            android:parentActivityName="insert your MainActivity class">
        </activity>
3. Invoke startActivity(new Intent(MainActivity.this, LocationFetcher.class)); to request location
4. Invoke LocationHelper.hasData() to check location data availability
5. Invoke LocationHelper.getLatitude() or LocationHelper.getLongitude() for coordinates
*/
package com.skynet.location;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public final class LocationFetcher extends Activity{
    // request code
    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_SETTING_REQUEST = 1;

    private static boolean hasData = false;
    private static boolean setupFinished = false;
    private static Location location;

    // Location settings
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder location_setting_request;
    private SettingsClient settings_client;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    protected LocationFetcher(){}

    public static boolean hasData() {return hasData;}
    public static double getLatitude() {return location.getLatitude();}
    public static double getLongitude() {return location.getLongitude();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(LocationFetcher.setupFinished) {
            finish();
            return;
        }
        Log.i("LocationFetcher", "Initializing LocationFetcher");
        // Initialize LocationRequest object
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize settings request
        location_setting_request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        settings_client = LocationServices.getSettingsClient(this);

        // Setup FusedLocationClient and define locationCallback
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    // code to handle null location
                    return;
                }
                LocationFetcher.location = locationResult.getLastLocation();
                LocationFetcher.hasData = true;
                Log.i("Lat:",Double.toString(LocationFetcher.location.getLatitude()));
                Log.i("Long:",Double.toString(LocationFetcher.location.getLongitude()));
            }
        };
        request_update();
    }

    private void get_location_permission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    private void check_location_settings() {
        Task<LocationSettingsResponse> task = settings_client.checkLocationSettings(location_setting_request.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i("LocationFetcher", "Location settings are satisfied");
                try {
                    Log.i("LocationFetcher", "Requesting location info from provider");
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    LocationFetcher.setupFinished = true;
                    finish();
                }
                catch (SecurityException SecEx){
                    Log.e("LocationFetcher", "Unexpected error occurred", SecEx);
                }
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Log.i("LocationFetcher", "Location settings not satisfied");
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(LocationFetcher.this, LOCATION_SETTING_REQUEST);
                    }
                    catch (IntentSender.SendIntentException sendEx) {}
                }
            }
        });
    }

    private void request_update() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            Log.i("LocationFetcher", "No location permission");
            get_location_permission();
        } else {
            Log.i("LocationFetcher", "Location permission obtained");
            Log.i("LocationFetcher", "Checking location settings");
            check_location_settings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                Log.i("LocationFetcher", "invoke permission result handler.");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    check_location_settings();
                } else {
                    Toast.makeText(this,
                            "Location permission is required for this function.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LOCATION_SETTING_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this,
                                "High accuracy setting is required for this function.",
                                Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    case Activity.RESULT_OK:
                        request_update();
                        break;
                }
                break;
        }
    }


}
