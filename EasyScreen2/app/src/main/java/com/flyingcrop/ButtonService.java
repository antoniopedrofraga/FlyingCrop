package com.flyingcrop;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.flyingcrop.common.logger.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Pedro on 03/08/2015.
 */
public class ButtonService extends Service {

    WindowManager wm;
    Button mButton;
    ImageView close;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams x_params;
    AlphaAnimation anim;
    int button_size;
    int mWidth;
    int mHeight;
    Vibrator vib;
    boolean exit = false;
    Drawable close_ico_red;
    long time_now = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);
        getMetrics();

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences settings = getSharedPreferences("data", 0);
        int size = settings.getInt("button_size", 50);
        button_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
        params = new WindowManager.LayoutParams(
                button_size, button_size,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                , PixelFormat.TRANSPARENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        if(settings.getInt("params.x",-1) != -1){
            params.x = settings.getInt("params.x",0);
            params.y = settings.getInt("params.y",0);
        }

        close = new ImageView(this);
        close.setImageResource(R.drawable.remove);

        int close_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size + 20, getResources().getDisplayMetrics());
        x_params = new WindowManager.LayoutParams(
                close_size, close_size,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , PixelFormat.TRANSPARENT);
        x_params.gravity = Gravity.LEFT | Gravity.TOP;

        x_params.x = mWidth / 2 - close_size / 2;
        x_params.y = mHeight / 2 - close_size / 2 - getStatusBarHeight();

        close_ico_red = getResources().getDrawable( R.drawable.remove_red );






        mButton = new Button(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Click", Toast.LENGTH_SHORT).show();
            }
        });
        mButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    time_now =  System.currentTimeMillis();
                    wm.addView(close, x_params);
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    params.x = (int)event.getRawX() - mButton.getWidth() / 2;
                    params.y = (int)event.getRawY() - mButton.getHeight() / 2 - getStatusBarHeight();
                    wm.updateViewLayout(mButton, params);

                    if(Math.abs(params.x - x_params.x) < button_size / 2 && Math.abs(params.y - x_params.y) < button_size / 2 && exit == false){
                        vib.vibrate(25);
                        close.setImageDrawable(close_ico_red);
                        wm.updateViewLayout(close, x_params);
                        exit = true;
                    }else if(!(Math.abs(params.x - x_params.x) < button_size / 2 && Math.abs(params.y - x_params.y) < button_size / 2) && exit == true){
                        close.setImageResource(R.drawable.remove);
                        wm.updateViewLayout(close, x_params);
                        exit = false;
                    }
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    wm.removeView(close);
                    if(exit){
                        stopSelf();
                    }else{
                        final SharedPreferences settings = getSharedPreferences("data", 0);
                        final SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("params.x", params.x);
                        editor.putInt("params.y", params.y);
                        editor.commit();
                    }
                    if(System.currentTimeMillis() - time_now < 100){
                        Intent bIntent = new Intent(getBaseContext(), Brush.class);
                        startService(bIntent);
                        stopSelf();
                    }
                    return true;
                }

                return false;
            }
        });
        mButton.setBackground(getResources().getDrawable(R.drawable.button));


        wm.addView(mButton, params);
    }
        @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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

    @Override
    public void onDestroy() {
        wm.removeView(mButton);
    }
}
