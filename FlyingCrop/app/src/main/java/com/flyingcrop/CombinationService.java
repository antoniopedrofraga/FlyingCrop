package com.flyingcrop;

import android.app.FragmentManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;


/**
 * Created by Pedro on 03/08/2015.
 */
public class CombinationService extends Service {

    WindowManager wm;
    AudioManager am;
    Button mButton;
    WindowManager.LayoutParams params;
    int combination = 0;



    @Override
    public void onCreate() {
        super.onCreate();


        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);
        mButton = new Button(this);
        mButton.setFocusable(false);
        mButton.setOnKeyListener(new EditText.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {



                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_VOLUME_UP:
                            combination++;
                            Thread thread_up = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        sleep(200);
                                        if(combination < 2)
                                            am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            thread_up.start();
                            break;
                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                            combination++;
                            Thread thread_down = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        sleep(200);
                                        if(combination < 2)
                                            am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            thread_down.start();
                            break;
                    }

                    if (combination == 2) {
                        Intent bIntent = new Intent(getBaseContext(), Brush.class);
                        startService(bIntent);
                        stopSelf();
                    }

                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_VOLUME_UP:
                            combination--;
                            break;
                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                            combination--;
                            break;
                        case KeyEvent.KEYCODE_BACK:
                            break;
                    }

                    return true;
                }
            return false;
            }
        });

        mButton.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        mButton.bringToFront();

        params = new WindowManager.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                , PixelFormat.TRANSPARENT);
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        wm.addView(mButton, params);
    }


    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        wm.removeView(mButton);
    }
}