package com.gesuper.lightclock.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;



public class TopRectView extends View {
	public static final String TAG = "TopRectView";
	
	private Paint mPaint;
	private Rect mItem;
	public TopRectView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mPaint = new Paint();
		this.mItem = null;
	}
	
	public TopRectView(Context context, Rect item){
		this(context);
		
		this.mPaint = new Paint();
		this.mItem = item;
		
		//Log.d(TAG, "rect: " + item.toShortString());
	}
	
	public  void onDraw(Canvas canvas){
		super.onDraw(canvas);  
		  
        /* 设置画布的颜色 */  
        canvas.drawColor(Color.TRANSPARENT);
  
        /* 设置取消锯齿效果 */  
        mPaint.setAntiAlias(true);
        if(this.mItem != null){
        	mPaint.setColor(Color.WHITE);
        	canvas.drawRect(mItem, mPaint);
        }
	}
}
