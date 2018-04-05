package com.skynet.hotspotdatabase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class DatabaseManager {

    private static DatabaseManager mInstance = new DatabaseManager();           //Singleton

    private DatabaseManager(){
    }

    public static synchronized DatabaseManager getDatabaseControl() {
        if (mInstance == null) {
            mInstance = new DatabaseManager();
        }
        return mInstance;
    }

    public void refreshDatabase(Context context){
        if (new InternetConnection().getInternet()) {         //check if internet is connected
            try {
                Log.d("Internet, refreshDatabase", "Internet connected");
                new AsyncStoreSQL(AppDatabase.getInstance(context), context).execute();     //create new thread to store in SQL
            } catch (Exception e) {
                Log.e("Internet, refreshDatabase", "Error, skipping data update", e);
            }
        } else {
            Toast.makeText(context, "Connect to internet to refresh data.", Toast.LENGTH_LONG).show();
            Log.d("Internet, refreshDatabase", "No Internet Connection, skipping Json retrieval");
        }
    }

    public Hotspot[] getAllHotspots(Activity activity){
        try {
            Hotspot[] query = new AsycQuery(AppDatabase.getInstance(activity)).execute().get();
            return query;
        } catch (InterruptedException e) {
            Log.w("Map", "Interrupted Exception");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.w("Map", "Execution Exception");
            e.printStackTrace();
        }
        return null;
    }
}

