package com.dorian.licenta.ServiceNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dorian.licenta.Activities.ResponseNotificationActivity;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.NearbyPlace.DataParser;
import com.dorian.licenta.NearbyPlace.GetNearbyPlacesData;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.dorian.licenta.Product.Product;
import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceNotification extends Service {

    private ArrayList<MyLocation> locatiiDate;

    private SharedPreferences sharedPreferences;
    private int idUser;

    private String produs = null;
    private int max = 0;

    public ServiceNotification(Context applicationContext) {
        super();
        Log.wtf("HERE", "constructor 1");
    }

    public ServiceNotification() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.wtf("Service:", " onStartCommand notification");
        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
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
                idUser = sharedPreferences.getInt("idUser", 0);
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                int seconds = calendar.get(Calendar.SECOND);

                //Log.i("ora", hour + ":" + minutes + ":" + seconds);
                if (hour == 10 && minutes == 07 && seconds == 0) {
                    Log.wtf("showNotification", "before");
                    GetMaxFreqLocation task = new GetMaxFreqLocation() {
                        @Override
                        protected void onPostExecute(MyLocation myLocation) {
                            showNotification(new LatLng(myLocation.getLat(), myLocation.getLgn()));
                            Log.wtf("apel notificare", "apel notificare");
                        }
                    };
                    task.execute(idUser);
                }

                if (minutes == 59 && seconds == 40) {
                    Log.wtf("CURATENIE", "SE FACE CURAT!!!");
                    RestServices.Factory.getIstance().getLocationsAferMonthAndDay(Calendar.getInstance().getTime().getMonth() + 1,
                            Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH), idUser).enqueue(new Callback<List<MyLocation>>() {
                        @Override
                        public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                            locatiiDate = new ArrayList<>();
                            locatiiDate.addAll(response.body());
                            concatenationLocations(locatiiDate);
                        }

                        @Override
                        public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        }
                    });
                }

                //hour + 3 == 23 &&
                if (minutes == 59 && seconds == 59) {
                    Log.wtf("CURATENIE", "SE FACE CURAT MAI AMANUNTIT!!!");
                    RestServices.Factory.getIstance().getLocationsAferDay(Calendar.DAY_OF_MONTH, idUser).enqueue(new Callback<List<MyLocation>>() {
                        @Override
                        public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                            locatiiDate = new ArrayList<>();
                            locatiiDate.addAll(response.body());
                            removedLocationsInsignificant(locatiiDate);
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
        Log.wtf("showNotification", "intra in functie");
        String url = getUrl(latLng.latitude, latLng.longitude, "restaurant");
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = null;
        DataTransfer[1] = url;
        final String[] placeName = new String[1];

        RestServices.Factory.getIstance().getProductsAfterUser(idUser).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                response.body().stream().filter(product -> max < product.getQuantity()).forEach(product -> {
                    produs = product.getName();
                    max = product.getQuantity();
                });

                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData() {
                    @Override
                    protected void onPostExecute(String result) {
                        List<HashMap<String, String>> nearbyPlacesList;
                        DataParser dataParser = new DataParser();
                        nearbyPlacesList = dataParser.parse(result);
                        HashMap<String, String> googlePlace = nearbyPlacesList.get(0);
                        placeName[0] = googlePlace.get("place_name");
                        Log.wtf("place name", placeName[0]);

                        Intent intent = new Intent(getApplicationContext(), ResponseNotificationActivity.class);
                        intent.putExtra("loc", placeName[0]);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
                        if (googlePlace.size() != 0) {
                            b.setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_menu_send)
                                    .setTicker("notificare")
                                    .setContentTitle("Doresti sa mananci " + produs + "?")
                                    .setContentText("Te invitam la " + placeName[0])
                                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                                    .setContentIntent(contentIntent)
                                    .setContentInfo("Info");
                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(1, b.build());
                        }
                    }
                };
                getNearbyPlacesData.execute(DataTransfer);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {

            }
        });
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + 3000);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private void concatenationLocations(ArrayList<MyLocation> locatiiDate) {
        MyLocation referinta = locatiiDate.get(0);
        MyLocation local = referinta;
        int nrLocatii = 0;
        int i = 1;
        try {
            do {
                if (new MyLocationHelper(referinta)
                        .distanceBetween2Locations(locatiiDate.get(i)) < 0.030) {
                    new MyLocationHelper(locatiiDate.get(i)).deleteLocation();
                    nrLocatii++;
                    local = locatiiDate.get(i);
                } else {
                    if (nrLocatii == 0 && referinta.getOraSfarsit() == null) {
                        new MyLocationHelper(referinta).deleteLocation();
                    } else {
                        new MyLocationHelper(referinta)
                                .updateLocation(local.getOraInceput());
                    }
                    nrLocatii = 0;
                    referinta = locatiiDate.get(i);
                    local = referinta;
                }
                i++;
            } while (i < locatiiDate.size());
            if (referinta != local) {
                new MyLocationHelper(referinta)
                        .updateLocation(local.getOraInceput());
            }
        } catch (Exception e) {
            Log.e("concatenationLocations", e.getMessage());
        }
    }

    private void removedLocationsInsignificant(ArrayList<MyLocation> locatiiDate) {
        for (int i = 0; i < locatiiDate.size(); i++) {
            MyLocationHelper referinta =
                    new MyLocationHelper(locatiiDate.get(i));
            if (referinta.minutesLocation() < 11) {
                referinta.deleteLocation();
            }
            Log.i("minute state in locatie", referinta.minutesLocation() + "");
        }
    }
}
