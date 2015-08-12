package com.flyingcrop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ViewGroup;

/**
 * Created by Pedro on 14/02/2015.
 */
class HUDView extends ViewGroup {
    private Paint mLoadPaint;
    private int x_start = 0;
    private int y_start = 0;
    private int x_final = 0;
    private int y_final = 0;

    public HUDView(Context context,int color,int x1,int x2, int y1, int y2) {
        super(context);
        this.x_start = x1;
        this.x_final = x2;
        this.y_start = y1;
        this.y_final = y2;
        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setColor(color);
    }

    public void refresh(int x1,int y1, int x2, int y2){
        this.x_start = x1;
        this.x_final = x2;
        this.y_start = y1;
        this.y_final = y2;
        setWillNotDraw(false);
        invalidate();

    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(x_start, y_start, x_final, y_final, mLoadPaint);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }


}