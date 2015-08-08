package com.flyingcrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;

class BrushView extends ViewGroup {
    private Paint mLoadPaint;
    int color;
    int size;
    int size_x;
    int size_y;
    Bitmap mBitmap;
    Canvas mCanvas;
    float status_bar = 0;

    ArrayList<ArrayList<Point>> history = new ArrayList<ArrayList<Point>>();

    int point_pos = 0;

    public BrushView(Context context,int color,int size,float x, float y, int size_x, int size_y, int status_bar) {
        super(context);
        this.color = color;
        this.size = size;
        this.status_bar = status_bar;
        this.size_x = size_x;
        this.size_y = size_y;
        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setColor(color);
        mLoadPaint.setStrokeWidth(size);

        mBitmap = Bitmap.createBitmap(size_x, size_y, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        history.add(new ArrayList<Point>());
        history.get(0).add(new Point((int) x, (int) y));

    }

    public void addPoint(float x, float y){
        if(history.size() > 0) {
            ArrayList<Point> last = history.get(history.size() - 1);

            if (last.size() != 0) {
                float x_anterior = last.get(last.size() - 1).x;
                float y_anterior = last.get(last.size() - 1).y;
                mCanvas.drawLine(x_anterior, y_anterior + status_bar, x, y + status_bar, mLoadPaint);
            }
            last.add(new Point((int) x , (int) y));
        }else{
            history.add(new ArrayList<Point>());
            history.get(0).add(new Point((int) x, (int) y));
        }

    }

    public void actionUp(){
        history.add(new ArrayList<Point>());
    }

    public void removeLast(){
        if(history.size() > 0) {
            history.remove(history.size() - 1);
            history.remove(history.size() - 1);

            mBitmap = Bitmap.createBitmap(size_x, size_y, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            ArrayList<ArrayList<Point>> temp = new ArrayList<>(history);
            history.clear();
            for (int draws = 0; draws < temp.size(); draws++) {
                for (int points = 0; points < temp.get(draws).size(); points++) {
                    addPoint(temp.get(draws).get(points).x, temp.get(draws).get(points).y);
                }
                    actionUp();
            }
        }
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

    public void clear(){
        history.clear();
        mBitmap = Bitmap.createBitmap(size_x, size_y, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        actionUp();
    }
}