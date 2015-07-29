package com.flyingcrop;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by Pedro on 13/02/2015.
 */
public class CropService extends Service implements View.OnTouchListener {

    //mView = new HUDView(this);
    int color;
    Button AllView;
    HUDView CropArea;
    WindowManager wm;
    int hit = 1;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams CPparams;
    float x_inicial = 0,y_inicial = 0,x_final,y_final;
    RelativeLayout relativeLayout;
    Bitmap myBitmap;
    boolean finished = false;

    //TESTE





    //----
    @Override
    public IBinder
    onBind(Intent intent) {
        return null;
    }




    @Override
    public void onCreate() {
        super.onCreate();

        Intent dialogIntent = new Intent(getBaseContext(), StopService.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getService(getBaseContext(), 0, dialogIntent, 0);
        Notification n  = new Notification.Builder(getBaseContext())
                .setContentTitle("FlyingCrop")
                .setContentText("Hit to stop cropping.")
                .setPriority(-2)
                .setWhen(0)
                .setContentIntent(pIntent)
                .setSmallIcon(com.flyingcrop.R.drawable.ab_ico)
                .setOngoing(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);


        final SharedPreferences settings = getSharedPreferences("data", 0);
        color = settings.getInt("color", Color.parseColor("#B3CCCCCC"));
        //mView = new HUDView(this);

        AllView = new Button(this);
        AllView.setClickable(true);
        AllView.setOnTouchListener(this);
        AllView.setBackgroundColor(color);
        AllView.setText("cropable area");
        AllView.bringToFront();

        params = new WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Available Area");

        wm.addView(AllView, params);

    }

    @Override
    public void onDestroy() {
        if(!finished) wm.removeView(AllView);
        
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x,y;

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
                x_inicial = event.getX();
                y_inicial = event.getY();
                AllView.setBackgroundColor(Color.parseColor("#00FFFFFF")); // Set Transparent
                AllView.setText("");
                wm.updateViewLayout(AllView, params);
                CPparams = new WindowManager.LayoutParams(
                        -1, -1,
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, PixelFormat.TRANSPARENT);

                CPparams.gravity = Gravity.LEFT | Gravity.TOP;
                CropArea = new HUDView(getBaseContext(),color,(int)x_inicial,(int)y_inicial,(int)x_inicial,(int)y_inicial);

                wm.addView(CropArea,CPparams);
                    //params.width = 100;//change SIZE UTIL!!!
                //params.height= 100;
            /*
            params.width = 100;//change SIZE UTIL!!!
            params.height= 100;
            params.x = 100;
            params.y = 100;
            */


        }
        if (event.getAction() == MotionEvent.ACTION_UP) {


                //Test
                wm.removeViewImmediate(CropArea);
                wm.removeView(AllView);
                fix_values();

                if (android.os.Build.VERSION.SDK_INT >= 21) {

                    Intent dialogIntent = new Intent(getBaseContext(), EasyCrop.class);
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    dialogIntent.putExtra("x_inicial", x_inicial);
                    dialogIntent.putExtra("y_inicial", y_inicial);
                    dialogIntent.putExtra("x_final", x_final);
                    dialogIntent.putExtra("y_final", y_final);
                    getApplication().startActivity(dialogIntent);
                }
                Intent dialogIntent = new Intent(getBaseContext(), EasyShareService.class);
                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startService(dialogIntent);
                finished = true;
                this.stopSelf();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            x_final = event.getX();
            y_final = event.getY();
            int x_inicial_usado = 0, y_inicial_usado = 0, x_final_usado = 0, y_final_usado = 0;

            if(x_final < x_inicial){
                x_inicial_usado = (int)x_final;
                x_final_usado = (int)x_inicial;
            }else{
                x_inicial_usado = (int)x_inicial;
                x_final_usado = (int)x_final;
            }

            if(y_final < y_inicial){
                y_inicial_usado = (int)y_final;
                y_final_usado = (int)y_inicial;
            }else{
                y_inicial_usado = (int)y_inicial;
                y_final_usado = (int)y_final;
            }

            CropArea.refresh(x_inicial_usado,y_inicial_usado,x_final_usado,y_final_usado);
            wm.updateViewLayout(CropArea, CPparams);

        }
        return false;
    }

    private void fix_values() {
        if(x_inicial > x_final){
            float temp = x_inicial;
            x_inicial = x_final;
            x_final = temp;
        }
        if(y_inicial > y_final){
            float temp = y_inicial;
            y_inicial = y_final;
            y_final = temp;
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }





}