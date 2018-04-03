package com.skynet.hotspotdatabase;

import android.content.Context;

/**
 * Created by eddyl on 24/3/2018.
 */

public interface ProcessHotspotJson {

    public Hotspot[] getHotspots(Context context);

    public void retrieveJson(Context context);

}
