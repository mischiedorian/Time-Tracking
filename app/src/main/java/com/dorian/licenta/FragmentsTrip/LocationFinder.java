package com.dorian.licenta.FragmentsTrip;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dorian on 22/03/2017.
 */

public class LocationFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyCbcVO-GVIhVoIQOWyn890GeFjq6I5fL2g";
    private String location;
    public static LatLng latLng;

    public LocationFinder(String location) {
        this.location = location;
    }

    public void execute() throws UnsupportedEncodingException {
        new DownloadJsonData(){
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
        String urlDestination = URLEncoder.encode(location, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        JSONObject jsonRoute = jsonRoutes.getJSONObject(0);
        JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

        Log.i("dorian",jsonStartLocation.getDouble("lat") +"-"+jsonStartLocation.getDouble("lng"));
        latLng = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
    }
}



