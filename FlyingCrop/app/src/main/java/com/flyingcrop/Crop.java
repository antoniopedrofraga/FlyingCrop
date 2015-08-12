package com.flyingcrop;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by Pedro on 03/08/2015.
 */
public class Crop extends Service {

    int color;
    Button AllView;
    Button close;
    HUDView CropArea;
    WindowManager wm;
    int hit = 1;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams close_params;
    WindowManager.LayoutParams CPparams;
    float x_inicial = 0,y_inicial = 0,x_final,y_final;
    boolean finished = false;

    private static final int[] FROM_COLOR = new int[]{49, 179, 110};
    private static final int THRESHOLD = 3;
    ArrayList<View> buttons = new ArrayList();

    @Override
    public void onCreate() {
        super.onCreate();



        final SharedPreferences settings = getSharedPreferences("data", 0);
        final SharedPreferences.Editor editor = settings.edit();

        if(settings.getBoolean("first_crop", true)){
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(it);
            Intent cropIntent = new Intent(getBaseContext(), FirstCrop.class);
            startService(cropIntent);
            stopSelf();
        }

        close_params = new WindowManager.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , PixelFormat.TRANSPARENT);
        close_params.gravity = Gravity.RIGHT | Gravity.TOP;

        Notification n = new Notification.Builder(getBaseContext())
                .setContentTitle("FlyingCrop")
                .setContentText(getResources().getString(R.string.crop_tool))
                .setPriority(-2)
                .setWhen(0)
                .setSmallIcon(com.flyingcrop.R.drawable.ab_ico)
                .setOngoing(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);


        color = settings.getInt("color", Color.parseColor("#B3CCCCCC"));
        int text_color = settings.getInt("secondary_color", Color.parseColor("#000000"));

        Drawable close_ico = getResources().getDrawable( R.drawable.close );
        ColorFilter filter = new LightingColorFilter( text_color, text_color);
        close_ico.setColorFilter(filter);

        close = new Button(this);
        close.setBackground(close_ico);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
        close.setOnKeyListener(new EditText.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                        stopSelf();
                }
                return false;
            }
        });

        AllView = new Button(this);
        AllView.setTextColor(text_color);
        AllView.setClickable(true);
        AllView.setOnKeyListener(new EditText.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                        stopSelf();
                }
                return false;
            }
        });

        AllView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x,y;

                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    buttons.clear();
                    wm.removeView(close);
                    x_inicial = event.getX();
                    y_inicial = event.getY();
                    AllView.setBackgroundColor(Color.parseColor("#00FFFFFF")); // Set Transparent

                    AllView.setText("");
                    wm.updateViewLayout(AllView, params);
                    CPparams = new WindowManager.LayoutParams(
                            -1, -1,
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSPARENT);

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


                    finished = true;
                    stopSelf();
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
        });

        AllView.setBackgroundColor(color);
        AllView.setText(getResources().getString(R.string.crop_area));
        AllView.bringToFront();

        params = new WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH , PixelFormat.TRANSPARENT);
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        wm.addView(AllView, params);
        wm.addView(close,close_params);
        buttons.add(close);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        SharedPreferences settings = getSharedPreferences("data", 0);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(0);
        if(!finished) wm.removeView(AllView);
        for(int i = 0; i < buttons.size(); i++)
            wm.removeView(buttons.get(i));

        Intent dialogIntent = new Intent(getBaseContext(), NotificationService.class);
        stopService(dialogIntent);

        if(!settings.getBoolean("first_crop", true)) {

            if (!finished) {
                Intent intent = new Intent(getBaseContext(), Brush.class);
                stopService(intent);
                startService(dialogIntent);
            }
        }

        super.onDestroy();
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
}
