package com.flyingcrop;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;


/**
 * Created by Pedro on 13/02/2015.
 */
public class EasyShareService extends Service{

    private boolean isRunning  = false;
    long volume_down;
    @Override
    public void onCreate() {
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid AN



                Intent dialogIntent = new Intent(getBaseContext(), CropService.class);
                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pIntent = PendingIntent.getService(getBaseContext(), 0, dialogIntent, 0);

                Intent stopIntent = new Intent(getBaseContext(), Stop.class);
                stopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent sIntent = PendingIntent.getService(getBaseContext(), 0, stopIntent, 0);

                Intent brushIntent = new Intent(getBaseContext(), Brush.class);
                stopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent bIntent = PendingIntent.getService(getBaseContext(), 0, brushIntent, 0);



                Notification.Builder notif  = new Notification.Builder(getBaseContext())
                        .setContentTitle("FlyingCrop")
                        .setContentText("Hit to crop.")
                        .setPriority(Notification.PRIORITY_MIN)
                        .setWhen(0)
                        .setContentIntent(pIntent)
                        .setSmallIcon(com.flyingcrop.R.drawable.ab_ico)
                        .setOngoing(true);
                        SharedPreferences settings = getBaseContext().getSharedPreferences("data", 0);

                        if(!settings.getBoolean("hide", false)) {
                            notif.addAction(R.drawable.brush, "Brush", bIntent);
                            notif.addAction(R.drawable.dismiss, "Dismiss", sIntent);
                        }


                Notification n  = notif.build();
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(0, n);
                //Your logic that service will perform will be placed here
                //In this example we are just looping and waits for 1000 milliseconds in each loop.

                //Stop service once it finishes its task



        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        NotificationManager notificationManager = (NotificationManager) getBaseContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }
}
