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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AlertListView extends ListView{

	public static final String TAG = "AlertListView";
	@SuppressWarnings("deprecation")
	public static final int TOUCH_SLOP = ViewConfiguration.getTouchSlop();
	public static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	public static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	public static final int NORMAL = 0;
	public static final int CREATE_PULL_DOWN = 1;
	public static final int CREATE_RELEASE_UP = 2;
	public static final int CREATE_REFRESH_DONE = 3;
	public static final int SEQUENCE = 4;
	
	public static final int ACTION_DOWN = 0;
	public static final int ACTION_SINGLE_CLICK = 1;
	public static final int ACTION_TOUCH_START = 2;
	public static final int ACTION_TOUCH_END = 3;
	public static final int ACTION_LONG_PRESS_START = 4;
	public static final int ACTION_LONG_PRESS_END = 5;
	public static final int ACTION_SCROLL = 6;
	
	private MainView mMainView;
	private int recored;
	private int mStartY;
	private int mCurrentY;
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
	private boolean mScroll;
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
			case ACTION_TOUCH_START:
				onTouchStart();
				break;
			case ACTION_TOUCH_END:
				if(mScroll == true){
					onTouchEnd();
				}
				break;
			case ACTION_LONG_PRESS_START:
				if(mStillDown){
					mLongPress = true;
					onLongPressStart();
				}
				break;
			case ACTION_LONG_PRESS_END:
				mLongPress = false;
				onLongPressEnd();
				break;
			case ACTION_SCROLL:
				onScroll();
				break;
			}
		}

	};
	
	public AlertListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    // TODO Auto-generated constructor stub

	}
	
	public void initListView(){
		//hide scroll bar
		this.setVerticalScrollBarEnabled(true);
		this.mTouchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();  
		this.recored = 1;
		this.headView = null;
		this.mHeight = this.getHeight();
		this.status = CREATE_REFRESH_DONE;
		this.mScroll = true;
		this.mDragItemView = null;
		this.setDivider(null);
		this.mHeadModel = new AlertItemModel(this.getContext());
		this.mScroll = true;
		this.initAdapter();
		
	}

	public void initAdapter() {
		// TODO Auto-generated method stub
		ArrayList<AlertItemModel> mAlertListArray  = new ArrayList<AlertItemModel>();
		AlertItemModel mAlertItem;
		DBHelperModel dbHelper = DBHelperModel.getInstance(this.getContext());
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
	 				ACTION_LONG_PRESS_START, LONG_PRESS_TIMEOUT);
	 		break;
	 	case MotionEvent.ACTION_UP:
	 		this.mStillDown = false;
	 		if(this.mLongPress){
	 			this.mActionHandler.sendEmptyMessage(ACTION_LONG_PRESS_END);
	 		} else if(this.mScroll){
	 			this.mActionHandler.removeMessages(ACTION_LONG_PRESS_START);
	 			this.mActionHandler.sendEmptyMessage(ACTION_TOUCH_END);
	 		}else {
	 			this.mActionHandler.removeMessages(ACTION_LONG_PRESS_START);
	 			this.onTouchEnd();
	 			this.mActionHandler.sendEmptyMessageDelayed(ACTION_SINGLE_CLICK, 10);
	 		}
	 		this.mLongPress = false;
	 		break;
	 	case MotionEvent.ACTION_MOVE:
	 		int deltaY = this.mCurrentY - this.mStartY;
	 		if(deltaY * deltaY < TOUCH_SLOP)
	 			break;
	 		if(!this.mStillDown){
	 			this.mStillDown = true;
	 			this.mActionHandler.sendEmptyMessage(ACTION_DOWN);
	 		}
	 		if(!this.mLongPress && !this.mScroll){
	 			this.mActionHandler.removeMessages(ACTION_LONG_PRESS_START);
	 			this.mActionHandler.sendEmptyMessage(ACTION_TOUCH_START);
		 		this.mScroll = true;
	 		}
	 		this.mActionHandler.sendEmptyMessage(ACTION_SCROLL);
	 		break;
	 	}
	 	return super.onTouchEvent(event);
	}
	
	private void onDown(){
		// TODO Auto-generated method stub
		Log.d(TAG, "onDown");
		this.mStartY = this.mCurrentY;
		this.mHeadModel.setBgColorId(new Random().nextInt(BgColor.COLOR_COUNT) + 1);
		this.mAdapter.insert(this.mHeadModel, 0);
		this.mActionHandler.sendEmptyMessageDelayed(ACTION_TOUCH_START, 1);
	}
	
	//handle the single click event
	private void onSingleClick(){
		// TODO Auto-generated method stub
		Log.d(TAG, "onSingleClick");
		
		int position = this.pointToPosition(0, this.mCurrentY);
		if(position == INVALID_POSITION){
			return ;
		}
		AlertItemView view = (AlertItemView) this.getChildAt(position - this.getFirstVisiblePosition());
		this.mMainView.onItemClicked(position, view);
	}

	private void onTouchStart() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onTouchStart");
		if(this.headView != null){
			return ;
		}
		this.headView = (AlertItemView) this.getChildAt(0);
		this.mTextView = (TextView)headView.findViewById(R.id.tv_content);
		this.headContentHeight = this.headView.getMHeight() ;
	}
	
	private void onTouchEnd() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onTouchEnd");
		if(this.status == CREATE_RELEASE_UP){
			this.status = CREATE_REFRESH_DONE;
			this.changeHeaderViewByStatus();
			this.createNewAlert();
		}else{
			this.status = CREATE_REFRESH_DONE;
			if(this.headView != null){ 
				changeHeaderViewByStatus();
				this.headView.hideFastMenu();
				this.headView.setPadding(0, 0, 0, 0);
			}
			this.mAdapter.remove(((AlertItemView) this.getChildAt(0)).getModel());
			this.mHeadModel = new AlertItemModel(this.getContext());
		}
		this.headView = null;
	}
	
	private void onLongPressStart(){
		// TODO Auto-generated method stub
		Log.d(TAG, "onLongPressStart");
		if(this.headView != null){
			this.onTouchEnd();
			this.mActionHandler.sendEmptyMessageDelayed(ACTION_LONG_PRESS_START, 10);
			return ;
		}
		this.status = SEQUENCE;
		
 		this.mStartY = this.mCurrentY;
 		//prepare for move item after long press
 		int position = AlertListView.this.pointToPosition(20, this.mCurrentY);
 		if(position == INVALID_POSITION){
			return ;
		}
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
		
		View itemContent = this.mDragItemView.findViewById(R.id.alert_content);
		Log.d(TAG, "drag item:" + this.mDragItemView.getContent());
		itemContent.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(itemContent.getDrawingCache());
		startDrag(bitmap, this.mDragPointY + this.mDragItemView.getTop());
	}
	
	private void onLongPressEnd(){
		// TODO Auto-generated method stub
		Log.d(TAG, "onLongPressEnd");
		if(this.mCurrentY > this.getHeight()){
			this.mMainView.updateDeleteBtnColor(false);
			this.mMainView.deleteItem(mDragItemView);
		}
		stopDrag();
		this.status = CREATE_REFRESH_DONE;
	}
	
	private void onScroll(){
		// TODO Auto-generated method stub

 		if(this.mLongPress){
 			dragView();
 			adjustScrollBounds(this.mCurrentY);
 			if(this.mCurrentY > this.getHeight()){
 				this.mMainView.updateDeleteBtnColor(true);
 			} else this.mMainView.updateDeleteBtnColor(false);
 		}
 		else if(this.status != NORMAL && this.getFirstVisiblePosition() == 0){
			this.updateCreateStatus();
		}
	}
	
 	private void updateCreateStatus() {
		// TODO Auto-generated method stub
 		int y = this.mCurrentY;
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
	

	private void dragView() {
		if(mDragView != null){
	        mWindowParams.y = this.mCurrentY - mDragPointY + mDragOffSetY;
	        mWindowManager.updateViewLayout(mDragView, mWindowParams);
		}
		int tempPosition = this.pointToPosition(20, this.mCurrentY);
		if(tempPosition == INVALID_POSITION){
			return ;
		}
		if(mDragCurrentPostion != tempPosition){
			this.exchangeAdapterItem(mDragCurrentPostion, tempPosition);
			mDragCurrentPostion = tempPosition;
		}
		
		//滚动
		int scrollY = 0;
		if(this.mCurrentY < mUpperBound){
			scrollY = 8;
		}else if(this.mCurrentY > mLowerBound){
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
		Log.d(TAG, "x:" + x + " y:" + y + "  count:" + this.getCount());
		AlertItemModel modelX = this.mAdapter.getItem(x);
		this.mAdapter.remove(modelX);
		this.mAdapter.insert(modelX, y);
	}
	
	public void saveSequence(){

	    if (this.mAdapter != null){
	    	for (int i = 0; i < this.mAdapter.getCount(); i++)
	    	{
	    		AlertItemView mItemView = (AlertItemView)this.getChildAt(i);
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
