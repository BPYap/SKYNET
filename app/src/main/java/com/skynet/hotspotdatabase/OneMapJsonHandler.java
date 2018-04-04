package com.skynet.hotspotdatabase;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by eddyl on 24/3/2018.
 */

public class OneMapJsonHandler implements ProcessHotspotJson {

    private int index;
    private int numHotspots;

    private String urlSite = "https://developers.onemap.sg/privateapi/";
    private String urlType = "themesvc/retrieveTheme?queryName=wireless_hotspots&token=";
    private String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjEzOTEsInVzZXJfaWQiOjEzOTEsImVtYWlsIjoiZWRkeWxpbTk1QGhvdG1haWwuY29tIiwiZm9yZXZlciI6ZmFsc2UsImlzcyI6Imh0dHA6XC9cL29tMi5kZmUub25lbWFwLnNnXC9hcGlcL3YyXC91c2VyXC9zZXNzaW9uIiwiaWF0IjoxNTIyNjA3NjIwLCJleHAiOjE1MjMwMzk2MjAsIm5iZiI6MTUyMjYwNzYyMCwianRpIjoiM2U1MjczMDQ0NWMxYTEzMDVjMDVmOTI0NTQ5M2UyYTIifQ.1_SKgAh6wPyHlU8jxooYCg5vhwH9vjzgcgsF5QyW6aM";
    private JSONObject json;


    protected OneMapJsonHandler() {
    }

    @Override
    public Hotspot[] getHotspots(Context context) {
        try {
            retrieveJson(context);
            int pollCount = 0;
            do{
                Thread.sleep(5000);
                Log.d("Json","Still requesting json... for "+Integer.toString(++pollCount)+" time.");
            }while(json == null && pollCount<5);

            JSONArray jsonArray = json.getJSONArray("SrchResults");
            numHotspots = Integer.parseInt(jsonArray.getJSONObject(0).getString("FeatCount"));
            int[] addressPostalCode = new int[numHotspots];
            double[] longtitude = new double[numHotspots];
            double[] lattitude = new double[numHotspots];
            String[] name = new String[numHotspots];
            String[] description = new String[numHotspots];
            String[] addressStreetName = new String[numHotspots];
            String[] operatorName = new String[numHotspots];
            StringBuilder stringBuilder = new StringBuilder();
            String temp[] = new String[2];
            Hotspot[] hotspot = new Hotspot[numHotspots];
            for (int i = 0; i < numHotspots; i++) {
                try{
                    index = i;
                    name[i] = jsonArray.getJSONObject(i + 1).getString("NAME");
                    description[i] = jsonArray.getJSONObject(i + 1).getString("DESCRIPTION");
                    addressStreetName[i] = jsonArray.getJSONObject(i + 1).getString("ADDRESSSTREETNAME");
                    operatorName[i] = jsonArray.getJSONObject(i + 1).getString("OPERATOR_NAME");
                    addressPostalCode[i] = Integer.parseInt(jsonArray.getJSONObject(i + 1).getString("ADDRESSPOSTALCODE"));
                    temp = jsonArray.getJSONObject(i + 1).getString("LatLng").split(",");
                    lattitude[i] = Double.parseDouble(temp[0]);
                    longtitude[i] = Double.parseDouble(temp[1]);
                    hotspot[i] = new Hotspot(index, lattitude[i], longtitude[i], addressPostalCode[i],
                            description[i], name[i], addressStreetName[i], operatorName[i]);
                }
                catch (Exception e){
                    Log.e("Json", "storeInSQL erorr ", e);
                    return null;
                }
            }
            Log.d("Json", "storeInSQL done!");
            return hotspot;
        } catch (Exception e) {
            Log.e("Json", "Json handler or drop error", e);
            return null;
        }
    }

    public void retrieveJson(Context context) {
        try {
            String url = this.getConcatUrl();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        json = response;
                    } catch (Exception e) {
                        Log.e("Json", "Failed assign Json", e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Json", "Volley error", error);
                }
            });
            Log.d("Json", "retrieveJson: done");
            RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
        } catch (Exception e) {
            Log.e("Json", "JsonObjectRequest failed", e);
        }
    }

    private String getConcatUrl() {
        return (urlSite + urlType + apiKey);
    }
}






