package com.skynet.main;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Environment;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import hotspotdatabase.DatabaseControl;
import location.LocationFetcher;
import utility.Utility;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.hills.HillsRenderConfig;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private LocationFetcher mLocationFetcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        //Copy map asset to local storage
        Utility.copyAssets(getApplicationContext());
        //mapsforge setup
        createSharedPreferences();
        createMapViews();
        createTileCaches();
        checkPermissionsAndCreateLayersAndControls();
        setTitle(getClass().getSimpleName());
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
    // mapsforge CHUNK
    protected MapView mapView;
    protected PreferencesFacade preferencesFacade;
    protected XmlRenderThemeStyleMenu renderThemeStyleMenu;
    protected List<TileCache> tileCaches = new ArrayList<TileCache>();
    private static final double LONG = 103.851959;
    private static final double LAT = 1.290270;

    protected int getLayoutId() {
        return R.layout.mapviewer;
    }

    protected int getMapViewId() {
        return R.id.mapView;
    }

    protected String getMapFileName() {
        return "sg.map";
    }

    protected XmlRenderTheme getRenderTheme() {
        return InternalRenderTheme.OSMARENDER;
    }

    protected void createLayers() {
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches.get(0),
                this.mapView.getModel().mapViewPosition, getMapFile(), getRenderTheme(), false, true, false);
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
//        for(double i=0.00001;i<0.01;i=i+0.00001){
//            addMarker(i);
//        }
    }

    protected void createTileCaches() {
        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor()));
    }

    protected void createControls() {
        initializePosition(mapView.getModel().mapViewPosition);
    }

    protected float getMaxTextWidthFactor() {
        return 0.7f;
    }

    protected byte getZoomLevelDefault() {
        return (byte) 13;
    }

    protected byte getZoomLevelMin() {
        return (byte) 13;
    }

    protected byte getZoomLevelMax() { return (byte) 20; }

    protected void createMapViews() {
        mapView = getMapView();
        mapView.getModel().init(this.preferencesFacade);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(hasZoomControls());
        mapView.getMapZoomControls().setAutoHide(isZoomControlsAutoHide());
        mapView.getMapZoomControls().setZoomLevelMin(getZoomLevelMin());
        mapView.getMapZoomControls().setZoomLevelMax(getZoomLevelMax());
        mapView.getModel().mapViewPosition.setMapLimit(BoundingBox.fromString("1.2837,103.6575,1.4317,104.0007"));
    }

    protected void createSharedPreferences() {
        this.preferencesFacade = new AndroidPreferences(this.getSharedPreferences(getPersistableId(), MODE_PRIVATE));
    }

    protected MapPosition getInitialPosition() {
        return new MapPosition(getMapFile().boundingBox().getCenterPoint(), (byte)13 );
        // return new MapPosition(new LatLong(LAT, LONG), getZoomLevelDefault());
    }

    protected File getMapFileDirectory() {
        Log.d("Directory is:",getApplicationContext().getExternalFilesDir(null).toString());
        return getApplicationContext().getExternalFilesDir(null);
    }

    protected MapDataStore getMapFile() {
        return new MapFile(new File(getMapFileDirectory(), this.getMapFileName()));
    }

    protected String getPersistableId() {
        return this.getClass().getSimpleName();
    }

    protected float getScreenRatio() {
        return 1.0f;
    }

    protected boolean hasZoomControls() {
        return true;
    }

    protected boolean isZoomControlsAutoHide() {
        return true;
    }

    protected IMapViewPosition initializePosition(IMapViewPosition mvp) {
        LatLong center = mvp.getCenter();

        if (center.equals(new LatLong(0, 0))) {
            mvp.setMapPosition(this.getInitialPosition());
        }
        mvp.setZoomLevelMax(getZoomLevelMax());
        mvp.setZoomLevelMin(getZoomLevelMin());
        return mvp;
    }

    protected void checkPermissionsAndCreateLayersAndControls() {
        createLayers();
        createControls();
    }


    @Override
    protected void onPause() {
        mapView.getModel().save(this.preferencesFacade);
        this.preferencesFacade.save();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        tileCaches.clear();
        super.onDestroy();
    }

    protected void purgeTileCaches() {
        for (TileCache tileCache : tileCaches) {
            tileCache.purge();
        }
        tileCaches.clear();
    }

    protected void redrawLayers() {
        mapView.getLayerManager().redrawLayers();
    }

    protected void setContentView() {
        setContentView(mapView);
    }

    protected MapView getMapView() {
        setContentView(getLayoutId());
        return (MapView) findViewById(getMapViewId());
    }

    protected HillsRenderConfig getHillsRenderConfig() {
        return null;
    }
}
