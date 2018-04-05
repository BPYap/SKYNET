package com.skynet.map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.skynet.hotspotdatabase.AppDatabase;
import com.skynet.hotspotdatabase.AsycQuery;
import com.skynet.hotspotdatabase.Hotspot;
import com.skynet.main.R;


import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

public class Map {
    private String bounding_box;
    private byte default_zoom_level = 13;
    private byte min_zoom_level;
    private byte max_zoom_level;

    private MapView mapView;
    private MapFile mapFile;
    private PreferencesFacade preferencesFacade;
    private List<TileCache> tileCaches = new ArrayList<>();

    private Context context;

    public Map(AppCompatActivity activity, View view, File map_file,Context context) {
        mapFile = new MapFile(map_file);
        this.context = context;
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
        mapView.setBuiltInZoomControls(true);
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

        // to have all the hotspot here and put the marker on
        Hotspot[] query = new Hotspot[0];
        try {
            query = new AsycQuery(AppDatabase.getInstance(context)).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for (Hotspot hotspot : query) {
            createPositionMarker(hotspot.getLatitude(), hotspot.getLongitude(), hotspot.getName());
            Log.i("Put marker on", hotspot.getName()); //debug
        }
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

    public void setZoomLevelMin(int level) {
        min_zoom_level = (byte) level;
        mapView.getMapZoomControls().setZoomLevelMin(min_zoom_level);
    }

    public void setZoomLevelMax(int level) {
        max_zoom_level = (byte) level;
        mapView.getMapZoomControls().setZoomLevelMax(max_zoom_level);
    }

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

    //create marker
    private void createPositionMarker(double paramDouble1, double paramDouble2, String name) {
        final LatLong localLatLong = new LatLong(paramDouble1, paramDouble2);
        TappableMarker positionMarker = new TappableMarker(R.drawable.marker_green, localLatLong, name);
        mapView.getLayerManager().getLayers().add(positionMarker);
    }

    public class TappableMarker extends Marker {
        int i=0;
        private String text;
        public String getName() {
            return this.text;
        }

        public TappableMarker(int icon, LatLong localLatLong, String name) {
            super(localLatLong, AndroidGraphicFactory.convertToBitmap(context.getResources().getDrawable(icon)),
                    1 * (AndroidGraphicFactory.convertToBitmap(context.getResources().getDrawable(icon)).getWidth()) / 2,
                    -1 * (AndroidGraphicFactory.convertToBitmap(context.getApplicationContext().getResources().getDrawable(icon)).getHeight()) / 2);
            this.text = name;
        }

        //on tap and return overlay bubble
        public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
            Bitmap bitmapRed;
            Bitmap bitmapGrey;
            Drawable drawableWhite = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getResources().getDrawable(R.drawable.marker_white) : context.getResources().getDrawable(R.drawable.marker_white);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
            bitmapRed = AndroidGraphicFactory.convertToBitmap(drawableWhite, paint);
            paint.setColorFilter(new PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY));
            bitmapGrey = AndroidGraphicFactory.convertToBitmap(drawableWhite, paint);

            if (this.contains(layerXY, tapXY)) {
                Log.d("LALA", "succeed");
                if (mapView.getLayerManager().getLayers().contains(this)) {
                    {
//                    if (this.getBitmap() != null){
//                        SimplestMapViewer.this.mapView.getLayerManager().getLayers().remove(this);
//                        SimplestMapViewer.this.mapView.getLayerManager().redrawLayers();}
//                    else
                        this.setBitmap(bitmapRed);
                        if (i>0)
                        {Log.d("LALA", "here");
                            this.setBitmap(bitmapGrey);}
                        i++;
                        Toast.makeText(context, this.getName(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
            return false;
        }
    }

}
