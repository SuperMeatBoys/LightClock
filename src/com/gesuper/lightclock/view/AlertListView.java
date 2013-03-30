package com.gesuper.lightclock.view;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.AlertItemModel;
import com.gesuper.lightclock.model.BgColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AlertListView extends ListView{

	public static final String TAG = "AlertListView";
	public static final int NORMAL = 0;
	public static final int CREATE_PULL_DOWN = 1;
	public static final int CREATE_RELEASE_UP = 2;
	public static final int CREATE_REFRESH_DONE = 3;
	public static final int SEQUENCE = 4;
	
	private MainView mMainView;
	private GestureDetector mSimpleGesture;
	private boolean isRecored;
	private int firstItemIndex;
	private int startY;
	private int status;
	private int paddingTop;
	
	private int RATIO = 2;
	private AlertItemView headView;
	private int headContentHeight;
	private TextView mTextView;
	
	private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private AlertItemView mDragItemView;
	private Bitmap mDragBitmap;
	private ImageView mDragView;
	//开始位置
	private int mDragStartPosition;
	//当前的位置
	private int mDragCurrentPostion;
	private int mDragOffSetY;
	//移动的位置
	private int mDragPointX;
	private int mDragPointY;
	//边界
	private int mUpperBound;
	private int mLowerBound;

	private int mHeight;
	private int mTouchSlop;
	
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
		this.mHeight = this.getHeight();
		this.status = CREATE_REFRESH_DONE;
		this.mDragItemView = null;
		this.mDragStartPosition = -1;
		this.setDivider(null);
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
		
		this.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.i(TAG, "on item long click " + position);
				status = SEQUENCE;
				View itemContent = view.findViewById(R.id.alert_content);
				itemContent.setDrawingCacheEnabled(true);
				Bitmap bitmap = Bitmap.createBitmap(itemContent.getDrawingCache());
				startDrag(bitmap, mDragPointY + view.getTop());
				//view.setVisibility(View.INVISIBLE);
				return false;
			}
			
		});
		
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
	 		startY = y;
	 		final int k = AlertListView.this.pointToPosition(x, y);
	 		if( k == ListView.INVALID_POSITION ){
	 			break;
	 		}
	 		mDragStartPosition = k;
	 		mDragItemView = (AlertItemView)this.getChildAt(k - this.getFirstVisiblePosition());
	
	 		if(mDragItemView.getStatus() == AlertItemView.STATUS_NORMAL){
	 			this.mDragPointX = 9;
	 			this.mDragPointY = y - mDragItemView.getTop();
	 			this.mDragOffSetY = (int) (event.getRawY() - y);
				
	 			int height = getHeight();
	 			mUpperBound = Math.min(y - mTouchSlop, height / 3);
	 			mLowerBound = Math.max(y + mTouchSlop, height * 2 / 3);
	 			mDragCurrentPostion = k;
	 		}
	 		break;
	 	case MotionEvent.ACTION_UP:
	 		Log.i(TAG, "ACTION UP");
	 		y = (int) event.getY();
	 		if(this.status == SEQUENCE){
	 			//if(mDragItemView != null)
	 			//mDragItemView.setVisibility(View.VISIBLE);
				if(y > this.getHeight()){
	 				Log.d(TAG, "Up drag position:" + mDragStartPosition);
	 				this.mMainView.updateDeleteColor(false);
	 				this.mMainView.deleteItem(mDragItemView);
	 			}
	 			stopDrag();
 				this.mDragStartPosition = -1;
				this.mMainView.refreshAdapter();
	 			this.status = CREATE_REFRESH_DONE;
	 		} else if(this.status != NORMAL){
	 			if(status == CREATE_RELEASE_UP){
	 				status = CREATE_REFRESH_DONE;
	 				changeHeaderViewByStatus();
	 				createNewAlert();
	 			}else{
	 				status = CREATE_REFRESH_DONE;
	 				changeHeaderViewByStatus();
	 				this.headView.setPadding(0, -this.headContentHeight, 0, 0);
	 			}
	 			isRecored = false;
	 		}
	 		break;
	 	case MotionEvent.ACTION_CANCEL:
	 	case MotionEvent.ACTION_MOVE:
	 		Log.i(TAG, "ACTION MOVE");
	 		y = (int) event.getY();
	 		if(this.status == SEQUENCE){
	 			dragView(y);
	 			adjustScrollBounds(y);
	 			if(y > this.getHeight()){
	 				Log.d(TAG, "MOVE y: " + y + " height:" + this.getHeight());
	 				this.mMainView.updateDeleteColor(true);
	 			} else this.mMainView.updateDeleteColor(false);
	 			
	 		} else if(this.status != NORMAL){
	 			updateCreateStatus(y);
			
	 		}
                break;
	 	}
	 	return super.onTouchEvent(event);
	}
	
 	private void updateCreateStatus(int y) {
		// TODO Auto-generated method stub
 		this.headView.setRandBgColor();
		if(!isRecored && firstItemIndex == 0){
			isRecored = true;
			startY = y;
		}
		Log.d(TAG, "startY: " + startY + "  y: "+ y);
		if(this.status == CREATE_RELEASE_UP){
			setSelection(0);
			if(y-startY < 0){
				this.status = CREATE_REFRESH_DONE;
				changeHeaderViewByStatus();
			}
			else if(y-startY < headContentHeight){
				this.status = CREATE_PULL_DOWN;
				changeHeaderViewByStatus();
			} else if(y-startY > 4 * headContentHeight){
				return ;
			}
			paddingTop = (y - startY) - headContentHeight;
			headView.setPadding(0, paddingTop / this.RATIO, 
					0, 0);
		}
		else if(this.status == CREATE_PULL_DOWN){
			setSelection(0);
			if(y - startY >= headContentHeight){
				this.status = CREATE_RELEASE_UP;
				changeHeaderViewByStatus();
			}else if(y - startY <= 0){
				this.status = CREATE_REFRESH_DONE;
				changeHeaderViewByStatus();
			}
			paddingTop = (y - startY) - headContentHeight;
			headView.setPadding(0, paddingTop / this.RATIO, 
					0, 0);
		}
		else if(this.status == CREATE_REFRESH_DONE){
			if(y - startY > 0){
				this.status = CREATE_PULL_DOWN;
				changeHeaderViewByStatus();
			}
		}
	}

	private void changeHeaderViewByStatus() {
		// TODO Auto-generated method stub
		switch(status){
		case CREATE_PULL_DOWN:
			this.mTextView.setHint("pull down to create");
			this.RATIO = 1;
			break;
		case CREATE_RELEASE_UP:
			this.RATIO = 4;
			this.mTextView.setHint("release to create");
			break;
		case CREATE_REFRESH_DONE:
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
		this.mMainView.setDeleteVisible(true);
		this.mWindowParams = new WindowManager.LayoutParams();
		this.mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		
		this.mWindowParams.alpha = (float) 0.5;
		this.mWindowParams.x = mDragPointX;
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
	        mWindowParams.y = y - mDragPointY + mDragOffSetY;
	        mWindowManager.updateViewLayout(mDragView, mWindowParams);
		}
		int tempPosition = pointToPosition(0, y);
		if(tempPosition == INVALID_POSITION){
			return ;
		}
		if(mDragCurrentPostion != tempPosition){
			this.mMainView.exchangeAdapterItem(mDragCurrentPostion, tempPosition);
			mDragCurrentPostion = tempPosition;
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
		this.mMainView.setDeleteVisible(false);
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
    }
	
	public void setMainView(MainView mainView) {
		// TODO Auto-generated method stub
		this.mMainView = mainView;
	}
	
	public void onItemClicked(int position){
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
	
	class SimpleGesture extends SimpleOnGestureListener {
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "on single tap up");
            return false;
        }

        public void onLongPress(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on long press");
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on scroll x:" + distanceX + " y:" + distanceY);
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on Fling x:" + velocityX + " y:" + velocityY);
            return false;
        }

        public void onShowPress(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "show press");
        }

        public boolean onDown(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on down");
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on double tap");
            return false;
        }
        
        public boolean onDoubleTapEvent(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on double tap event");
            return false;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.d(TAG, "on single tap confirmed");
        	int position = AlertListView.this.pointToPosition((int)e.getX(), (int)e.getY());
        	if( position == ListView.INVALID_POSITION ){
        		return false;
        	}
    		AlertItemView itemView = (AlertItemView) AlertListView.this.getChildAt
    				(position - AlertListView.this.getFirstVisiblePosition());
    		AlertListView.this.mMainView.onItemClicked(position, itemView);
            return false;
        }
	}

}
