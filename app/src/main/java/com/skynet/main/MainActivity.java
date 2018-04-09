package com.skynet.main;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import com.skynet.hotspotdatabase.DatabaseManager;
import com.skynet.location.LocationFetcher;
import com.skynet.map.Map;
import com.skynet.utility.Utility;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private LocationFetcher mLocationFetcher;
    private Map map;
    private int marker_radius = 200;

    // Activity Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Copy map asset to local storage
        File map_file = Utility.copyAssets(getApplicationContext(), "sg.map");

        // Mapforge setup
        map = new Map(this, findViewById(R.id.mapView), map_file);
        map.setBounding_box("1.2278,103.6088,1.4679,104.0313");
        map.setZoomLevelMin(13);
        map.setZoomLevelMax(20);

        // Database setup
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        DatabaseManager.getDatabaseControl().refreshDatabase(getApplicationContext());

        mLocationFetcher = LocationFetcher.getInstance(this);
        setupUI();
    }

    private void setupUI() {
        // UI toolbar (dunno where)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // UI FAB (Floating Action Button!)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationFetcher.get_location_update(MainActivity.this);
                map.setPosition(mLocationFetcher.getLatitude(), mLocationFetcher.getLongitude());
                map.markme(mLocationFetcher.getLatitude(),
                            mLocationFetcher.getLongitude(), marker_radius);
            }
        });
        // UI dropdown menu (for navigating to location)
        final Spinner dropDown = (Spinner)findViewById(R.id.spinner);
        dropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Double dis_lat = Double.parseDouble(getResources().getStringArray(R.array.districts_lat)
                                    [dropDown.getSelectedItemPosition()]);
                Double dis_long = Double.parseDouble(getResources().getStringArray(R.array.districts_long)
                                    [dropDown.getSelectedItemPosition()]);
                map.setPosition(dis_lat, dis_long);
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
        // UI seekbar (for marker_radius)
        SeekBar radiusSeekbar = (SeekBar)findViewById(R.id.radiusSeekbar);
        radiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int increment = Integer.parseInt(getString(R.string.radius_incr));
                marker_radius = seekBar.getProgress()*increment;
                map.markme(mLocationFetcher.getLatitude(),
                            mLocationFetcher.getLongitude(), marker_radius);
                Log.i("MainActivity", "seekbar set marker_radius to "+Integer.toString(marker_radius));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationFetcher.get_location_update(this);
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

    // UI callback
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
            // show seekbar marker_radius
           SeekBar radiusSeekbar = (SeekBar)findViewById(R.id.radiusSeekbar);
           if(radiusSeekbar.getVisibility()==View.VISIBLE)
           {
               radiusSeekbar.setVisibility(View.INVISIBLE);
           }
           else
           {
               radiusSeekbar.setVisibility(View.VISIBLE);
           }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
