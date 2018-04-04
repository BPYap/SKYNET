package com.skynet.main;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.skynet.hotspotdatabase.DatabaseControl;
import com.skynet.location.LocationFetcher;
import com.skynet.map.Map;
import com.skynet.utility.Utility;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private LocationFetcher mLocationFetcher;
    private Map map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Copy map asset to local storage
        File map_file = Utility.copyAssets(getApplicationContext(), "sg.map");
        //Setup mapforge
        map = new Map(this, findViewById(R.id.mapView), map_file);
        map.setBounding_box("1.2837,103.6575,1.4317,104.0007");
        map.setZoomLevelMin(13);
        map.setZoomLevelMax(20);

        // UI toolbar (dunno where)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // UI FAB (Floating Action Button!)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // UI dropdown menu (for navigating to location)
        final Spinner dropDown = (Spinner)findViewById(R.id.spinner);
        dropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tgt = dropDown.getSelectedItem().toString();
                Log.i("Selected item ", tgt);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // UI drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Location service CHUNK
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    // code to handle null location

                    return;
                }

                Location location = locationResult.getLastLocation();
                // code to use location data
                Log.i("Lat:",Double.toString(location.getLatitude()));
                Log.i("Long:",Double.toString(location.getLongitude()));
            }
        };
        mLocationFetcher = LocationFetcher.getInstance(this, locationCallback);

        //Database setup
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        DatabaseControl.getDatabaseControl().refreshDatabase(getApplicationContext());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       if (id == R.id.nav_refresh) {
            // re-fetch the json file
        } else if (id == R.id.nav_about) {
            // static page separate activity
        } else if (id == R.id.nav_settings) {
            // enter setting page
           startActivity(new Intent(MainActivity.this, Settings.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // location service CHUNK (refresh location upon re-enter app)
    @Override
    protected void onResume() {
        super.onResume();
        mLocationFetcher.get_location_update(this); // call to get location update, can put anywhere
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.save_preferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.cleanup();
    }
}
