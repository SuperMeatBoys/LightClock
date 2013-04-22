package com.gesuper.lightclock.view;

import java.util.ArrayList;
import java.util.Random;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.AlertItemModel;
import com.gesuper.lightclock.model.AlertListAdapter;
import com.gesuper.lightclock.model.BgColor;
import com.gesuper.lightclock.model.DBHelperModel;
import com.gesuper.lightclock.model.MyGesture;
import com.gesuper.lightclock.model.MyGesture.OnGestureListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AlertListView extends ListView implements OnTouchListener, OnGestureListener{

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
	
	public static final int ACTION_SINGLE_CLICK = 1;
	public static final int ACTION_TOUCH_START = 2;
	public static final int ACTION_TOUCH_END = 3;
	public static final int ACTION_LONG_PRESS_START = 4;
	public static final int ACTION_LONG_PRESS_END = 5;
	public static final int ACTION_SCROLL = 6;
	
	private int RATIO = 2;
	
	private MyGesture mGesture;
	
	private MainView mMainView;
	private int mStartX;
	private int mStartY;
	private int mCurrentX;
	private int mCurrentY;
	private int mCurrentRawY;
	private int status;
	
	ArrayList<AlertItemModel> mAlertListArray;
	private AlertListAdapter mAdapter;
	private AlertItemView mHeadView;
	private int headContentHeight;
	private TextView mTextView;
	
	private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
	private Bitmap mDragBitmap;
	private ImageView mDragView;
	
    private AlertItemView mDragItemView;
	
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
	private boolean mLongPress;
	
	private int mScrollY;
	
	private Handler mHeadViewHandler = new Handler(){
		public void handleMessage(Message message){
			Log.d(TAG, "mHeadView Handler");
			mHeadView = (AlertItemView) AlertListView.this.getChildAt(0);

			mTextView = (TextView)mHeadView.findViewById(R.id.tv_content);
			headContentHeight = mHeadView.getMHeight() ;
		}
	};
	
	public AlertListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    // TODO Auto-generated constructor stub

	}
	
	public void initListView(){
		//hide scroll bar
		this.setVerticalScrollBarEnabled(true);
		this.mGesture = new MyGesture(this.getContext(), this, null);
		this.mGesture.setIsLongpressEnabled(true);
		this.mTouchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
		this.mHeight = this.getHeight();
		this.status = CREATE_REFRESH_DONE;
		this.mScroll = true;
		this.mDragItemView = null;
		this.mLongPress = false;
		this.mScroll = false;
		this.initAdapter();
		this.setOnTouchListener(this);
		this.setSmoothScrollbarEnabled(true);
		this.mHeadViewHandler.sendEmptyMessageDelayed(0, 100);
	}

	public void initAdapter() {
		// TODO Auto-generated method stub
		mAlertListArray  = new ArrayList<AlertItemModel>();
		AlertItemModel mAlertItem;
		DBHelperModel dbHelper = DBHelperModel.getInstance(this.getContext());
		Cursor cursor = dbHelper.query(AlertItemModel.mColumns, null, null, AlertItemModel.SEQUENCE + " asc");
		while(cursor.moveToNext()){
			mAlertItem = new AlertItemModel(this.getContext(), cursor);
			mAlertListArray.add(mAlertItem);
		}
		this.mAlertListArray.add(0, new AlertItemModel(this.getContext()));
		this.mAdapter = new AlertListAdapter(this.getContext(), R.layout.activity_alert_item,
				mAlertListArray);
		Log.d(TAG, "set adapter");
		this.setAdapter(this.mAdapter);
	}
	
	public void onItemEditEnd() {
		// TODO Auto-generated method stub
		this.scrollTo(0, mScrollY);
	}
 	private void updateHeadViewStatus() {
		// TODO Auto-generated method stub
 		Log.d(TAG, "updateCreateStatus");
 		int y = this.mCurrentY;
 		int paddingTop;
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
			mHeadView.setPadding(0, paddingTop / this.RATIO, 
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
			mHeadView.setPadding(0, paddingTop / this.RATIO, 
					0, 0);
		}
		else if(this.status == CREATE_REFRESH_DONE){
			if(y - mStartY > 50){
				this.status = CREATE_PULL_DOWN;
				changeHeaderViewByStatus();
			}
		}
	}
 	
	private void updateItemStatus(AlertItemView mItemView) {
		// TODO Auto-generated method stub
		this.setPadding(this.mCurrentX - this.mStartX, 0, 0, 0);
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
		this.mHeadView.getModel().setId((long) -2);
		this.mHeadView.setPadding(0, 0, 0, 0);
		this.mAlertListArray.add(0, new AlertItemModel(this.getContext()));
		this.mAdapter.notifyDataSetChanged();
		
		this.mHeadViewHandler.sendEmptyMessageDelayed(0, 200);
		
		this.mMainView.createHandler.sendEmptyMessageDelayed(1, 100);
		
	}
	
	public View getItemAt(int index){
		return this.getChildAt(index+1);
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
			this.setSelectionFromTop(mDragCurrentPostion, top + scrollY);
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
            this.smoothScrollBy(speed, 30);
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
	
	public void deleteItem(final AlertItemView mItemView){
		//this.mDelete.setTextColor(0xFFDC143C);
		new AlertDialog.Builder(this.getContext()).
				setTitle(R.string.item_menu_delete).
				setMessage("Do you really want to delete it?").
				setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mItemView.deleteItem();
						mAlertListArray.remove(mItemView.getModel());
						mAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton(R.string.dialog_cancel, null).show();
				
		
	}

	private void finishItem(AlertItemView mItemView) {
		// TODO Auto-generated method stub
		mAlertListArray.remove(mItemView.getModel());
		mAlertListArray.add(mItemView.getModel());
		mAdapter.notifyDataSetChanged();
	}
	
	public void exchangeAdapterItem(int x, int y){
		Log.d(TAG, "x:" + x + " y:" + y + "  count:" + this.getCount());
		AlertItemModel model = this.mAlertListArray.get(x);
		this.mAlertListArray.set(x, this.mAlertListArray.get(y));
		this.mAlertListArray.set(y, model);
		this.mAdapter.notifyDataSetChanged();
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
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		this.mStartX = this.mCurrentX;
		this.mStartY = this.mCurrentY;
		this.mHeadView.setBgColor(new Random().nextInt(BgColor.COLOR_COUNT) + 1);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		if(this.mLongPress){
			if(this.mCurrentY > this.getHeight()){
				this.mMainView.updateDeleteBtnColor(false);
				this.deleteItem(mDragItemView);
			}
			stopDrag();
			this.status = CREATE_REFRESH_DONE;
			this.mLongPress = false;
		} else if(this.mScroll){
			if(this.status == CREATE_RELEASE_UP){
				this.status = CREATE_REFRESH_DONE;
				this.changeHeaderViewByStatus();
				this.createNewAlert();
			}else{
				this.status = CREATE_REFRESH_DONE;
				if(this.mHeadView != null){ 
					changeHeaderViewByStatus();
					this.mHeadView.setPadding(0, - this.mHeadView.getMHeight(), 0, 0);
				}
			}
			this.mScroll = false;
		} else {
			int position = this.pointToPosition(0, this.mCurrentY);
			if(position == INVALID_POSITION){
				return false;
			}
			AlertItemView view = (AlertItemView) this.getChildAt(position - this.getFirstVisiblePosition());
			this.mScrollY = this.getScrollY();
			this.scrollTo(0, view.getTop());
			this.mMainView.onItemClicked(position, view);
		}
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		this.mScroll = true;
		if(this.mLongPress){
 			dragView();
 			adjustScrollBounds(this.mCurrentY);
 			if(this.mCurrentY > this.getHeight()){
 				this.mMainView.updateDeleteBtnColor(true);
 			} else this.mMainView.updateDeleteBtnColor(false);
 		}
 		else if(this.mCurrentY - this.mStartY > 50 && 
 				this.status != NORMAL && 
 				this.getFirstVisiblePosition() == 0){
			this.updateHeadViewStatus();
		} else if(this.mCurrentY - this.mStartY < 50 && 
				Math.abs(this.mCurrentX - this.mStartX) > 50){
			int position = this.pointToPosition(this.mCurrentX, this.mCurrentY);
			if(position == INVALID_POSITION){
				return false;
			}
			AlertItemView mItemView = (AlertItemView) this.getChildAt(position);
			this.updateItemStatus(mItemView);
		}
		return false;
	}

	@Override
	public boolean onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		this.status = SEQUENCE;
		this.mLongPress = true;
 		this.mStartY = this.mCurrentY;
 		//prepare for move item after long press
 		int position = AlertListView.this.pointToPosition(20, this.mCurrentY);
 		if(position == INVALID_POSITION){
			return false;
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
		
		Log.d(TAG, "drag item:" + this.mDragItemView.getContent());
		this.mDragItemView.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(this.mDragItemView.getDrawingCache(true));
		this.mDragItemView.setDrawingCacheEnabled(false);
		startDrag(bitmap, this.mDragPointY + this.mDragItemView.getTop());
		return false;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if(Math.abs(e1.getY() - e2.getY()) < 20){
			int position = this.pointToPosition((int) e2.getX(), (int) e2.getY());
			if(position == INVALID_POSITION){
				return false;
			}
			AlertItemView mItemView = (AlertItemView) this.getChildAt(position);
			
			if(e1.getX() - e2.getX() > 100 ){
				this.finishItem(mItemView);
			} else if(e2.getX() - e1.getX() > 100 ){
				this.deleteItem(mItemView);
			}
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		this.mCurrentX = (int) event.getX();
		this.mCurrentY = (int) event.getY();
	 	this.mCurrentRawY = (int) event.getRawY();
		return this.mGesture.onTouchEvent(event);
	}

}
