package com.skynet.hotspotdatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.skynet.onemap.OneMapJsonHandler;

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
            //create new instance of processHotspotJson using OneMapJsonHandler
            ProcessHotspotJson processHotspotJson = new OneMapJsonHandler();
            hotspots = processHotspotJson.getHotspots(context);
            if (hotspots == null){
                Log.d("refreshDatabase", "hotspot value is null");
                return null;
            }
            storeInSQL(hotspots);
            String test = appDatabase.hotspotDao().findByIndex(500).getName(); //Query debug test
            Log.d("refreshDatabase", "Debug test: "+test);
        }
        catch (Exception e){
            Log.e("refreshDatabase", "Async doInBackground Error", e);
        }
        return null;
    }

    private void storeInSQL(Hotspot[] hotspots){
        try {
            appDatabase.hotspotDao().dropTable();                   //delete table
            appDatabase.hotspotDao().insertAll(hotspots);           //create and update table
        }
        catch (Exception e){
            Log.e("refreshDatabase", "storeInSQL Dao Error", e);
        }
    }
}
