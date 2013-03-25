package com.gesuper.lightclock.view;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.AlertItemModel;
import com.gesuper.lightclock.model.BgColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AlertListView extends ListView {

	public static final String TAG = "AlertListView";
	public static final int PULL_DOWN = 0;
	public static final int RELEASE_UP = 1;
	public static final int REFRESH_DONE = 2;
	
	private MainView mMainView;
	
	private boolean isRecored;
	private boolean isRefreshable;
	private int firstItemIndex;
	private int startY;
	private int status;
	
	private int RATIO = 1;
	private AlertItemView headView;
	private int headContentHeight;
	private TextView mTextView;
	
	private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private AlertItemView mDragItemView;
    private Rect mTempRect = new Rect();
	private Bitmap mDragBitmap;
	private ImageView mDragView;
	//开始拖动时的位置
	private int mDragStartPosition;
	//当前的位置
	private int mDragCurrentPostion;
	//当前位置距离边界的位置
	private int mDragOffsetX;
	private int mDragOffSetY;
	//移动的位置
	private int mDragPointX;
	private int mDragPointY;
	//边界
	private int mUpperBound;
	private int mLowerBound;

	private int mHeight;
	private int mTouchSlop;
	private int mDragPos;
	private int mFirstDragPos;
	private GestureDetector mGestureDetector;
	
	private long lastDownTime;
	private boolean isFirstMove;
	private boolean isLongPressed;
	public AlertListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    // TODO Auto-generated constructor stub
	    this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();  
	    this.initResource();
	}
	
	private void initResource(){
		//hide scroll bar
		this.setVerticalScrollBarEnabled(true);
		this.isRecored = false;
		this.isRefreshable = true;
		this.mHeight = this.getHeight();
		this.status = REFRESH_DONE;
		
		this.isFirstMove = true;
		this.isLongPressed = false;
		this.mDragItemView = null;
		AlertItemModel mItemModel = new AlertItemModel();
		mItemModel.setId((long) -2);
		
		headView = new AlertItemView(this.getContext());
		headView.setModel(mItemModel);
		headView.showMenu();
		mTextView = (TextView)headView.findViewById(R.id.tv_content);
		this.measureView(headView);
		this.headContentHeight = this.headView.getMeasuredHeight();
		Log.d(TAG, "headContentHeight" + this.headContentHeight);
		this.headView.setPadding(0, -1 * this.headContentHeight, 0, 0);
		this.headView.invalidate();
		this.addHeaderView(headView);

		//this.mGestureDetector = new GestureDetector(this.getContext(), new MyGestureListener());
		//this.mGestureDetector.setIsLongpressEnabled(true);
	}
	
	

	private void createNewAlert() {
		// TODO Auto-generated method stub
		this.headView.setPadding(0, -this.headContentHeight, 0, 0);
		this.mMainView.addNewItem(this.headView.getModel().getBgColorId());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		final int x, y;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "ACTION DOWN");
			x = (int)event.getX();
			y = (int)event.getY();
			final int k = AlertListView.this.pointToPosition(x, y);
			if( k == ListView.INVALID_POSITION ){
				break;
			}
			mDragItemView = (AlertItemView)this.getChildAt(k - this.getFirstVisiblePosition());
			
			if(mDragItemView.getStatus() == AlertItemView.STATUS_NORMAL){
				this.mDragPointY = y - mDragItemView.getTop();
				this.mDragOffSetY = (int) (event.getRawY() - y);
				
				mDragItemView.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View v) {
						// TODO Auto-generated method stub
						int height = getHeight();
						mUpperBound = Math.min(y - mTouchSlop, height / 3);
						mLowerBound = Math.max(y + mTouchSlop, height * 2 / 3);
						mDragCurrentPostion = mDragStartPosition = k;
						mDragItemView.setDrawingCacheEnabled(true);
						Bitmap bitmap = Bitmap.createBitmap(mDragItemView.getDrawingCache());
						startDrag(bitmap, y);
						mDragItemView.setVisibility(View.INVISIBLE);
						return false;
					}
				});
			}
			break;
		case MotionEvent.ACTION_UP:
			stopDrag();
			if(mDragItemView != null)
				mDragItemView.setVisibility(View.VISIBLE);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_MOVE:
			y = (int) event.getY();
			dragView(y);
			if (y >= getHeight() / 3) {
	            mUpperBound = getHeight() / 3;
	        }
	        if (y <= getHeight() * 2 / 3) {
	            mLowerBound = getHeight() * 2 / 3;
	        }
			int speed = 0;
			if (y > mLowerBound) {
                if (getLastVisiblePosition() < getCount() - 1) {
                    speed = y > (getHeight() + mLowerBound) / 2 ? 16 : 4;
                } else {
                    speed = 1;
                }
            } else if (y < mUpperBound) {
                speed = y < mUpperBound / 2 ? -16 : -4;
                if (getFirstVisiblePosition() == 0
                        && getChildAt(0).getTop() >= getPaddingTop()) {
                    speed = 0;
                }
            }
            if (speed != 0) {
                smoothScrollBy(speed, 30);
            }
            break;
		}
		return super.onTouchEvent(event);
	}

	private void changeHeaderViewByStatus() {
		// TODO Auto-generated method stub
		switch(status){
		case PULL_DOWN:
			this.mTextView.setHint("pull down to create");
			this.RATIO = 1;
			break;
		case RELEASE_UP:
			this.RATIO = 4;
			this.mTextView.setHint("release to create");
			break;
		case REFRESH_DONE:
			this.RATIO = 1;
			this.mTextView.setHint("click to edit");
			break;
		}
	}
	
	public View getItemAt(int index){
		return super.getChildAt(index+1);
	}

	public void startDrag(Bitmap mBitmap, int y){
		this.stopDrag();
		this.mWindowParams = new WindowManager.LayoutParams();
		this.mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		
		this.mWindowParams.alpha = (float) 0.5;
		this.mWindowParams.x = 0;
		this.mWindowParams.y = y - mDragPointY + mDragOffSetY;

		this.mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		this.mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		this.mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		this.mWindowParams.format = PixelFormat.TRANSLUCENT;
		this.mWindowParams.windowAnimations = 0;
        
        Context context = this.getContext();
        ImageView v = new ImageView(context);
        v.setBackgroundColor(BgColor.appwidget_text);
        v.setImageBitmap(mBitmap);
        mDragBitmap = mBitmap;

        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
	}
	

	private void dragView(int y) {
		if(mDragView != null){
			mWindowParams.x = 0;
	        mWindowParams.y = y - mDragPointY + mDragOffSetY;
	        mWindowManager.updateViewLayout(mDragView, mWindowParams);
		}
		int tempPosition = pointToPosition(0, y);
		if(tempPosition == INVALID_POSITION){
			return ;
		}
		if(mDragCurrentPostion != tempPosition){
			mDragCurrentPostion = tempPosition;
			this.mMainView.exchangeAdapterItem(this.mDragStartPosition, mDragCurrentPostion);
			
		}
		
		//滚动
		int scrollY = 0;
		if(y < mUpperBound){
			scrollY = 8;
		}else if(y > mLowerBound){
			scrollY = -8;
		}
		
		if(scrollY != 0){
			int top = this.getChildAt(mDragCurrentPostion - this.getFirstVisiblePosition()).getTop();
			setSelectionFromTop(mDragCurrentPostion, top + scrollY);
		}
    }


	
	public void stopDrag(){
		if (mDragView != null) {
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }
	}
	
	private void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }
	
	public void setMainView(MainView mainView) {
		// TODO Auto-generated method stub
		this.mMainView = mainView;
	}
	
	public boolean isRecored(){
		return this.isRecored;
	}
	
	@SuppressWarnings("deprecation")
	public void measureView(View child){
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if(p == null){
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);  
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if(lpHeight > 0){
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		}else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	private class MyGestureListener extends SimpleOnGestureListener  {
	
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onDown");
			return true;
		}
	
		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onShowPress");
		}
	
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onSingleTapUp");
			return true;
			
		}
	
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onScroll");
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onLongPress");
			int x = (int)e.getX();
			int y = (int)e.getY();
			int k = AlertListView.this.pointToPosition(x, y);
			if( k == ListView.INVALID_POSITION ){
				super.onLongPress(e);
				return ;
			}
			AlertItemView mItemView = (AlertItemView) AlertListView.this.getChildAt(k - AlertListView.this.getFirstVisiblePosition());
			Log.d(TAG, "" + k + " " + AlertListView.this.getFirstVisiblePosition());
			if(mItemView.getStatus() == AlertItemView.STATUS_NORMAL){
				AlertListView.this.mDragPointY = y - mItemView.getTop();
				AlertListView.this.mDragOffSetY = (int) (e.getRawY() - y);
				mItemView.setDrawingCacheEnabled(true);
				Bitmap mBitmap = Bitmap.createBitmap(mItemView.getDrawingCache());
				AlertListView.this.startDrag(mBitmap, y);
				
				AlertListView.this.mDragPos = k;
				AlertListView.this.mFirstDragPos = k;
				AlertListView.this.mUpperBound = Math.min(y - AlertListView.this.mTouchSlop, AlertListView.this.mHeight / 3);
				AlertListView.this.mLowerBound = Math.min(y + AlertListView.this.mTouchSlop, AlertListView.this.mHeight / 3);
				
			}
			Log.i(TAG, "onLongPress end");
			super.onLongPress(e);
		}
	
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onFling");
			return super.onFling(e1, e2, velocityX, velocityY);
		}	
	}
}
