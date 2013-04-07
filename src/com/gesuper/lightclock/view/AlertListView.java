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
import android.util.AttributeSet;
import android.util.Log;
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
	public static final int NORMAL = 0;
	public static final int CREATE_PULL_DOWN = 1;
	public static final int CREATE_RELEASE_UP = 2;
	public static final int CREATE_REFRESH_DONE = 3;
	public static final int SEQUENCE = 4;
	
	private MainView mMainView;
	private int recored;
	private int firstItemIndex;
	private int startY;
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
		//mTextView = (TextView)headView.findViewById(R.id.tv_content);
		//this.measureView(headView);
		//this.headContentHeight = this.headView.getMeasuredHeight() + this.headView.getMenuHeight();
		//Log.d(TAG, "headContentHeight" + this.headContentHeight);
		//this.headView.setPadding(0, -this.headContentHeight, 0, 0);
		//this.headView.invalidate();
		//this.addHeaderView(headView);
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
			
		});
		
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
		dbHelper.close();
		this.mAdapter = new AlertListAdapter(this.getContext(), R.layout.activity_alert_item,
				mAlertListArray);
		Log.d(TAG, "set adapter");
		this.setAdapter(this.mAdapter);
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
	 		Log.i(TAG, "set rand bg color for head view");
	 		this.mHeadModel.setBgColorId(new Random().nextInt(BgColor.COLOR_COUNT) + 1);
	 		
	 		Log.d(TAG, "FirstVisiblePosition: " + this.getFirstVisiblePosition());
			if(this.getFirstVisiblePosition() == 0){
				recored += 1;
			}
	 		final int k = AlertListView.this.pointToPosition(x, y);
	 		
	 		this.mAdapter.insert(this.mHeadModel, 0);
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
	 		if(this.headView == null){
	 			this.headView = (AlertItemView) this.getChildAt(0);

	 			this.mTextView = (TextView)headView.findViewById(R.id.tv_content);
	 			this.headContentHeight = this.headView.getMHeight();

	 			Log.d(TAG, "headView: " + this.headContentHeight);
	 		}
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
	 			this.status = CREATE_REFRESH_DONE;
	 		} else if(this.status != NORMAL){
	 			if(status == CREATE_RELEASE_UP){
	 				status = CREATE_REFRESH_DONE;
	 				changeHeaderViewByStatus();
	 				this.createNewAlert();
	 			}else{
	 				status = CREATE_REFRESH_DONE;
	 				if(this.headView != null){ 
	 					changeHeaderViewByStatus();
	 					this.headView.hideFastMenu();
	 					this.headView.setPadding(0, 0, 0, 0);
	 				}
	 				this.mAdapter.remove(((AlertItemView) this.getChildAt(0)).getModel());
	 			}
	 		}
 			recored = 1;
 			this.headView = null;
	 		break;
	 	case MotionEvent.ACTION_CANCEL:
	 	case MotionEvent.ACTION_MOVE:
	 		y = (int) event.getY();
	 		if(this.headView == null){
	 			this.headView = (AlertItemView) this.getChildAt(0);
	 			this.mTextView = (TextView)headView.findViewById(R.id.tv_content);
	 			this.headContentHeight = this.headView.getMHeight() ;

	 			Log.d(TAG, "headView: " + this.headContentHeight);
	 		}
	 		if(this.getFirstVisiblePosition() > 1){
	 			this.recored = -1;
	 		}
	 		if(this.status == SEQUENCE){
	 			dragView(y);
	 			adjustScrollBounds(y);
	 			if(y > this.getHeight()){
	 				this.mMainView.updateDeleteColor(true);
	 			} else this.mMainView.updateDeleteColor(false);
	 			
	 		} else if(this.status != NORMAL && this.recored > 0){
	 			updateCreateStatus(y);
	 		}
            break;
	 	}
	 	return super.onTouchEvent(event);
	}
	
 	private void updateCreateStatus(int y) {
		// TODO Auto-generated method stub
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
