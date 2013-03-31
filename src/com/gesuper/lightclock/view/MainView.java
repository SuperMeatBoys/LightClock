package com.gesuper.lightclock.view;

import java.util.ArrayList;
import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.AlertItemModel;
import com.gesuper.lightclock.model.AlertListAdapter;
import com.gesuper.lightclock.model.DBHelperModel;
import com.gesuper.lightclock.view.AlertItemView.PopupListener;
import com.gesuper.lightclock.view.AlertItemView.ResizeListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;


public class MainView  extends LinearLayout {
	public static final String TAG = "MainView";
	
	private Context mContext;
	private ArrayList<AlertItemModel> mAlertListArray;
	private AlertListView mListView ;
	private AlertItemView mCurItemView;
	private PopupWindow mPopView;
	private AlertListAdapter mAdapter;
	private TextView mDelete;
	
	private Handler createHandler = new Handler(){
		public void handleMessage(Message message){
			
			final int position = message.what;
			MainView.this.mCurItemView = 
					(AlertItemView) MainView.this.mListView.getItemAt(
							position - MainView.this.mListView.getFirstVisiblePosition()
					);
			
			MainView.this.mCurItemView.setVisibility(View.VISIBLE);
			MainView.this.createTopRectView(position);
			MainView.this.mCurItemView.startEdit(true);
		}
	};
	
	private Handler removeHandler = new Handler(){
		
		public void handleMessage(Message message){
			MainView.this.mCurItemView.hideFastMenu();
			MainView.this.mCurItemView.setStatusNormal();
			MainView.this.mCurItemView.setPadding(0, 0, 0, 0);
			MainView.this.mAdapter.remove(MainView.this.mCurItemView.getModel());
		}
	};
	
	private Handler removeAnimationHandler = new Handler(){
		
		public void handleMessage(Message message){
			MainView.this.mCurItemView.setPadding(0, message.what, 0, 0);
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
		this.mListView.setMainView(this);
		this.mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(view instanceof AlertItemView){
					MainView.this.onItemClicked(position, (AlertItemView) view);
				}
			}

		});
		AlertItemModel mAlertItem;
		//mAlertItem = new AlertItemModel();
		//mAlertItem.setContent("enough ? are you kidding me?");
		this.mDelete = (TextView)findViewById(R.id.alert_delete);
		Log.d(TAG, "init alert list");
		DBHelperModel dbHelper = new DBHelperModel(this.mContext);
		//dbHelper.insert(mAlertItem.formatContentValuesWithoutId());
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
		
	}

	public void onItemClicked(int position, AlertItemView view){
		MainView.this.mCurItemView = view;
		if(MainView.this.mListView.isRecored()) return;
		if(MainView.this.mCurItemView.getStatus() != AlertItemView.STATUS_NORMAL) return ;
		MainView.this.mCurItemView.showMenu();
		MainView.this.editHandler.sendEmptyMessageDelayed(position, 50);
	}
	
	public void beforeAddItem() {
		// TODO Auto-generated method stub
		if(this.mListView.getFirstVisiblePosition() > 0 ){
			this.mListView.setSelection(0);
		}
	}
	
	public boolean addNewItem(int bgColorId){
		AlertItemModel mItemModel = new AlertItemModel();
		mItemModel.setBgColorId(bgColorId);
		this.mAdapter.insert(mItemModel, 0);
		this.createHandler.sendEmptyMessageDelayed(0, 300);
		this.requestFocus();
		return true;
	}
	
	private void createTopRectView(final int position) {
		// TODO Auto-generated method stub
		int[] mPositionScreen = new int[2];
		int[] mPositionList = new int[2];
		int[] mPositionItem = new int[2];

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
		this.mPopView.showAtLocation(this, Gravity.NO_GRAVITY, 0, mPositionScreen[1]);
		
		this.mPopView.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				if(MainView.this.mCurItemView != null){
					if(MainView.this.mCurItemView.getStatus() == AlertItemView.STATUS_CREATE 
							&& MainView.this.mCurItemView.getContent().equals("")){
						InputMethodManager inputManager =
				                (InputMethodManager)MainView.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(
								MainView.this.mListView.getWindowToken(), 0);
						new Thread(){
				        	public void run(){
				        		int paddingTop = 16; 
				        		while(paddingTop < MainView.this.mCurItemView.getHeight()){
				        			MainView.this.removeAnimationHandler.sendEmptyMessageDelayed(-paddingTop, paddingTop);
				        			paddingTop += 16;
				        		}
				        		MainView.this.removeAnimationHandler.sendEmptyMessageDelayed(-MainView.this.mCurItemView.getHeight(), MainView.this.mCurItemView.getHeight());
				        		MainView.this.removeHandler.sendEmptyMessageDelayed(0, 200L);
				        	}
				        }.start();
					}
					else {
						MainView.this.mCurItemView.endEdit();
						
					}
				}
				AlertItemView mItemView;
				int count = MainView.this.mListView.getCount();
				for(int i=0;i<count;i++){
					mItemView = (AlertItemView) MainView.this.mListView.getItemAt(i);
					if(mItemView != null)
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
			mItemView = (AlertItemView) this.mListView.getItemAt(i - firstVisiblePosition);

			if(mItemView != null){
				if(mItemView.equals(this.mCurItemView)) continue;
				mItemView.setTranslucence(true);
			}
		}
	}
	
	public void setDeleteVisible(boolean f){
		if(f)
			this.mDelete.setVisibility(View.VISIBLE);
		else this.mDelete.setVisibility(View.GONE);
	}
	
	public void updateDeleteColor(boolean f){
		if(f)
			this.mDelete.setTextColor(0xFFDC143C);
		else this.mDelete.setTextColor(0xFF000000);
	}
	
	public void deleteItem(final AlertItemView itemView){
		//this.mDelete.setTextColor(0xFFDC143C);
		new AlertDialog.Builder(this.getContext()).
				setTitle(R.string.item_menu_delete).
				setMessage("Do you really want to delete it?").
				setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						itemView.deleteItem();
						mAdapter.remove(itemView.getModel());
					}
				}).setNegativeButton(R.string.dialog_cancel, null).show();
				
		
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
	    		AlertItemView mItemView = (AlertItemView)this.mListView.getItemAt(i);
	    		if (mItemView != null && mItemView.getSequence() != i)
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
	
	public void exchangeAdapterItem(int x, int y){
		Log.i(TAG, "x:" + x);
		AlertItemModel modelX = this.mAdapter.getItem(x-1);
		this.mAdapter.remove(modelX);
		this.mAdapter.insert(modelX, y-1);
	}

	public void refreshAdapter() {
		// TODO Auto-generated method stub
		this.saveSequence();
		this.mAdapter.notifyDataSetChanged();
	}
}
