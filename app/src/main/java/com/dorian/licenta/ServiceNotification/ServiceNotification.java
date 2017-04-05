package com.dorian.licenta.ServiceNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dorian.licenta.Activities.ResponseNotificationActivity;
import com.dorian.licenta.GetMaxFreqLocation;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by misch on 05.04.2017.
 */

public class ServiceNotification extends Service {

    private ArrayList<MyLocation> locatiiDate;

    public ServiceNotification(Context applicationContext) {
        super();
        Log.wtf("HERE", "constructor 1");
    }

    public ServiceNotification() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent incomingSms = new Intent(getApplicationContext(), ServiceNotification.class);
        sendBroadcast(incomingSms);
        start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.wtf("Service: ", "Destroyed");
        super.onDestroy();
        sendBroadcast(new Intent("ReloadIt1"));
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void start() {
        timer = new Timer();
        catchLocations();
        timer.schedule(timerTask, 1000, 1000);
    }

    public void catchLocations() {
        timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                int seconds = calendar.get(Calendar.SECOND);
                if (hour + 3 == 7 && minutes == 0 && seconds == 0) {
                    GetMaxFreqLocation task = new GetMaxFreqLocation() {
                        @Override
                        protected void onPostExecute(MyLocation myLocation) {
                            showNotification(new LatLng(myLocation.getLat(), myLocation.getLgn()));
                        }
                    };
                    task.execute();
                }

                if (minutes == 59 && seconds == 57) {
                    RestServices.Factory.getIstance().getLocationsAferMonthAndDay(java.util.Calendar.getInstance().getTime().getMonth() + 1 + "", java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) + "").enqueue(new Callback<List<MyLocation>>() {
                        @Override
                        public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                            locatiiDate = new ArrayList<>();
                            for (MyLocation location : response.body()) {
                                locatiiDate.add(location);
                            }
                            checkLocations(locatiiDate);
                        }

                        @Override
                        public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        }
                    });
                }

                if(hour +3 == 23 && minutes == 59 && seconds == 59){
                    RestServices.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
                        @Override
                        public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                            locatiiDate = new ArrayList<>();
                            for (MyLocation location : response.body()) {
                                locatiiDate.add(location);

                            }
                            checkLocations1(locatiiDate);
                        }

                        @Override
                        public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        }
                    });
                }
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(LatLng latLng) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), ResponseNotificationActivity.class), 0);

        Intent intent = new Intent(getApplicationContext(), ResponseNotificationActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_menu_send)
                .setTicker("Hearty365")
                .setContentTitle("Cel mai mult stai la locatia:")
                .setContentText(latLng.latitude + ":" + latLng.longitude)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    private void checkLocations(ArrayList<MyLocation> locatiiDate) {
        MyLocation referinta = locatiiDate.get(0);
        MyLocation local = referinta;
        int nrLocatii = 0;
        int i = 1;
        try {
            do {
                if (new MyLocationHelper(referinta).distanceBetween2Locations(locatiiDate.get(i)) < 0.030) {
                    new MyLocationHelper(locatiiDate.get(i)).deleteLocation();
                    nrLocatii++;
                    local = locatiiDate.get(i);
                } else {
                    if (nrLocatii == 0 && referinta.getOraSfarsit() == null) {
                        new MyLocationHelper(referinta).deleteLocation();
                    } else {
                        new MyLocationHelper(referinta).updateLocation(local.getOraInceput());
                    }
                    nrLocatii = 0;
                    referinta = locatiiDate.get(i);
                    local = referinta;
                }
                i++;
            } while (i < locatiiDate.size());
            if (referinta != local) {
                new MyLocationHelper(referinta).updateLocation(local.getOraInceput());
            }
        } catch (Exception e) {
            Log.e("checkLocations", e.getMessage());
        }
    }

    private void checkLocations1(ArrayList<MyLocation> locatiiDate) {
        for (int i = 0; i < locatiiDate.size(); i++) {
            MyLocationHelper referinta = new MyLocationHelper(locatiiDate.get(i));
            if (referinta.minutesLocation() < 11) {
                referinta.deleteLocation();
            }
            Log.i("ceva", referinta.minutesLocation() + "");
        }
    }
}
