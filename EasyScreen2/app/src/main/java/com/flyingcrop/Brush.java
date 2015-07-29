package com.flyingcrop;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Pedro on 13/02/2015.
 */
public class Brush extends Service implements View.OnTouchListener {

    //mView = new HUDView(this);
    int color;
    boolean first_down = true;
    Button AllView;
    BrushView brushPoint;
    WindowManager wm;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams CPparams;
    float x_inicial = 0,y_inicial = 0,x_final,y_final;
    boolean finished = false;

    ArrayList<BrushView> brush_list = new ArrayList<>();

    float mWidth = 0;
    float mHeight = 0;
    float mScreenDensity = 0;

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

        get_Metrics();

        Intent dialogIntent = new Intent(getBaseContext(), CropService.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getService(getBaseContext(), 0, dialogIntent, 0);

        Intent cancelIntent = new Intent(getBaseContext(), StopBrush.class);
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent cIntent = PendingIntent.getService(getBaseContext(), 0, cancelIntent, 0);

        Intent restartIntent = new Intent(getBaseContext(), RestartBrush.class);
        restartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent rIntent = PendingIntent.getService(getBaseContext(), 0, restartIntent, 0);


        Notification n  = new Notification.Builder(getBaseContext())
                .setContentTitle("FlyingCrop")
                .setContentText("Hit to crop. (Painting)")
                .setPriority(2)
                .setWhen(0)
                .setContentIntent(pIntent)
                .setSmallIcon(com.flyingcrop.R.drawable.brush)
                .addAction(R.drawable.clean, "Restart", rIntent)
                .addAction(R.drawable.go_back, "Cancel", cIntent)
                .setOngoing(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        notificationManager.notify(0, n);


        final SharedPreferences settings = getSharedPreferences("data", 0);
        color = settings.getInt("brush_color", Color.parseColor("#FF0000"));
        String str = String.format("#%06X", 0xFFFFFF & color);
        String str2 = str.replace("#", "#" + "33");
        int color2 = Color.parseColor(str2);

        //mView = new HUDView(this);

        AllView = new Button(this);
        AllView.setClickable(true);
        AllView.setOnTouchListener(this);
        AllView.setBackgroundColor(color2);
        AllView.setText("brushable area");
        AllView.bringToFront();

        params = new WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                , PixelFormat.TRANSPARENT);
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Available Area");

        wm.addView(AllView, params);


    }

    @Override
    public void onDestroy() {
        if(!finished) {
            wm.removeView(AllView);
        }
        for(int i = 0; i < brush_list.size(); i++){
            wm.removeView(brush_list.get(i));
        }

        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x,y;

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {

            x_inicial = event.getX();
            y_inicial = event.getY();
            if(first_down) {
                AllView.setBackgroundColor(Color.parseColor("#00FFFFFF")); // Set Transparent
                AllView.setText("");
                wm.updateViewLayout(AllView, params);

                CPparams = new WindowManager.LayoutParams(
                        -1, -1,
                        WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.TRANSPARENT);


                CPparams.gravity = Gravity.LEFT | Gravity.TOP;
                SharedPreferences settings = getSharedPreferences("data", 0);
                int size = settings.getInt("size", 20);

                brushPoint = new BrushView(getBaseContext(), color, size, x_inicial, y_inicial, (int)mWidth, (int)mHeight, getStatusBarHeight());
                brushPoint.refresh();


                wm.addView(brushPoint, CPparams);
                brush_list.add(brushPoint);
                first_down = false;
            }else{
                brushPoint.addPoint(event.getX(), event.getY());
                brushPoint.refresh();
                wm.updateViewLayout(brushPoint, CPparams);
            }
            //wm.updateViewLayout(CropArea, CPparams);

            //params.width = 100;//change SIZE UTIL!!!
            //params.height= 100;
            /*
            params.width = 100;//change SIZE UTIL!!!
            params.height= 100;
            params.x = 100;
            params.y = 100;
            */
            return true;

        }else
        if (event.getAction() == MotionEvent.ACTION_UP) {
            wm.removeView(AllView);
            wm.addView(AllView, params);
            return true;
        }else

        if (event.getAction() == MotionEvent.ACTION_MOVE)
        {

            brushPoint.addPoint(event.getX(), event.getY());
            brushPoint.refresh();
            wm.updateViewLayout(brushPoint, CPparams);

            return true;
        }else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {

            return true;
        }
        return v.onTouchEvent(event);
    }

    private void get_Metrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Method mGetRawH = null;
        Method mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                mWidth = metrics.widthPixels;
                mHeight = metrics.heightPixels;
                mScreenDensity = metrics.densityDpi;
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    mWidth = (Integer) mGetRawW.invoke(display);
                    mHeight = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
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

