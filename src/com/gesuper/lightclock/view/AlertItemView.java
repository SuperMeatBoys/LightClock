package com.gesuper.lightclock.view;

import java.util.Date;

import com.gesuper.lightclock.activity.*;
import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.*;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class AlertItemView extends LinearLayout {
	public static final String TAG = "AlertItemView";
	
	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_EDIT = 1;
	public static final int STATUS_DOWN = 2;
	public static final int STATUS_MOVE = 3;
	public static final int STATUS_CREATE = 4;
	
	private MainView mMainView;
	private AlertItemModel mItemModel;
	private int status;
	
	private RelativeLayout mContent;
	private EditText mEditText;
	private TextView mTextView;
	
	private LinearLayout mMenu;
	private TextView mModifyTime;
	private TextView mClock;
	private TextView mShare;
	private TextView mColor;
	
	private LinearLayout mColorList;
	private View mColor1;
	private View mColor2;
	private View mColor3;
	private View mColor4;
	private TextView mColorOk;
	
	private InputMethodManager inputManager;
	private ResizeListener resizeListener;
	
	private OnTouchListener itemTouchListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			v.setBackgroundColor(Color.GREEN);
			Log.d(TAG, "touch");
			return false;
		}
		
	};
	
	private OnClickListener changeColorListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.color_1:
				AlertItemView.this.mContent.setBackgroundColor(BgColor.COLOR_1);
				AlertItemView.this.mItemModel.setBgColorId(1);
				break;
			case R.id.color_2:
				AlertItemView.this.mContent.setBackgroundColor(BgColor.COLOR_2);
				AlertItemView.this.mItemModel.setBgColorId(2);
				break;
			case R.id.color_3:
				AlertItemView.this.mContent.setBackgroundColor(BgColor.COLOR_3);
				AlertItemView.this.mItemModel.setBgColorId(3);
				break;
			case R.id.color_4:
				AlertItemView.this.mContent.setBackgroundColor(BgColor.COLOR_4);
				AlertItemView.this.mItemModel.setBgColorId(4);
				break;
			}
		}
		
	};
	
	public AlertItemView(Context context){
		super(context);
		
		this.mMainView = (MainView) ((MainActivity)context).getView();
	    inflate(context, R.layout.activity_alert_item, this);
	    
		this.inputManager =
                (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		initResource();
	}
	
	public AlertItemView(Context context, AlertItemModel itemModel){
		this(context);
		this.setModel(itemModel);
	}
	
	private void initResource() {
		// TODO Auto-generated method stub
		this.mContent = (RelativeLayout)findViewById(R.id.alert_content);
		this.mEditText = (EditText)findViewById(R.id.ed_content);
		this.mTextView = (TextView)findViewById(R.id.tv_content);
		this.status = STATUS_NORMAL;
		
		this.mEditText.addTextChangedListener(new TextWatcher(){
			private String lastStr;
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (AlertItemView.this.mEditText.getLineCount() <= 3)
		        {
					this.lastStr = s.toString();
			        AlertItemView.this.mItemModel.setContent(this.lastStr);
			        AlertItemView.this.adjustHeight();
		        }
				else {
					AlertItemView.this.mEditText.setText(this.lastStr);
					AlertItemView.this.mEditText.setSelection(this.lastStr.length());
				}
			}
			
		});
		
		this.mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				Log.d(TAG, "actionId: " + actionId);
				switch(actionId){  
		        case EditorInfo.IME_NULL:  
		            System.out.println("null for default_content: " + v.getText() );  
		            break;  
		        case EditorInfo.IME_ACTION_SEND:  
		            System.out.println("action send for email_content: "  + v.getText());  
		            break;  
		        case EditorInfo.IME_ACTION_DONE:  
		            System.out.println("action done for number_content: "  + v.getText());  
		            break;  
		        }  
				return true;
			}
			
		});
		
		this.mMenu = (LinearLayout)findViewById(R.id.item_menu);
		this.mModifyTime = (TextView)findViewById(R.id.item_modify_time);
		this.mClock = (TextView)findViewById(R.id.item_clock);
		this.mShare = (TextView)findViewById(R.id.item_share);
		this.mColor = (TextView)findViewById(R.id.item_color);
		
		this.mClock.setOnTouchListener(itemTouchListener);
		this.mClock.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertItemView.this.AddClock();
			}
			
		});
		
		this.mShare.setOnTouchListener(itemTouchListener);
		this.mShare.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v){
				// TODO Auto-generated method stub
				AlertItemView.this.shareContent();
			}
			
		});
		
		this.mColor.setOnTouchListener(itemTouchListener);
		this.mColor.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertItemView.this.setBgColor();
			}
			
		});
		
		this.mColorList = (LinearLayout)findViewById(R.id.item_color_list);
		this.mColor1 = (View)findViewById(R.id.color_1);
		this.mColor1.setOnClickListener(changeColorListener);
		this.mColor1.setBackgroundColor(BgColor.COLOR_1);
		this.mColor2 = (View)findViewById(R.id.color_2);
		this.mColor2.setOnClickListener(changeColorListener);
		this.mColor2.setBackgroundColor(BgColor.COLOR_2);
		this.mColor3 = (View)findViewById(R.id.color_3);
		this.mColor3.setOnClickListener(changeColorListener);
		this.mColor3.setBackgroundColor(BgColor.COLOR_3);
		this.mColor4 = (View)findViewById(R.id.color_4);
		this.mColor4.setOnClickListener(changeColorListener);
		this.mColor4.setBackgroundColor(BgColor.COLOR_4);
		this.mColorOk = (TextView)findViewById(R.id.color_ok);
		this.mColorOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertItemView.this.mColorList.setVisibility(View.GONE);
				AlertItemView.this.mMainView.setPopupDismiss();
			}
			
		});
	}

	public void setModel(AlertItemModel itemModel){

		this.mItemModel = itemModel;
		this.mEditText.setText(this.mItemModel.getContent());
		this.mTextView.setText(this.mItemModel.getContent());
		
		this.changeBgColor(this.mItemModel.getBgColorId());
		
		if(itemModel.getId() == -1){
			this.setVisibility(View.INVISIBLE);
		}

		this.mModifyTime.setText(DateUtils.formatDateTime(this.getContext(),
                this.mItemModel.getModifyDate(), DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_SHOW_YEAR));
	}
	
	public void startEdit(boolean isCreate){
		if(this.status != STATUS_NORMAL)
			return ;
		if(isCreate)
			this.status = STATUS_CREATE;
		else this.status = STATUS_EDIT;
		this.mTextView.setVisibility(View.GONE);
		this.mEditText.setVisibility(View.VISIBLE);
		Log.d(TAG, "edit content: "+this.mItemModel.getContent() );
		this.mEditText.setSelection(this.mEditText.getEditableText().length());
		this.mEditText.requestFocus();
		
		this.inputManager.showSoftInput(this.mEditText, 0);
	}
	
	public void endEdit(){
		this.mTextView.setText(this.mItemModel.getContent());
		this.mEditText.setVisibility(View.GONE);
		this.mTextView.setVisibility(View.VISIBLE);
		this.resizeListener = null;
		inputManager.hideSoftInputFromWindow(this.mEditText.getWindowToken(), 0);
		DBHelperModel dbHelper = new DBHelperModel(AlertItemView.this.getContext());
		Log.d(TAG, ""+this.status);
		if(this.status == STATUS_CREATE){
			Long id = dbHelper.insert(this.mItemModel.formatContentValuesWithoutId());
			this.mItemModel.setId(id);
			Log.d(TAG, "item saved");
		}else if(this.status == STATUS_EDIT){
			ContentValues cv = new ContentValues();
			cv.put(AlertItemModel.MODIFY_DATE, new Date().getTime());
			cv.put(AlertItemModel.CONTENT, AlertItemView.this.mTextView.getText().toString());
			dbHelper.update(cv, 
					AlertItemModel.ID + " = " + AlertItemView.this.mItemModel.getId(), 
					null);
			Log.d(TAG,"item modified");
			
		}
		dbHelper.close();
		this.hideMenu();
		this.status = STATUS_NORMAL;
	}
	
	public void showMenu(){
		this.mMenu.setVisibility(View.VISIBLE);
	}
	
	public void hideMenu(){
		this.mMenu.setVisibility(View.GONE);
		this.mColorList.setVisibility(View.GONE);
		
		this.status = STATUS_NORMAL;
	}
	
	public boolean deleteItem(){
		DBHelperModel dbHelper = new DBHelperModel(AlertItemView.this.getContext());
		boolean result = dbHelper.delete(
				AlertItemModel.ID + " = " + AlertItemView.this.mItemModel.getId(), 
					null);

		dbHelper.close();
		this.mMainView.deleteItem(this.mItemModel.getSequence());
		return result;
	}
	
	public void AddClock(){
		this.mMainView.getTime();
	}
	
	public void shareContent(){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TITLE, "分享我的提醒");
		intent.putExtra(Intent.EXTRA_TEXT, "I have a event to share: " + this.mItemModel.getContent() + "\nFrom lightclock");
		this.getContext().startActivity(Intent.createChooser(intent, "分享"));
	}
	
	public void setBgColor(){
		this.mMenu.setVisibility(View.GONE);
		this.mColorList.setVisibility(View.VISIBLE);
	}
	
	public void changeBgColor(int colorId){
		int color = BgColor.COLOR_1;
		switch(colorId){
		case 1:
			color = BgColor.COLOR_1;
			break;
		case 2:
			color = BgColor.COLOR_2;
			break;
		case 3:
			color = BgColor.COLOR_3;
			break;
		case 4:
			color = BgColor.COLOR_4;
			break;
		}
		this.mContent.setBackgroundColor(color);
	}
	
	public void updateSequence(){
		DBHelperModel dbHelper = new DBHelperModel(AlertItemView.this.getContext());
		ContentValues cv = new ContentValues();
		cv.put(AlertItemModel.MODIFY_DATE, new Date().toString());
		cv.put(AlertItemModel.SEQUENCE, this.mItemModel.getSequence());
		dbHelper.update(cv, 
				AlertItemModel.ID + " = " + AlertItemView.this.mItemModel.getId(), 
				null);
		dbHelper.close();
	}
	
	public void setTranslucence(boolean isSet){
		if(isSet){
			AlphaAnimation localAlphaAnimation = new AlphaAnimation(1.0F, 0.5F);
			localAlphaAnimation.setDuration(100L);
		    localAlphaAnimation.setFillAfter(true);
		    startAnimation(localAlphaAnimation);
		    this.status = STATUS_DOWN;
		}else{
			this.clearAnimation();
			this.status = STATUS_NORMAL;
		}
	}
	
	public int getStatus(){
		return this.status;
	}

	public String getContent(){
		return this.mItemModel.getContent();
	}

	public void setSequence(int sequence){
		this.mItemModel.setSequence(sequence);
	}
	
	public int getSequence(){
		return this.mItemModel.getSequence();
	}
	
	public void setResizeListener(ResizeListener resize){
		this.resizeListener = resize;
	}
	
	public void setPopupListener(PopupListener popup){
	}
	
	protected void adjustHeight() {
		// TODO Auto-generated method stub
	    if (this.resizeListener != null){
	    	this.resizeListener.resize();
	    }
	}
	
	public static abstract interface ResizeListener
	{
		public abstract void resize();
	}
	
	public static abstract interface PopupListener{
		public abstract void popupDismiss(); 
	}

	public AlertItemModel getModel() {
		// TODO Auto-generated method stub
		return this.mItemModel;
	}
}
