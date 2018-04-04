package com.skynet.hotspotdatabase;

import android.content.Context;
import android.util.Log;

public class DatabaseControl {

    private static DatabaseControl mInstance = new DatabaseControl();           //Singleton

    private DatabaseControl(){
    }

    public static synchronized DatabaseControl getDatabaseControl() {
        if (mInstance == null) {
            mInstance = new DatabaseControl();
        }
        return mInstance;
    }

    public void refreshDatabase(Context context){
        if (new InternetConnection().getInternet() == true) {         //check if internet is connected
            try {
                Log.d("MainActivity", "Internet connected");
                new AsyncStoreSQL(AppDatabase.getInstance(context), context).execute();     //store in SQL
            } catch (Exception e) {
                Log.e("MainActivity", "Error, skipping data update", e);
            }
        } else {
            Log.d("MainActivity", "No Internet Connection, skipping Json retrieval");
        }
    }
}

