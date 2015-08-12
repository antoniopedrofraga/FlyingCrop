/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flyingcrop;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.flyingcrop.common.logger.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;

/**
 * Provides UI for the screen capture.
 */
public class ScreenCaptureFragment extends Fragment {

    private static final String TAG = "ScreenCaptureFragment";

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    boolean mBounded;
    Brush mServer;

    private int mScreenDensity;
    public static Image image = null;
    private int mResultCode;
    private Intent mResultData;
    ImageReader mImageReader;
    private Surface mSurface;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private Handler mHandler = new Handler(/*Looper.getMainLooper()*/);
    private Boolean image_available = false;

    private SurfaceView mSurfaceView;
    private Buffer mHeaderBuffer;
    int mWidth = 0;
    int mHeight = 0;
    int screen_width = 0;
    int screen_height = 0;
    DisplayMetrics metrics;
    Bitmap bitmap = null;
    FileOutputStream fos = null;
    int loop = 0;

    //crop bitmap
    float x_inicial;
    float y_inicial;
    float x_final;
    float y_final;
    float status_bar_height;
    String STORE_DIRECTORY;


    //Saving

    String storage;

    public ScreenCaptureFragment(){}




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(com.flyingcrop.R.layout.fragment_screen_capture, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSurfaceView = (SurfaceView) view.findViewById(com.flyingcrop.R.id.surface);
        mSurface = mSurfaceView.getHolder().getSurface();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        mMediaProjectionManager = (MediaProjectionManager)
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startScreenCapture();
    }

    private void get_Metrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Method mGetRawH = null;
        Method mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                mWidth = metrics.widthPixels;
                mHeight = metrics.heightPixels;
                screen_width = mWidth;
                screen_height = mHeight;
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
        Log.d("getMetrics", "Width " + mWidth);
        Log.d("getMetrics", "Height " + mHeight);
        //crop bitmap
        x_inicial = getActivity().getIntent().getFloatExtra("x_inicial", -1);
        y_inicial = getActivity().getIntent().getFloatExtra("y_inicial", -1);
        x_final = getActivity().getIntent().getFloatExtra("x_final", -1);
        y_final = getActivity().getIntent().getFloatExtra("y_final", -1);
        if(y_inicial < 0) y_inicial = 0;

        status_bar_height = getStatusBarHeight();

