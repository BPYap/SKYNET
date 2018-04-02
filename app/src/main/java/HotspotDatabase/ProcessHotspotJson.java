package hotspotdatabase;

import android.content.Context;

/**
 * Created by eddyl on 24/3/2018.
 */

public interface ProcessHotspotJson {

    public hotspotdatabase.Hotspot[] getHotspots(Context context);

    public void retrieveJson(Context context);

}
