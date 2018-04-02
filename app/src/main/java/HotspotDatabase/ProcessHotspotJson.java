package HotspotDatabase;

import android.content.Context;

import HotspotDatabase.Hotspot;

/**
 * Created by eddyl on 24/3/2018.
 */

public interface ProcessHotspotJson {

    public HotspotDatabase.Hotspot[] getHotspots(Context context);

    public void retrieveJson(Context context);

}
