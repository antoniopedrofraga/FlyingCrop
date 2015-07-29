package com.flyingcrop;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;


/**
 * Created by Pedro on 13/02/2015.
 */
public class StopService extends Service{

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        stopService(new Intent(StopService.this , CropService.class));

        Intent dialogIntent = new Intent(getBaseContext(), EasyShareService.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startService(dialogIntent);

        this.stopSelf();
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}