package com.gesuper.lightclock.view;

import java.util.ArrayList;
import com.gesuper.lightclock.R;
import com.gesuper.lightclock.activity.MainActivity;
import com.gesuper.lightclock.model.AlertItemModel;
import com.gesuper.lightclock.model.AlertListAdapter;
import com.gesuper.lightclock.model.DBHelperModel;
import com.gesuper.lightclock.view.AlertItemView.PopupListener;
import com.gesuper.lightclock.view.AlertItemView.ResizeListener;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;


public class MainView  extends LinearLayout {
	public static final String TAG = "MainView";
	
	private Context mContext;
	private ArrayList<AlertItemModel> mAlertListArray;
	private AlertListView mListView ;
	private Button mNewButton ;
	private AlertItemView mCurItemView;
	private PopupWindow mPopView;
	private AlertListAdapter mAdapter;
	
	private Handler createHandler = new Handler(){
		public void handleMessage(Message message){
			
			final int position = message.what;
			MainView.this.mCurItemView = 
					(AlertItemView) MainView.this.mListView.getChildAt(
							position - MainView.this.mListView.getFirstVisiblePosition()
					);
				
			TranslateAnimation translateAnimation = new TranslateAnimation(0.0F, 0.0F, -MainView.this.mCurItemView.getHeight(), 0.0F);
	        translateAnimation.setDuration(300L);
	        translateAnimation.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					MainView.this.mCurItemView.setVisibility(View.VISIBLE);
					MainView.this.createTopRectView(position);
					MainView.this.mCurItemView.startEdit(true);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
	        	
	        });
	        Log.d(TAG, "start create animation");
	        MainView.this.mCurItemView.startAnimation(translateAnimation);
		}
	};
	
	private Handler removeHandler = new Handler(){
		
		public void handleMessage(Message message){
			final int position = message.what;
			if(position < 0 || position > MainView.this.mListView.getCount() || MainView.this.mListView.getCount() == 0)
				return ;
			MainView.this.mCurItemView = 
					(AlertItemView) MainView.this.mListView.getChildAt(
							position - MainView.this.mListView.getFirstVisiblePosition()
					);
			if(MainView.this.mCurItemView != null){
				AnimationSet mAnimationSet = new AnimationSet(true);
				AlphaAnimation mAlphaAnimation = new AlphaAnimation(1.0F, 0.0F);
		        TranslateAnimation mTranslateAnimation = new TranslateAnimation(0.0F, 0.0F, 0.0F, -MainView.this.mCurItemView.getHeight());
		        mAnimationSet.addAnimation(mAlphaAnimation);
		        mAnimationSet.addAnimation(mTranslateAnimation);
		        mAnimationSet.setDuration(300L);
		        mAnimationSet.setAnimationListener(new AnimationListener(){

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						MainView.this.deleteItem(position);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
		        	
		        });
		        Log.d(TAG, "start create animation");
		        MainView.this.mCurItemView.startAnimation(mAnimationSet);
			}
		}
	};
	
	private Handler editHandler = new Handler(){
		
		public void handleMessage(Message message){
			MainView.this.createTopRectView(message.what);
			MainView.this.mCurItemView.startEdit(false);
		}
	};
	//private Handler scrollHandler = new Handler(){
	//	public void handleMessage(Message message){
	//		
	//		MainView.this.mListView.setSelection(message.what);
	//	}
	//};
	
	public MainView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
		inflate(context, R.layout.activity_main, this);
		this.mContext = context;
		this.mAlertListArray  = new ArrayList<AlertItemModel>();
		this.mPopView = null;
		initResource();
		
	}
	
	private void initResource() {
		// TODO Auto-generated method stub
		this.mListView = (AlertListView)findViewById(R.id.alert_list);
		this.mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(view instanceof AlertItemView){
					MainView.this.mCurItemView = ((AlertItemView) view);
					if(MainView.this.mCurItemView.getStatus() != AlertItemView.STATUS_NORMAL) return ;
					MainView.this.mCurItemView.showMenu();
					MainView.this.editHandler.sendEmptyMessageDelayed(position, 50);
				}
			}

		});
		
		AlertItemModel mAlertItem;
		Log.d(TAG, "init alert list");
		DBHelperModel dbHelper = new DBHelperModel(this.mContext);
		Cursor cursor = dbHelper.query(AlertItemModel.mColumns, null, null, AlertItemModel.SEQUENCE + " asc");
		while(cursor.moveToNext()){
			mAlertItem = new AlertItemModel(cursor);
			this.mAlertListArray.add(mAlertItem);
		}
		dbHelper.close();
		this.mAdapter = new AlertListAdapter(mContext, R.layout.activity_alert_item,
				this.mAlertListArray);
		Log.d(TAG, "set adapter");
		this.mListView.setAdapter(this.mAdapter);
		
		this.mNewButton = (Button)findViewById(R.id.new_alert);
		this.mNewButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainView.this.beforeAddItem();
				MainView.this.addNewItem();
			}
		});
		
	}

	public void beforeAddItem() {
		// TODO Auto-generated method stub
		if(this.mListView.getFirstVisiblePosition() > 0 ){
			this.mListView.setSelection(0);
		}
	}
	
	public boolean addNewItem(){
		Log.d(TAG, "new items created !");

		this.createHandler.sendEmptyMessageDelayed(0, 300);
		AlertItemModel mItemModel = new AlertItemModel();
		this.mAdapter.insert(mItemModel, 0);
		this.requestFocus();
		return true;
	}
	
	private void createTopRectView(final int position) {
		// TODO Auto-generated method stub
		int[] mPositionScreen = new int[2];
		int[] mPositionList = new int[2];
		int[] mPositionItem = new int[3];

		this.getLocationOnScreen(mPositionScreen);
		this.mListView.getLocationOnScreen(mPositionList);
		this.mCurItemView.getLocationOnScreen(mPositionItem);
		final Rect mRectTop = new Rect(mPositionScreen[0], mPositionList[1], 
				this.getWidth() + mPositionList[0], 
				this.getHeight() + mPositionList[1] - mPositionScreen[1]);
		final Rect mRectItem = new Rect(mPositionItem[0], 
					mPositionItem[1] - mPositionScreen[1], 
					this.mCurItemView.getWidth() + mPositionItem[0], 
					this.mCurItemView.getHeight() + mPositionItem[1] - mPositionScreen[1]);
		TopRectView mTopView= new TopRectView(this.mContext);
		
		Log.d(TAG, "height: " + this.mCurItemView.getHeight());
		mTopView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub 
				if(mRectItem.contains((int)event.getX(), (int)event.getY())){
					MainView.this.mListView.dispatchTouchEvent(event);
					Log.d(TAG, "click inside ");
				}
				else {
					MainView.this.setPopupDismiss();
					Log.d(TAG, "click outside");
				}
				return true;
			}
			
		});

		this.mPopView = new PopupWindow(this.mContext);
		this.mPopView.setInputMethodMode(View.FOCUSABLES_TOUCH_MODE);
		this.mPopView.setFocusable(false);
		this.mPopView.setContentView(mTopView);
		this.mPopView.setBackgroundDrawable(null);
		this.mPopView.setWidth(mRectTop.width());
		this.mPopView.setHeight(mRectTop.height());
		Log.d(TAG, "width: "+mRectTop.width() + ", " + mRectTop.height());
		this.mPopView.showAtLocation(this, Gravity.NO_GRAVITY, 0, mPositionScreen[1]);
		
		this.mPopView.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				if(MainView.this.mCurItemView != null){
					if(MainView.this.mCurItemView.getStatus() == AlertItemView.STATUS_CREATE 
							&& MainView.this.mCurItemView.getContent().equals("")){
						MainView.this.removeHandler.sendEmptyMessageDelayed(position, 200L);
					}
					else {
						MainView.this.mCurItemView.endEdit();
						
					}
				}
				AlertItemView mItemView;
				int count = MainView.this.mListView.getCount();
				for(int i=0;i<count;i++){
					mItemView = (AlertItemView) MainView.this.mListView.getChildAt(i);
					mItemView.setTranslucence(false);
				}
			}
			
		});
		MainView.this.mCurItemView.setResizeListener(new ResizeListener(){

			@Override
			public void resize() {
				// TODO Auto-generated method stub
				int[] mPositionScreen = new int[2];
		        MainView.this.getLocationOnScreen(mPositionScreen);
		        int[] mPositionItem = new int[2];
		        MainView.this.mCurItemView.getLocationOnScreen(mPositionItem);
		        mRectItem.left = mPositionItem[0];
		        mRectItem.top = (mPositionItem[1] - mPositionScreen[1]);
		        mRectItem.right = (MainView.this.mCurItemView.getWidth() + mPositionItem[0]);
		        mRectItem.bottom = (MainView.this.mCurItemView.getHeight() + mPositionItem[1] - mPositionScreen[1]);
			}
			
		});
		MainView.this.mCurItemView.setPopupListener(new PopupListener(){

			@Override
			public void popupDismiss() {
				// TODO Auto-generated method stub
				MainView.this.mPopView.dismiss();
				MainView.this.mPopView = null;
			}
			
		});
		
		int firstVisiblePosition =  this.mListView.getFirstVisiblePosition();
		AlertItemView mItemView;
		for(int i=0; i<this.mListView.getCount(); i++){
			if (i == position) continue;
			mItemView = (AlertItemView) this.mListView.getChildAt(i - firstVisiblePosition);
			mItemView.setTranslucence(true);
		}
	}
	
	public void deleteItem(int position){
		this.mAdapter.remove(this.mCurItemView.getModel());
	}
	
	public void saveSequence(){
		if ((this.mPopView != null) && (this.mPopView.isShowing()))
	    {
			this.mPopView.dismiss();
			this.mPopView = null;
	    }

	    if (this.mAdapter != null){
	    	for (int i = 0; i < this.mAdapter.getCount(); i++)
	    	{
	    		AlertItemView mItemView = (AlertItemView)this.mListView.getChildAt(i);
	    		if (mItemView.getSequence() != i)
	    		{
	    			mItemView.setSequence(i);
	    			mItemView.updateSequence();
	    		}
	    	}
	    }
	}

	public void setPopupDismiss() {
		// TODO Auto-generated method stub
		if(this.mPopView != null){
			this.mPopView.dismiss();
			this.mPopView = null;
		}
	}

	public void getTime() {
		// TODO Auto-generated method stub
		((MainActivity) this.mContext).getTime();
	}
}
