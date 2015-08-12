package com.flyingcrop;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Pedro on 06/08/2015.
 */
public class FirstNotification extends Service {
    WindowManager wm;
    Button overlay;
    Button next;
    TextView text;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams next_params;
    WindowManager.LayoutParams image_params;
    WindowManager.LayoutParams text_params;
    int mWidth;
    int mHeight;
    Button image;

    @Override
    public void onCreate() {

        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);
        getMetrics();

        params = new WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                , PixelFormat.TRANSPARENT);

        next_params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                , PixelFormat.TRANSPARENT);

        next_params.gravity = Gravity.RIGHT | Gravity.BOTTOM;

        text_params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                , PixelFormat.TRANSPARENT);
        text_params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        text_params.y = mWidth;

        image_params = new WindowManager.LayoutParams(
                mWidth, mWidth,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                , PixelFormat.TRANSPARENT);
        image_params.gravity = Gravity.LEFT| Gravity.TOP;

        text = new TextView(this);
        text.setText(getResources().getString(R.string.first_notification_first));
        text.setTextColor(Color.parseColor("#FFFFFF"));
        overlay =  new Button(this);//HUDView(getBaseContext(), Color.parseColor("#B3CCCCCC"), 0, 0, mWidth, mHeight);
        overlay.setBackgroundColor(Color.parseColor("#B3000000")); //testiing

        next = new Button(this);
        next.setText(getResources().getString(R.string.got_it));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("data", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("first_notification", false);
                editor.commit();
                Intent serviceIntent = new Intent(getApplicationContext(), NotificationService.class);
                startService(serviceIntent);
                stopSelf();
            }
        });

        image =  new Button(this);
        image.setBackground(getResources().getDrawable(R.drawable.notification));


        wm.addView(overlay, params);
        wm.addView(image, image_params);
        wm.addView(text, text_params);
        wm.addView(next, next_params);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        wm.removeView(overlay);
        wm.removeView(next);
        wm.removeView(text);
        wm.removeView(image);
    }

    private void getMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        Method mGetRawH = null;
        Method mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                mWidth = metrics.widthPixels;
                mHeight = metrics.heightPixels;

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
}
