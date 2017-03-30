package com.dorian.licenta.FragmentsTrip;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;
import com.dorian.licenta.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;


/**
 * Created by Dorian on 22/03/2017.
 */

public class LocationFinder {
    private static final String directionUrlApi = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyCbcVO-GVIhVoIQOWyn890GeFjq6I5fL2g";
    private String location;
    private LatLng latLng;
    private Context context;

    public LocationFinder(String location, Context context) {
        this.location = location;
        this.context = context;
    }

    public void execute() throws UnsupportedEncodingException {
        new DownloadJsonData() {
            @Override
            protected void onPostExecute(String s) {
                try {
                    parseJSon(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(location, "utf-8");

        return directionUrlApi + "origin=" + urlOrigin + "&destination=" + urlOrigin;
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        JSONObject jsonRoute = jsonRoutes.getJSONObject(0);
        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

        latLng = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
        Log.wtf("dorian", latLng.toString());

        Activity activity = (Activity) context;
        android.app.FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentMap hello = new FragmentMap(latLng);
        fragmentTransaction.add(R.id.contentFragment, hello);
        fragmentTransaction.commit();
    }
}



