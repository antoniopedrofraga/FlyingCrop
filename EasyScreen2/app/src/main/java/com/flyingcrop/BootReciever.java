package com.flyingcrop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Pedro on 19/07/2015.
 */
public class BootReciever extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        SharedPreferences settings = context.getSharedPreferences("data", 0);
        boolean bootRun = settings.getBoolean("boot", false);

        if(bootRun) {
            Intent myIntent = new Intent(context, EasyShareService.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(myIntent);
        }
    }

}