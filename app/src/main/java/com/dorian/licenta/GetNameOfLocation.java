package com.dorian.licenta;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Dorian on 7/19/2017.
 */

public class GetNameOfLocation extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        String address = null;
        try {
            url = new URL(params[0]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            InputStream in = connection.getInputStream();
            JSONObject responseData;
            Scanner s = new Scanner(in).useDelimiter("\\A");

            String text = s.next();
            responseData = new JSONObject(text);

            JSONArray results = responseData.getJSONArray("results");
            JSONObject data = results.getJSONObject(1);
            address = data.getString("formatted_address");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }
}
