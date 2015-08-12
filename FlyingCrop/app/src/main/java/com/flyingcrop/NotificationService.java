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
public class NotificationService extends Service{

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

                final SharedPreferences settings = getSharedPreferences("data", 0);

                if(settings.getInt("type", 0) == 1){
                    if(settings.getBoolean("first_button", true)){
                        Intent bIntent = new Intent(getBaseContext(), FirstButton.class);
                        startService(bIntent);
                    }else {
                        Intent bIntent = new Intent(getBaseContext(), ButtonService.class);
                        startService(bIntent);
                    }
                    stopSelf();
                }else{
                    if (settings.getBoolean("first_notification", true)) {
                        Intent bIntent = new Intent(getBaseContext(), FirstNotification.class);
                        startService(bIntent);
                        stopSelf();
                    }
                }

                Intent dialogIntent = new Intent(getBaseContext(), Crop.class);

                PendingIntent pIntent = PendingIntent.getService(getBaseContext(), 0, dialogIntent, 0);

                Intent stopIntent = new Intent(getBaseContext(), Stop.class);

                stopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent sIntent = PendingIntent.getService(getBaseContext(), 0, stopIntent, 0);

                Intent brushIntent = new Intent(getBaseContext(), Brush.class);
                PendingIntent bIntent = PendingIntent.getService(getBaseContext(), 0, brushIntent, 0);

                Intent flIntent = new Intent(getBaseContext(), MainMenu.class);
                PendingIntent fIntent = PendingIntent.getActivity(getBaseContext(), 0, flIntent, 0);



                Notification.Builder notif  = new Notification.Builder(getBaseContext())
                        .setContentTitle("FlyingCrop")
                        .setContentText(getResources().getString(R.string.notification_launch_menu))
                        .setSubText(getResources().getString(R.string.notification_scroll_down))
                        .setPriority(Notification.PRIORITY_MIN)
                        .setWhen(0)
                        .setContentIntent(fIntent)
                        .setSmallIcon(com.flyingcrop.R.drawable.ab_ico)
                        .setOngoing(true);


                            notif.addAction(R.drawable.nicon_crop, "", pIntent);
                            notif.addAction(R.drawable.brush, "", bIntent);
                            notif.addAction(R.drawable.dismiss, "", sIntent);



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