        SharedPreferences settings = getActivity().getSharedPreferences("data", 0);
        int scale = settings.getInt("scale", 1);
        switch(scale){
            case 0: // low
                mWidth *= 0.25;
                mHeight *= 0.25;
                x_inicial *= 0.25;
                x_final *= 0.25;
                y_inicial *= 0.25;
                y_final *= 0.25;
                status_bar_height *= 0.25;
                break;
            case 1: // medium
                mWidth *= 0.5;
                mHeight *= 0.5;
                x_inicial *= 0.5;
                x_final *= 0.5;
                y_inicial *= 0.5;
                y_final *= 0.5;
                status_bar_height *= 0.5;
                break;
            case 2: //high
                mWidth *= 0.75;
                mHeight *= 0.75;
                x_inicial *= 0.75;
                x_final *= 0.75;
                y_inicial *= 0.75;
                y_final *= 0.75;
                status_bar_height *= 0.75;
                break;

        }
        storage = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(getActivity(), getResources().getString(R.string.fragment_accept), Toast.LENGTH_SHORT).show();
                Intent stopIntent = new Intent(getActivity(), Brush.class);
                getActivity().stopService(stopIntent);
                return;
            }

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            get_Metrics();
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScreenCapture();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownMediaProjection();
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    void startScreenCapture() {
        Activity activity = getActivity();
        if (mSurface == null || activity == null) {
            return;
        }
        if (mMediaProjection != null) {
            setUpVirtualDisplay();
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {

            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    private void setUpVirtualDisplay() {
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                mSurfaceView.getWidth() + "x" + mSurfaceView.getHeight() +
                " (" + mScreenDensity + ")");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                mWidth, mHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface, null, null);
        saveImage();
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    private class VirtualDisplayCallback extends VirtualDisplay.Callback {

        @Override
        public void onPaused() {
            super.onPaused();
            Log.e(TAG, "VirtualDisplayCallback: onPaused");
        }

        @Override
        public void onResumed() {
            super.onResumed();
            Log.e(TAG, "VirtualDisplayCallback: onResumed");
        }

        @Override
        public void onStopped() {
            super.onStopped();
            Log.e(TAG, "VirtualDisplayCallback: onStopped");
        }
    }

    public void saveImage(){
        if (mMediaProjection != null) {


            final Notification.Builder builder = new Notification.Builder(getActivity())
                    .setContentTitle(getResources().getString(R.string.fragment_saving_crop))
                    .setContentText(getResources().getString(R.string.fragment_wait))
                    .setSmallIcon(com.flyingcrop.R.drawable.ab_ico);


            final NotificationManager notificationManager =
                    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, builder.build());


            STORE_DIRECTORY = storage;


            int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

            mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
            mMediaProjection.createVirtualDisplay("screencap", mWidth, mHeight, mScreenDensity, flags, mImageReader.getSurface(), new VirtualDisplayCallback(), mHandler);
            if(ServiceIsRunning()) {
                Intent mIntent = new Intent(getActivity(), Brush.class);
                getActivity().bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
            }

            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {

                    image_available = true;




                    Log.d("teste", "image_available : " + image_available.toString());


                    try {

                        final long time_now =  System.currentTimeMillis();
                        image = mImageReader.acquireLatestImage();


                        Log.i(TAG, "Time 1 " + (System.currentTimeMillis() - time_now));

                        if (image != null) {

                            if(ServiceIsRunning()) {
                                if(loop == 0) {
                                    View v = mServer.getView();
                                    WindowManager wm = mServer.getWindowM();
                                    if (v != null)
                                        wm.removeViewImmediate(v);
                                    else
                                        mServer.removeAllView();

                                }
                                loop++;
                            }

                            if(!isFolder()){
                                Notification.Builder builder = new Notification.Builder(getActivity())
                                        .setContentTitle(getResources().getString(R.string.fragment_error))
                                        .setContentText(getResources().getString(R.string.fragment_error_access))
                                        .setSmallIcon(com.flyingcrop.R.drawable.ab_ico);
                                Toast.makeText(getActivity(), getResources().getString(R.string.fragment_error_toast), Toast.LENGTH_SHORT).show();

                                NotificationManager notificationManager =
                                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                                notificationManager.notify(1, builder.build());

                                getActivity().finish();
                            }

                                    savePlanes();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }

                        if (bitmap != null) {
                            bitmap.recycle();
                        }

                        if (image != null) {
                            image.close();
                        }
                    }
                }

            }, null);
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


    boolean isFolder(){
        File folder = new File(storage + "/FlyingCrop");
        boolean success = true;
        if (!folder.exists()) success = folder.mkdirs();
        if (!success) return false;

        return true;
    }

    void notifySS(Bitmap bitmap, String date, String dir){

        File file =  new File(dir);

        if(file.exists()){
            Log.d("FlyingCrop", "O ficheiro a ser partilhado existe");
        }else{
            Log.d("FlyingCrop", "O ficheiro a ser partilhado não existe");
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));


        PendingIntent i = PendingIntent.getActivity(getActivity(), 0, Intent.createChooser(share, getResources().getString(R.string.fragment_share)),PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "image/png");
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new Notification.Builder(getActivity())
                .setContentTitle(date + ".png")
                .setContentText(getResources().getString(R.string.fragment_img_preview))
                .addAction(android.R.drawable.ic_menu_share, getResources().getString(R.string.fragment_share), i)
                .setSmallIcon(com.flyingcrop.R.drawable.ab_ico)
                        .setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle()
                        .bigPicture(bitmap))
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)

                .build();
        final SharedPreferences settings = getActivity().getSharedPreferences("data", 0);

        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, notif);



        if(settings.getBoolean("vibration", false)){
            Vibrator v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    String getDate(){
        DateFormat df = new SimpleDateFormat("MM_dd_yyyy__HH_mm_ss");

// Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
// Using DateFormat format method we can create a string
// representation of a date with the defined format.
        return df.format(today);
    }

    public Bitmap resize(Bitmap bitmap) {
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if(Math.abs(y_inicial-y_final) < Math.abs(x_inicial-x_final)){
            newHeight = (int) (Math.abs(y_inicial-y_final) * 0.4);
            newWidth = newHeight;
        }else{
            newHeight = (int) (Math.abs(x_inicial-x_final) * 0.4);
            newWidth = newHeight;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }


    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {

            mBounded = false;
            mServer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            Brush.LocalBinder mLocalBinder = (Brush.LocalBinder)service;
            mServer = mLocalBinder.getServerInstance();
            //mServer.clear();
        }
    };


    @Override
    public void onStop() {
        super.onStop();
        if(mBounded) {
            getActivity().unbindService(mConnection);
            mBounded = false;
        }
    };

    public boolean ServiceIsRunning(){
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.flyingcrop.Brush"
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    void savePlanes(){

        Image.Plane[] planes = image.getPlanes();
        Buffer imageBuffer = planes[0].getBuffer().rewind();
        //Log.i(TAG, "Time 2 " + (System.currentTimeMillis() - time_now));

        // create bitmap
        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(imageBuffer);

        int offset = 0;
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;
        ByteBuffer buffer = planes[0].getBuffer();
        //Log.e(TAG, "Time 3 " + (System.currentTimeMillis() - time_now));
        for (int i = 0; i < mHeight; ++i) {
            for (int j = 0; j < mWidth; ++j) {
                int pixel = 0;
                pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                pixel |= (buffer.get(offset + 2) & 0xff);       // B
                pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                bitmap.setPixel(j, i, pixel);
                offset += pixelStride;
            }
            offset += rowPadding;
        }
        //Log.i(TAG, "Time 4 " + (System.currentTimeMillis() - time_now));




        Log.i(TAG, "x_inicial " + x_inicial);
        Log.i(TAG, "x_final " + x_final);
        Log.i(TAG, "y_inicial " + y_inicial);
        Log.i(TAG, "y_final " + y_final);



        bitmap = Bitmap.createBitmap(bitmap,(int) x_inicial ,(int)status_bar_height + (int)y_inicial , Math.abs((int)x_final - (int) x_inicial), Math.abs((int)y_final - (int) y_inicial));
        //bitmap = Bitmap.createBitmap(bitmap, 0 ,0,mWidth, mHeight);
        // write bitmap to a file
        SharedPreferences settings = getActivity().getSharedPreferences("data", 0);

        if(!settings.getBoolean("watermark",false)) {
            Canvas mCanvas = new Canvas(bitmap);

            Bitmap watermark = resize(BitmapFactory.decodeResource(getActivity().getResources(),
                    R.drawable.watermark));

            mCanvas.drawBitmap(watermark, 0, 0, null);



        }


        String date = getDate();
        String dir = STORE_DIRECTORY + "/FlyingCrop/"+ date + ".png";
        try {
            fos = new FileOutputStream(dir);
        }catch(Exception e){

        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        //Log.e(TAG, "Time 5 " + (System.currentTimeMillis() - time_now));
        File file = new File(dir);
        // MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());

        if(settings.getBoolean("toast",true)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.fragment_img_saved) + " " + dir, Toast.LENGTH_SHORT).show();
        }

        Intent mIntent = new Intent(getActivity(), Brush.class);
        getActivity().stopService(mIntent);

        notifySS(bitmap, date, dir);


        MediaScannerConnection.scanFile(getActivity(), new String[] {dir}, null, null);


        mImageReader = null;
        getActivity().finish();
    }



}
