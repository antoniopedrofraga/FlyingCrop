package com.flyingcrop;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Stop extends Service {
    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent stopIntent = new Intent(getBaseContext(), NotificationService.class);
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopService(stopIntent);

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