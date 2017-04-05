package com.dorian.licenta.ServiceNotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dorian.licenta.ServiceNotification.ServiceNotification;


/**
 * Created by misch on 05.04.2017.
 */

public class NotificationRestartBrodcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ServiceNotification.class));
    }

}
