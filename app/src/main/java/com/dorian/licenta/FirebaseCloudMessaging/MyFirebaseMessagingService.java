package com.dorian.licenta.FirebaseCloudMessaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dorian.licenta.Activities.ResponseNotificationActivity;
import com.dorian.licenta.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String adresaRest;
    private String nameRest;
    private double ratingRest;
    private double lat;
    private double lgn;
    private double probability;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(RemoteMessage message) {

        String image = message.getNotification().getIcon();
        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();
        String sound = message.getNotification().getSound();

        adresaRest = message.getData().get("adresaRest");
        nameRest = message.getData().get("nameRest");
        ratingRest = Double.valueOf(message.getData().get("ratingRest"));
        lat = Double.parseDouble(message.getData().get("lat"));
        lgn = Double.parseDouble(message.getData().get("lgn"));
        probability = Double.parseDouble(message.getData().get("prob"));

        this.sendNotification(new NotificationData(image, ratingRest, title, text, sound));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendNotification(NotificationData notificationData) {
        Intent intent = new Intent(this, ResponseNotificationActivity.class);

        Log.wtf("location" ,notificationData.getTitle());

        intent.putExtra("loc", notificationData.getTitle().split(",")[0]);
        intent.putExtra("rating", ratingRest);
        intent.putExtra("address", adresaRest);
        intent.putExtra("probability", probability);
        intent.putExtra("latitude", lat);
        intent.putExtra("longitude", lgn);

        intent.addFlags(Intent.FILL_IN_ACTION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = null;
        try {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(URLDecoder.decode(notificationData.getTitle(), "UTF-8"))
                    .setContentText(URLDecoder.decode(notificationData.getTextMessage(), "UTF-8"))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (notificationBuilder != null) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Math.toIntExact((long) notificationData.getRating()), notificationBuilder.build());
        }
    }
}
