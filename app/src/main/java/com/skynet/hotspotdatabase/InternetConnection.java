package com.skynet.hotspotdatabase;

import android.util.Log;

import java.net.InetAddress;

//Internet connection

public class InternetConnection {
    private boolean internet = false;                           //internet status

    public boolean getInternet(){
        isInternetAvailable();
        return internet;
    }


    private void isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            if (!ipAddr.equals("") == true){
                Log.d("Internet","Internet connected");
                internet = true;
            }
            else {
                Log.d("Internet", "Internet not connected");
                internet = false;
            }

        } catch (Exception e) {
            Log.e("Internet", "Internet Error", e);
            internet = false;
        }
    }
}
