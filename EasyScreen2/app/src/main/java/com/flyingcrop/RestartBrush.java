package com.flyingcrop;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RestartBrush extends Service {
    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent stopIntent2 = new Intent(RestartBrush.this, Brush.class);
        stopIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopService(stopIntent2);

        Intent startIntent = new Intent(RestartBrush.this, Brush.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startService(startIntent);

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