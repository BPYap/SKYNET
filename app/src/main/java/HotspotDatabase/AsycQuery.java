package HotspotDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eddyl on 25/3/2018.
 */

public class AsycQuery extends AsyncTask<Void, Void, Hotspot[]> {

    private HotspotDatabase.AppDatabase appDatabase;
    private Hotspot[] hotspots;
    public AsycQuery(HotspotDatabase.AppDatabase appDatabase){
        this.appDatabase = appDatabase;
    }

    protected Hotspot[] doInBackground(Void... voids) {
        try {
            this.hotspots = appDatabase.hotspotDao().getAll();
            Log.d("AsyncQuery", hotspots.toString());//Query test
        }
        catch (Exception e){
            Log.e("AsyncQuery", "Async doInBackground Error", e);
        }
        return hotspots;
    }

}
