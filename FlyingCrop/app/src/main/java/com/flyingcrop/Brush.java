package com.flyingcrop;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Pedro on 13/02/2015.
 */
public class Brush extends Service {

    int color;
    boolean first_down = true;
    Button AllView;
    Button undo;
    Button crop;
    Button close;
    BrushView brushPoint;
    WindowManager wm;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams undo_params;
    WindowManager.LayoutParams crop_params;
    WindowManager.LayoutParams close_params;
    WindowManager.LayoutParams CPparams;
    float x_inicial = 0,y_inicial = 0;
    int color2;
    Boolean onCrop = false;

    ArrayList<BrushView> brush_list = new ArrayList<>();

    ArrayList<View> button_list = new ArrayList<>();
    boolean crop_action = false;



    float mWidth = 0;
    float mHeight = 0;
    float mScreenDensity = 0;

    //TESTE

    Thread thread = new Thread();
    Runnable run;


    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences settings = getSharedPreferences("data", 0);
        final SharedPreferences.Editor editor = settings.edit();

        if(settings.getBoolean("first_brush", true)){
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(it);
            Intent brushIntent = new Intent(getBaseContext(), FirstBrush.class);
            startService(brushIntent);
            stopSelf();
        }

        get_Metrics();




        undo_params = new WindowManager.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , PixelFormat.TRANSPARENT);
        undo_params.gravity = Gravity.RIGHT | Gravity.TOP;

        crop_params = new WindowManager.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , PixelFormat.TRANSPARENT);
        crop_params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

        close_params = new WindowManager.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , PixelFormat.TRANSPARENT);
        close_params.gravity = Gravity.LEFT | Gravity.TOP;

        Notification n  = new Notification.Builder(getBaseContext())
                .setContentTitle("FlyingCrop")
                .setContentText(getResources().getString(R.string.brush_tool))
                .setPriority(2)
                .setWhen(0)
                .setSmallIcon(com.flyingcrop.R.drawable.brush)
                .setOngoing(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        notificationManager.notify(0, n);


        color = settings.getInt("brush_color", Color.parseColor("#FF0000"));
        String str = String.format("#%06X", 0xFFFFFF & color);
        String str2 = str.replace("#", "#" + "80");
        color2 = Color.parseColor(str2);

        //mView = new HUDView(this);
        addButtons();


        AllView = new Button(this);
        AllView.setClickable(true);
        AllView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {

                    x_inicial = event.getX();
                    y_inicial = event.getY();
                    if(first_down) {

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

                    AllView.setBackgroundColor(Color.parseColor("#00FFFFFF")); // Set Transparent
                    AllView.setText("");
                    for(int i = 0; i < button_list.size(); i++)
                        wm.removeView(button_list.get(i));

                }else
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    wm.addView(undo, undo_params);
                    button_list.add(undo);


                    wm.addView(crop, crop_params);
                    button_list.add(crop);

                    wm.addView(close, close_params);
                    button_list.add(close);

                    wm.removeView(AllView);
                    wm.addView(AllView, params);
                    brushPoint.actionUp();

                    undo.bringToFront();
                    crop.bringToFront();

                    AllView.setBackgroundColor(color2);
                    AllView.setText(getResources().getString(R.string.brush_area));

                }else

                if (event.getAction() == MotionEvent.ACTION_MOVE)
                {

                    brushPoint.addPoint(event.getX(), event.getY());
                    brushPoint.refresh();
                    wm.updateViewLayout(brushPoint, CPparams);


                }else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {


                }
                return false;
            }
        });
        AllView.setOnKeyListener(new EditText.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                        stopSelf();
                }
                return false;
            }
        });

        AllView.setBackgroundColor(color2);

        AllView.setText(getResources().getString(R.string.brush_area));
        int textColor = settings.getInt("brush_secondary_color", Color.parseColor("#FFFFFF"));
        AllView.setTextColor(textColor);


        params = new WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                , PixelFormat.TRANSPARENT);
        wm =  (WindowManager) getSystemService(WINDOW_SERVICE);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Available Area");

        wm.addView(AllView, params);

        wm.addView(crop, crop_params);
        button_list.add(crop);

        wm.addView(close, close_params);
        button_list.add(close);
    }

    private void addButtons() {
        final SharedPreferences settings = getSharedPreferences("data", 0);

        int text_color = settings.getInt("brush_secondary_color", Color.parseColor("#FFFFFF"));

        Drawable close_ico = getResources().getDrawable( R.drawable.close );
        ColorFilter filter = new LightingColorFilter( text_color, text_color);
        close_ico.setColorFilter(filter);

        Drawable undo_ico = getResources().getDrawable( R.drawable.undo );
        undo_ico.setColorFilter(filter);

        Drawable crop_ico = getResources().getDrawable( R.drawable.ab_ico );
        crop_ico.setColorFilter(filter);


        close = new Button(getBaseContext());
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

        crop = new Button(getBaseContext());
        crop.setBackground(crop_ico);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllView.setBackgroundColor(Color.parseColor("#00FFFFFF")); // Set Transparent
                AllView.setText("");
                for(int i = 0; i < button_list.size(); i++)
                    wm.removeView(button_list.get(i));
                button_list.clear();
                Intent dialogIntent = new Intent(getBaseContext(), Crop.class);
                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(dialogIntent);
                crop_action = true;
            }
        });
        crop.setOnKeyListener(new EditText.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                        stopSelf();
                }
                return false;
            }
        });

        undo = new Button(getBaseContext());
        undo.setBackground(undo_ico);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brushPoint.removeLast();
                brushPoint.refresh();

            }
        });
        undo.setOnKeyListener(new EditText.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                        stopSelf();
                }
                return false;
            }
        });
    }


    @Override
    public void onDestroy() {

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(0);

        if(!onCrop)
            wm.removeView(AllView);
        SharedPreferences settings = getSharedPreferences("data", 0);

        for(int i = 0; i < brush_list.size(); i++){
            wm.removeView(brush_list.get(i));
        }

        for(int i = 0; i < button_list.size(); i++)
            wm.removeView(button_list.get(i));


        Intent dialogIntent = new Intent(getBaseContext(), NotificationService.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(!settings.getBoolean("first_crop", true) && !crop_action)
            startService(dialogIntent);

        super.onDestroy();
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


    IBinder mBinder = new LocalBinder();



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public View getView() {
        if(brushPoint != null) {
            onCrop = true;
            brush_list.remove(brushPoint);
            wm.removeViewImmediate(AllView);
        }
        return brushPoint;
    }

    public WindowManager getWindowM() {
        return wm;
    }

    public void removeAllView() {
        onCrop = true;
        wm.removeViewImmediate(AllView);
    }

    public class LocalBinder extends Binder {
        public Brush getServerInstance() {
            return Brush.this;
        }
    }

    public void clear(){

        if(brushPoint != null) {

            brushPoint.clear();
            brushPoint.refresh();
            stopSelf();
        }
    }

}

