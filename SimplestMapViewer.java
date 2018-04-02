/*
 * Copyright 2014 Ludwig M Brinckmann
 * Copyright 2015-2018 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.samples.android;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.util.MapViewerTemplate;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import java.util.concurrent.ExecutionException;



/**
 * The simplest form of creating a map viewer based on the MapViewerTemplate.
 * It also demonstrates the use simplified cleanup operation at activity exit.
 */
public class SimplestMapViewer extends MapViewerTemplate {
    /**
     * This MapViewer uses the built-in Osmarender theme.
     *
     * @return the render theme to use
     */
    @Override
    protected XmlRenderTheme getRenderTheme() {
        return InternalRenderTheme.OSMARENDER;
    }

    /**
     * This MapViewer uses the standard xml layout in the Samples app.
     */
    @Override
    protected int getLayoutId() {
        return R.layout.mapviewer;
    }

    /**
     * The id of the mapview inside the layout.
     *
     * @return the id of the MapView inside the layout.
     */
    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    /**
     * The name of the map file.
     *
     * @return map file name
     */
    @Override
    protected String getMapFileName() {
        return "germany.map";
    }

    /**
     * Creates a simple tile renderer layer with the AndroidUtil helper.
     */
    @Override
    protected void createLayers() throws ExecutionException, InterruptedException {
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches.get(0),
                this.mapView.getModel().mapViewPosition, getMapFile(), getRenderTheme(), false, true, false);
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

        //createPositionMarker(1.34907194, 103.6895278);
        //createPositionMarker(1.348, 105.2441);
        Hotspot[] query = new AsycQuery(AppDatabase.getInstance(getApplicationContext())).execute().get();
        for (Hotspot hotspot : query) {
            createPositionMarker(hotspot.getLat(), hotspot.getLong());
            Log.i("Put marker on", hotspot.getNAME()); //debug
        }

    }

    @Override
    protected void createMapViews() {
        super.createMapViews();
    }

    /**
     * Creates the tile cache with the AndroidUtil helper
     */
    @Override
    protected void createTileCaches() {
        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor()));
    }

    @Override
    protected MapPosition getInitialPosition() {
        int tileSize = this.mapView.getModel().displayModel.getTileSize();
        byte zoomLevel = LatLongUtils.zoomForBounds(new Dimension(tileSize * 4, tileSize * 4), getMapFile().boundingBox(), tileSize);
        return new MapPosition(getMapFile().boundingBox().getCenterPoint(), zoomLevel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getClass().getSimpleName());
        //To enable internet in Emulator
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (InternetConnection.getInternetStatus().getInternet() == true) {
            try {
                Log.d("MainActivity", "Internet connected");
                new AsyncStoreSQL(AppDatabase.getInstance(getApplicationContext()), getApplicationContext()).execute();
            } catch (Exception e) {
                Log.e("MainActivity", "Error, skipping data update", e);
            }
        } else {
            Log.d("MainActivity", "No Internet Connection, skipping Json retrieval");
        }
    }

    //create marker
    private void createPositionMarker(double paramDouble1, double paramDouble2) {
        final LatLong localLatLong = new LatLong(paramDouble1, paramDouble2);
        TappableMarker positionMarker = new TappableMarker(R.drawable.marker_green, localLatLong);
        mapView.getLayerManager().getLayers().add(positionMarker);
    }

    public class TappableMarker extends Marker {
        public TappableMarker(int icon, LatLong localLatLong) {
            super(localLatLong, AndroidGraphicFactory.convertToBitmap(SimplestMapViewer.this.getApplicationContext().getResources().getDrawable(icon)),
                    1 * (AndroidGraphicFactory.convertToBitmap(SimplestMapViewer.this.getApplicationContext().getResources().getDrawable(icon)).getWidth()) / 2,
                    -1 * (AndroidGraphicFactory.convertToBitmap(SimplestMapViewer.this.getApplicationContext().getResources().getDrawable(icon)).getHeight()) / 2);
        }

        //on tap and return overlay bubble
//            String here = Double.toString(tapLatLong.getLatitude()) + " " + Double.toString(tapLatLong.getLongitude()) + "\n" + Double.toString(layerXY.x)+ " " + Double.toString(layerXY.y)+ "\n" + Double.toString(tapXY.x)+ " " + Double.toString((tapXY.y)); //for debug purpose
//            Log.i("Points:", here); //for debug purpose

        public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) throws ExecutionException, InterruptedException {
            Bitmap bubble;

//            Hotspot[] query = new AsycQuery(AppDatabase.getInstance(getApplicationContext())).execute().get();
//            LatLong[] HotspotLatLong = new LatLong[query.length];
//            for (int a=0; a<=query.length; a++) {
//                LatLong Hotspot = new LatLong(query[a].getLat(), query[a].getLong());
//               HotspotLatLong[a] = Hotspot;
//            }
//            Thread.sleep(100000);
      //      Log.d("NO", "FAILED");
    //        Hotspot Hotspot = AppDatabase.getInstance(getApplicationContext()).hotspotDao().findHotspot(tapLatLong.getLongitude(),tapLatLong.getLatitude());
  //          Log.d("TAP", " ");
//            Thread.sleep(100000);
            if (this.contains(layerXY,tapXY)){
//                for (LatLong Hotspot : HotspotLatLong){
//                    if (tapLatLong.equals(HotspotLatLong))
//                        break;
//                    else
//                        i++;
//                }
                TextView bubbleView = new TextView(getApplicationContext());
                Utils.setBackground(bubbleView, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getDrawable(R.drawable.balloon_overlay_unfocused) : getResources().getDrawable(R.drawable.balloon_overlay_unfocused));
                bubbleView.setGravity(Gravity.CENTER);
                bubbleView.setMaxEms(20);
                bubbleView.setTextSize(15);
                bubbleView.setTextColor(0xff00bdbd);
                bubbleView.setText("Hotspot.getNAME()");
                bubble = Utils.viewToBitmap(getApplicationContext(), bubbleView);
                bubble.incrementRefCount();
                Marker bubbleMarker = new Marker(tapLatLong, bubble, 0, -bubble.getHeight() / 2);
                if (!mapView.getLayerManager().getLayers().contains(bubbleMarker)) {
                    this.setBitmap(bubble);
                }
                return true;
            }
            return false;
        }
    }
}


