package com.xiaosche.sdncardmonitor;
import com.example.sdncardmonitor.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class WaterMarkView extends View {

	Bitmap mBitmap;
	Paint mPaint;
    public WaterMarkView(Context context) {
        super(context);
        setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
            ,LinearLayout.LayoutParams.MATCH_PARENT)); 
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.notconnected);
        mPaint = new Paint();;
    }

    public WaterMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);

        mPaint.setAlpha(98);
        canvas.drawBitmap(mBitmap, 70, 100, mPaint);
    }
}