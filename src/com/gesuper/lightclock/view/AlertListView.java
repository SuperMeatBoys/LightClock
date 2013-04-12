package com.gesuper.lightclock.view;

import java.util.ArrayList;
import java.util.Random;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.AlertItemModel;
import com.gesuper.lightclock.model.AlertListAdapter;
import com.gesuper.lightclock.model.BgColor;
import com.gesuper.lightclock.model.DBHelperModel;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AlertListView extends ListView{

	public static final String TAG = "AlertListView";
	public static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	public static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	public static final int NORMAL = 0;
	public static final int CREATE_PULL_DOWN = 1;
	public static final int CREATE_RELEASE_UP = 2;
	public static final int CREATE_REFRESH_DONE = 3;
	public static final int SEQUENCE = 4;
	
	public static final int ACTION_DOWN = 0;
	public static final int ACTION_SINGLE_CLICK = 1;
	public static final int ACTION_TAP_START = 2;
	public static final int ACTION_TAP_END = 3;
	public static final int ACTION_LONG_PRESS_START = 4;
	public static final int ACTION_LONG_PRESS_END = 5;
	public static final int ACTION_SCROLL = 6;
	
	private MainView mMainView;
	private int recored;
	private int mStartY;
	private int mCurrentRawY;
	private int status;
	private int paddingTop;
	
	private int RATIO = 2;

	private AlertListAdapter mAdapter;
	private AlertItemView headView;
	private AlertItemModel mHeadModel;
	private int headContentHeight;
	private TextView mTextView;
	
	private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
	private Bitmap mDragBitmap;
	private ImageView mDragView;
	//开始位置
	private int mDragStartPosition;
    private AlertItemView mDragItemView;
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
	private boolean mStillDown;
	private boolean mLongPress;
	
	private Handler mActionHandler = new Handler(){
		public void handleMessage(Message message){
			switch(message.what){
			case ACTION_DOWN:
				onDown();
				break;
			case ACTION_SINGLE_CLICK:
				onSingleClick();
				break;
			case ACTION_LONG_PRESS_START:
				if(mStillDown){
					mLongPress = true;
					onLongPressStart();
				}
				break;
			case ACTION_SCROLL:
				onScroll();
				break;
			}
		}
	};
	private int mCurrentY;
	private boolean mScroll;
	
	public AlertListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    // TODO Auto-generated constructor stub
	    //
	    //this.initResource();
	}
	
	public void initListView(){
		//hide scroll bar
		this.setVerticalScrollBarEnabled(true);
		this.mTouchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();  
		this.recored = 1;
		this.mHeight = this.getHeight();
		this.status = CREATE_REFRESH_DONE;
		this.mDragItemView = null;
		this.mDragStartPosition = -1;
		this.setDivider(null);
		this.mHeadModel = new AlertItemModel(this.getContext());
		this.mHeadModel.setId((long) -1);

		this.initAdapter();
		
		this.headView = null;
		/*
		this.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.i(TAG, "on item long click " + position);
				status = SEQUENCE;
				View v = AlertListView.this.getChildAt(position);
				View itemContent = v.findViewById(R.id.alert_content);
				itemContent.setDrawingCacheEnabled(true);
				Bitmap bitmap = Bitmap.createBitmap(itemContent.getDrawingCache());
				startDrag(bitmap, mDragPointY + v.getTop());
				//view.setVisibility(View.INVISIBLE);
				return false;
			}
			
		});*/
		
	}

	public void initAdapter() {
		// TODO Auto-generated method stub
		ArrayList<AlertItemModel> mAlertListArray  = new ArrayList<AlertItemModel>();
		AlertItemModel mAlertItem;
		//mAlertItem = new AlertItemModel();
		//mAlertItem.setContent("enough ? are you kidding me?");
		Log.d(TAG, "init alert list");
		DBHelperModel dbHelper = DBHelperModel.getInstance(this.getContext());
		//dbHelper.insert(mAlertItem.formatContentValuesWithoutId());
		Cursor cursor = dbHelper.query(AlertItemModel.mColumns, null, null, AlertItemModel.SEQUENCE + " asc");
		while(cursor.moveToNext()){
			mAlertItem = new AlertItemModel(this.getContext(), cursor);
			mAlertListArray.add(mAlertItem);
		}
		this.mAdapter = new AlertListAdapter(this.getContext(), R.layout.activity_alert_item,
				mAlertListArray);
		Log.d(TAG, "set adapter");
		this.setAdapter(this.mAdapter);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	 	// TODO Auto-generated method stub
	 	this.mCurrentY = (int) event.getY();
	 	this.mCurrentRawY = (int) event.getRawY();
	 	switch(event.getAction()){
	 	case MotionEvent.ACTION_DOWN:
	 		this.mStillDown = true;
	 		this.mLongPress = false;
	 		this.mScroll = false;
	 		this.mActionHandler.sendEmptyMessage(ACTION_DOWN);
	 		this.mActionHandler.sendEmptyMessageDelayed(
	 				ACTION_LONG_PRESS_START, TAP_TIMEOUT + LONG_PRESS_TIMEOUT);
	 		break;
	 	case MotionEvent.ACTION_UP:
	 		this.mStillDown = false;
	 		if(this.mLongPress){
	 			this.mActionHandler.sendEmptyMessage(ACTION_LONG_PRESS_END);
	 		} else if(this.mScroll){
	 			this.mActionHandler.sendEmptyMessage(ACTION_SINGLE_CLICK);
	 		}else {
	 			this.mActionHandler.sendEmptyMessage(ACTION_SINGLE_CLICK);
	 		}
	 		this.mLongPress = false;
	 		break;
	 	case MotionEvent.ACTION_MOVE:
	 		this.mStillDown = true;
			//prepare for create new item
	 		if(!this.mLongPress && !this.mScroll){
	 	 		this.mHeadModel.setBgColorId(new Random().nextInt(BgColor.COLOR_COUNT) + 1);
	 			this.mAdapter.insert(this.mHeadModel, 0);
	 			
		 		this.mScroll = true;
	 		}
	 		if(!this.mLongPress){
	 			this.mActionHandler.removeMessages(ACTION_LONG_PRESS_START);
	 			this.mActionHandler.sendEmptyMessage(ACTION_TAP_START);
	 		}
	 		this.mActionHandler.sendEmptyMessage(ACTION_SCROLL);
	 		break;
	 	}
	 	return super.onTouchEvent(event);
	}
	
	private void onDown(){
 		this.mStartY = this.mCurrentY;
 		//prepare for move item after long press
 		int position = AlertListView.this.pointToPosition(0, this.mCurrentY);
 		if(position == INVALID_POSITION){
			return ;
		}
 		this.mDragStartPosition = position;
 		this.mDragItemView = (AlertItemView)this.getChildAt(position - this.getFirstVisiblePosition());

 		if(mDragItemView.getStatus() == AlertItemView.STATUS_NORMAL){
 			this.mDragPointX = 9;
 			this.mDragPointY = this.mCurrentY - mDragItemView.getTop();
 			this.mDragOffSetY = (int) (this.mCurrentRawY - this.mCurrentY);
			
 			int height = getHeight();
 			mUpperBound = Math.min(this.mCurrentY - mTouchSlop, height / 3);
 			mLowerBound = Math.max(this.mCurrentY + mTouchSlop, height * 2 / 3);
 			mDragCurrentPostion = position;
 		}
	}
	
	//handle the single click event
	private void onSingleClick(){
		int position = this.pointToPosition(0, this.mCurrentY);
		if(position == INVALID_POSITION){
			return ;
		}
		AlertItemView view = (AlertItemView) this.getChildAt(position - this.getFirstVisiblePosition());
		this.mMainView.onItemClicked(position, view);
	}
	
	private void onLongPressStart(){
		this.mLongPress = true;
		this.status = SEQUENCE;
		View itemContent = this.mDragItemView.findViewById(R.id.alert_content);
		itemContent.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(itemContent.getDrawingCache());
		startDrag(bitmap, this.mDragPointY + this.mDragItemView.getTop());
	}
	
	private void onLongPressEnd(){
		if(this.mCurrentY > this.getHeight()){
			this.mMainView.updateDeleteBtnColor(false);
			this.mMainView.deleteItem(mDragItemView);
		}
		stopDrag();
		this.mDragStartPosition = -1;
		this.status = CREATE_REFRESH_DONE;
	}
	
	private void onScroll(){
		
	}
	
 	private void updateCreateStatus(int y) {
		// TODO Auto-generated method stub
		if(this.status == CREATE_RELEASE_UP){
			setSelection(0);
			if(y-mStartY < 0){
				this.status = CREATE_REFRESH_DONE;
				changeHeaderViewByStatus();
			}
			else if(y-mStartY < headContentHeight){
				this.status = CREATE_PULL_DOWN;
				changeHeaderViewByStatus();
			} else if(y-mStartY > 4 * headContentHeight){
				return ;
			}
			paddingTop = (y - mStartY) - headContentHeight;
			headView.setPadding(0, paddingTop / this.RATIO, 
					0, 0);
		}
		else if(this.status == CREATE_PULL_DOWN){
			setSelection(0);
			if(y - mStartY >= headContentHeight){
				this.status = CREATE_RELEASE_UP;
				changeHeaderViewByStatus();
			}else if(y - mStartY <= 0){
				this.status = CREATE_REFRESH_DONE;
				changeHeaderViewByStatus();
			}
			paddingTop = (y - mStartY) - headContentHeight;
			headView.setPadding(0, paddingTop / this.RATIO, 
					0, 0);
		}
		else if(this.status == CREATE_REFRESH_DONE){
			if(y - mStartY > 0){
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
			this.RATIO = 2;
			break;
		case CREATE_RELEASE_UP:
			this.RATIO = 4;
			this.mTextView.setHint("release to create");
			break;
		case CREATE_REFRESH_DONE:
			this.RATIO = 2;
			this.mTextView.setHint("click to edit");
			break;
		}
	}
	
	private void createNewAlert() {
		// TODO Auto-generated method stub
		this.headView.setPadding(0, 0, 0, 0);
		this.mMainView.createHandler.sendEmptyMessageDelayed(0, 300);
		this.mHeadModel = new AlertItemModel(this.getContext());
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
			this.exchangeAdapterItem(mDragCurrentPostion, tempPosition);
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
            mWindowManager.removeView(mDragView);
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
	
	public boolean isRecored(){
		if(this.status == CREATE_REFRESH_DONE)
			return false;
		return true;
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
	
	public void exchangeAdapterItem(int x, int y){
		Log.i(TAG, "x:" + x);
		AlertItemModel modelX = this.mAdapter.getItem(x-1);
		this.mAdapter.remove(modelX);
		this.mAdapter.insert(modelX, y-1);
	}
	
	public void saveSequence(){

	    if (this.mAdapter != null){
	    	for (int i = 0; i < this.mAdapter.getCount(); i++)
	    	{
	    		AlertItemView mItemView = (AlertItemView)this.getItemAt(i);
	    		if (mItemView != null && mItemView.getSequence() != i)
	    		{
	    			mItemView.setSequence(i);
	    			mItemView.updateSequence();
	    		}
	    	}
	    }
	}

	public void removeModel(AlertItemModel model) {
		// TODO Auto-generated method stub
		this.mAdapter.remove(model);
	}
}
