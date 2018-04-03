package com.skynet.hotspotdatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by eddyl on 25/3/2018.
 */

public class AsyncStoreSQL extends AsyncTask<Void, Void, Void> {

    private AppDatabase appDatabase;
    private Context context;
    private Hotspot[] hotspots;

    protected AsyncStoreSQL(AppDatabase appDatabase, Context context){
        this.appDatabase = appDatabase;
        this.context = context;
    }

    protected Void doInBackground(Void... voids) {
        try {
            ProcessHotspotJson processHotspotJson = new OneMapJsonHandler();
            hotspots = processHotspotJson.getHotspots(context);
            if (hotspots == null){
                Log.d("AsyncStoreSQL", "hotspot value is null");
                return null;
            }
            storeInSQL(hotspots);
            Log.d("AsyncStoreSQL", "Store in SQL done");
            String test = appDatabase.hotspotDao().findByPostcode(470719).getNAME(); //Query debug test
            Log.d("AsyncStoreSQL", test);
        }
        catch (Exception e){
            Log.e("AsyncStoreSQL", "Async doInBackground Error", e);
        }
        return null;
    }

    private void storeInSQL(Hotspot[] hotspots){
        try {
            appDatabase.hotspotDao().dropTable();                   //delete table
            appDatabase.hotspotDao().insertAll(hotspots);           //create and update table
        }
        catch (Exception e){
            Log.e("Database", "storeInSQL Error", e);
        }
    }
}
