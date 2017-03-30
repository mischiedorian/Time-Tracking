package com.dorian.licenta.Service;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.Main2Activity;
import com.dorian.licenta.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.PendingIntent.*;

/**
 * Created by doriancristian on 17.02.2017.
 */

public class RSSPullService extends Service implements LocationListener {

    LocationManager locationManager;
    String provider;
    GoogleApiClient googleApiClient;
    private NotificationManager mNM;

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        Log.i("service", "serviciu s-a creat");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            Log.i("locatie ", "gasita");
        } else {
            Log.i("locatie ", "negasita");
        }

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "ceva";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = getActivity(this, 0,
                new Intent(this, Main2Activity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("serviciu")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(1337, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bucla();
        return START_NOT_STICKY;
    }

    private void bucla() {
        synchronized (this) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    if (ActivityCompat.checkSelfPermission(RSSPullService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RSSPullService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    // Location location = locationManager.getLastKnownLocation(provider);
                    // onLocationChanged(location);
                    Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                    try {
                        Date date = Calendar.getInstance().getTime();
                        new MyLocationHelper(new MyLocation(date.getDay(), date.getMonth() + 1, date.getHours() + ":" + date.getMinutes(),
                                "", location.getLatitude(), location.getLongitude()))
                                .insertLocation();
                        Log.i("locatie ", "insereaza");
                    } catch (Exception e) {
                        Log.e("locatie", e.getMessage());
                    }
                    locationManager.removeUpdates(RSSPullService.this);
                }
            }, 10000);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Service distrus", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Latitude: ", lat.toString());
        Log.i("Longitude: ", lng.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class MyBinder extends Binder {
        public RSSPullService getService() {
            return RSSPullService.this;
        }
    }
}