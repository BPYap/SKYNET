package com.skynet.map;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.skynet.main.R;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Map {
    private String bounding_box;
    private byte default_zoom_level = 13;
    private byte min_zoom_level;
    private byte max_zoom_level;

    private Activity activity;
    private MapView mapView;
    private MapFile mapFile;
    private PreferencesFacade preferencesFacade;
    private List<TileCache> tileCaches = new ArrayList<>();

    private MyLocationOverlay myLocationOverlay;

    public Map(AppCompatActivity activity, View view, File map_file) {
        this.activity = activity;
        mapFile = new MapFile(map_file);
        createSharedPreferences(activity);
        createMapViews(view);
        createTileCaches(activity);
        createLayers();
        createControls();
    }

    private void createSharedPreferences(AppCompatActivity activity) {
        this.preferencesFacade = new AndroidPreferences(
                activity.getSharedPreferences(activity.getClass().getSimpleName(), MODE_PRIVATE));
    }

    private void createMapViews(View view) {
        mapView = (MapView) view;
        mapView.getModel().init(this.preferencesFacade);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getMapZoomControls().setAutoHide(true);
    }

    private void createTileCaches(AppCompatActivity activity) {
        this.tileCaches.add(AndroidUtil.createTileCache(
                activity, activity.getClass().getSimpleName(),
                this.mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor()));
    }

    private void createLayers() {
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(
                this.tileCaches.get(0),
                this.mapView.getModel().mapViewPosition,
                mapFile,
                InternalRenderTheme.OSMARENDER, false, true, false);
        Log.w("MAP", (Double.toString(tileRendererLayer.getMapDataStore().boundingBox().maxLatitude)));
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
        MarkerGroup markerGroup = new MarkerGroup(activity);
        markerGroup.createMarkers(this.mapView);
    }

    private void createControls() {
        initializePosition(mapView.getModel().mapViewPosition);
    }

    private MapPosition getInitialPosition() {
        return new MapPosition(mapFile.boundingBox().getCenterPoint(), default_zoom_level );
    }

    private void initializePosition(IMapViewPosition mvp) {
        LatLong center = mvp.getCenter();

        if (center.equals(new LatLong(0, 0))) {
            mvp.setMapPosition(this.getInitialPosition());
        }
    }

    public void setPosition(double latitude, double longitude) {
        LatLong latLong = new LatLong(latitude, longitude);
        byte zoom_level = (byte)((int)max_zoom_level - 6);
        MapPosition mapPosition = new MapPosition(latLong, zoom_level);
        mapView.getModel().mapViewPosition.setMapPosition(mapPosition, true);
    }

    public byte getZoomLevelDefault() {
        return default_zoom_level;
    }

    public void setZoomLevelDefault(int level) {
        default_zoom_level = (byte) level;
        mapView.getModel().mapViewPosition.setZoomLevel(default_zoom_level);
    }

    public byte getZoomLevelMin() {
        return min_zoom_level;
    }

    public void setZoomLevelMin(int level) {
        min_zoom_level = (byte) level;
        mapView.setZoomLevelMin(min_zoom_level);
    }

    public byte getZoomLevelMax() { return max_zoom_level; }

    public void setZoomLevelMax(int level) {
        max_zoom_level = (byte) level;
        mapView.setZoomLevelMax(max_zoom_level);
    }

    public String getBoundingBox() { return bounding_box; }

    public void setBounding_box(String bounding_box) {
        this.bounding_box = bounding_box;
        mapView.getModel().mapViewPosition.setMapLimit(BoundingBox.fromString(bounding_box));
    }

    public void save_preferences() {
        mapView.getModel().save(this.preferencesFacade);
        this.preferencesFacade.save();
    }

    public void cleanup() {
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        tileCaches.clear();
    }

    //Marker
    public void markme(double latitude, double longitude, int radius){
        if(this.myLocationOverlay==null)
        {
            LatLong latLong = new LatLong(latitude, longitude);
            Drawable drawable = activity.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position,null);
            Marker position_marker = new Marker(latLong, AndroidGraphicFactory.convertToBitmap(drawable), 0, 0);

            // circle to show the location accuracy (optional)
            //key in the
            Circle circle = new Circle(latLong, radius,
                    MarkerGroup.getPaint(AndroidGraphicFactory.INSTANCE.createColor(48, 0, 0, 255), 0, Style.FILL),
                    MarkerGroup.getPaint(AndroidGraphicFactory.INSTANCE.createColor(160, 0, 0, 255), 2, Style.STROKE));

            // create the overlay
            this.myLocationOverlay = new MyLocationOverlay(position_marker, circle);
            mapView.getLayerManager().getLayers().add(this.myLocationOverlay);
        }
        else
        {
            this.myLocationOverlay.setPosition(latitude,longitude,radius);
        }

    }
}
