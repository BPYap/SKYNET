package com.skynet.map;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.hills.HillsRenderConfig;
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
    // TODO: make these global into arguments for constructor for modularity
    private static final String BOUNDING_BOX = "1.2837,103.6575,1.4317,104.0007";

    private MapView mapView;
    private MapFile mapFile;
    private PreferencesFacade preferencesFacade;
    protected List<TileCache> tileCaches = new ArrayList<TileCache>();

    public Map(AppCompatActivity activity, View view, File map_file) {
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
        mapView.setBuiltInZoomControls(hasZoomControls());
        mapView.getMapZoomControls().setAutoHide(isZoomControlsAutoHide());
        mapView.getMapZoomControls().setZoomLevelMin(getZoomLevelMin());
        mapView.getMapZoomControls().setZoomLevelMax(getZoomLevelMax());
        mapView.getModel().mapViewPosition.setMapLimit(BoundingBox.fromString(BOUNDING_BOX));
    }

    private void createTileCaches(AppCompatActivity activity) {
        this.tileCaches.add(AndroidUtil.createTileCache(
                activity, activity.getClass().getSimpleName(),
                this.mapView.getModel().displayModel.getTileSize(), 1.0f,
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

        // add markers here
    }

    private void createControls() {
        initializePosition(mapView.getModel().mapViewPosition);
    }

    protected byte getZoomLevelDefault() {
        return (byte) 13;
    }

    protected byte getZoomLevelMin() {
        return (byte) 13;
    }

    protected byte getZoomLevelMax() { return (byte) 20; }

    protected MapPosition getInitialPosition() {
        return new MapPosition(mapFile.boundingBox().getCenterPoint(), (byte)13 );
        // return new MapPosition(new LatLong(LAT, LONG), getZoomLevelDefault());
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

    protected void purgeTileCaches() {
        for (TileCache tileCache : tileCaches) {
            tileCache.purge();
        }
        tileCaches.clear();
    }

    protected void redrawLayers() {
        mapView.getLayerManager().redrawLayers();
    }

    protected HillsRenderConfig getHillsRenderConfig() {
        return null;
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
}
