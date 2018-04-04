package com.skynet.hotspotdatabase;

import android.content.Context;
import android.util.Log;

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
            Log.d("Internet, refreshDatabase", "No Internet Connection, skipping Json retrieval");
        }
    }
}
