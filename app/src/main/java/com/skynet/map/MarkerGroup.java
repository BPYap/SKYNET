package com.skynet.map;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
import com.skynet.hotspotdatabase.DatabaseManager;
import com.skynet.hotspotdatabase.Hotspot;
import com.skynet.main.R;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

public class MarkerGroup {
    private static Drawable blank_marker;
    private static android.graphics.Paint red_paint = new android.graphics.Paint();
    private static android.graphics.Paint grey_paint = new android.graphics.Paint();
    private Activity activity;
    private MapView mapView;
    private TappableMarker last_tapped;

    public MarkerGroup(Activity activity)
    {
        this.activity = activity;
        last_tapped = null;
    }

    protected static org.mapsforge.core.graphics.Paint getPaint(int color, int strokeWidth, Style style) {
        org.mapsforge.core.graphics.Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    private void initialize_bitmap() {
        blank_marker = activity.getResources().getDrawable(R.drawable.marker_white, null);
        red_paint = new android.graphics.Paint();
        red_paint.setAntiAlias(true);
        red_paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
        grey_paint = new android.graphics.Paint();
        grey_paint.setAntiAlias(true);
        grey_paint.setColorFilter(new PorterDuffColorFilter(android.graphics.Color.argb(255, 103, 122, 94), PorterDuff.Mode.MULTIPLY));
    }

    protected void createMarkers(final MapView mapView) {
        initialize_bitmap();
        try {
            Hotspot[] query = DatabaseManager.getDatabaseControl().getAllHotspots(activity);
            int pollCount = 0;
            do {
                Thread.sleep(5000);
                Log.d("Map", "Waiting for Hotspot retrieval... Attempt #"+ ++pollCount);
            } while (query == null && pollCount < 5);
            for (Hotspot hotspot : query) {
                final LatLong localLatLong = new LatLong(hotspot.getLatitude(), hotspot.getLongitude());
                TappableMarker positionMarker = new TappableMarker(R.drawable.marker_green, localLatLong, hotspot.getName()) {
                   @Override
                   public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY){
                       if (this.contains(layerXY, tapXY)) {
                           Log.d("Touch event", "triggered");
                           if (mapView.getLayerManager().getLayers().contains(this)) {
                               {
                                   Toast.makeText(activity, this.getName(), Toast.LENGTH_SHORT).show();
                                   if (getLast_tapped() != null){
                                       getLast_tapped().setBitmap(AndroidGraphicFactory.convertToBitmap(blank_marker, grey_paint));
                                   }
                                   this.setBitmap(AndroidGraphicFactory.convertToBitmap(blank_marker, red_paint));
                                   setLast_tapped(this);
                                   mapView.getModel().mapViewPosition.animateTo(getPosition());
                               }
                               return true;
                           }
                       }
                       return false;
                   }
                };
                mapView.getLayerManager().getLayers().add(positionMarker);
                Log.i("Put marker on", hotspot.getName()); //debug
            }
        }catch (Exception e) {
            Log.d("Map", "Unable to retrieve hotspot data");
        }
    }

    public TappableMarker getLast_tapped(){return last_tapped;}

    public void setLast_tapped(TappableMarker marker){this.last_tapped = marker;}

    public class TappableMarker extends Marker {

        private String text;
        public String getName() {
            return this.text;
        }

        public TappableMarker(int icon, LatLong localLatLong, String name) {
            super(localLatLong, AndroidGraphicFactory.convertToBitmap(activity.getResources().getDrawable(icon, null)),
                    (AndroidGraphicFactory.convertToBitmap(activity.getResources().getDrawable(icon, null)).getWidth()) / 2,
                    -1 * (AndroidGraphicFactory.convertToBitmap(activity.getApplicationContext().getResources().
                            getDrawable(icon,null)).getHeight()) / 2);
            this.text = name;
        }
    }
}
