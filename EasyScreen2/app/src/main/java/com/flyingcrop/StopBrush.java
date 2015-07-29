package com.flyingcrop;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class StopBrush extends Service {
    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent stopIntent = new Intent(StopBrush.this, Brush.class);
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopService(stopIntent);

        Intent stopIntent2 = new Intent(StopBrush.this, EasyShareService.class);
        stopIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopService(stopIntent2);

        Intent startIntent = new Intent(getBaseContext(), EasyShareService.class);
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