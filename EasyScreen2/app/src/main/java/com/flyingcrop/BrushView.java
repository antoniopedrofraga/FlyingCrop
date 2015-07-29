package com.flyingcrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.ViewGroup;

import java.util.ArrayList;

class BrushView extends ViewGroup {
    private Paint mLoadPaint;
    int color;
    int size;
    Bitmap mBitmap;
    Canvas mCanvas;
    float status_bar = 0;

    ArrayList<Point> brush_path = new ArrayList<>();

    int point_pos = 0;

    public BrushView(Context context,int color,int size,float x, float y, int size_x, int size_y, int status_bar) {
        super(context);
        this.color = color;
        this.size = size;
        this.status_bar = status_bar;
        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setColor(color);
        mLoadPaint.setStrokeWidth(10);

        mBitmap = Bitmap.createBitmap(size_x, size_y, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);


        brush_path.add(new Point((int)x,(int)y));

    }

    public void addPoint(float x, float y){
        mCanvas.drawCircle(x,y + status_bar,size,mLoadPaint);
    }


    public void refresh(){
        setWillNotDraw(false);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap,0,0,null);
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }



}