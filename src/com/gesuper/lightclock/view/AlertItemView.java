package com.gesuper.lightclock.view;

import java.util.Date;
import java.util.Random;

import com.gesuper.lightclock.activity.*;
import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.*;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	private LinearLayout mClock;
	private TextView mClockTime;
	private TextView mShare;
	private TextView mColor;
	
	private InputMethodManager inputManager;
	private ResizeListener resizeListener;
	
	private Handler mRemoveMenuBackgroundColor = new Handler(){
		public void handleMessage(Message message){
			View v = AlertItemView.this.findViewById(message.what);
			v.setBackgroundColor(Color.WHITE);
		}
	};
	
	private OnTouchListener itemTouchListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				v.setBackgroundColor(Color.GREEN);
				break;
			case MotionEvent.ACTION_UP: 
				AlertItemView.this.mRemoveMenuBackgroundColor.
					sendEmptyMessageDelayed(v.getId(), 300);
				break;
			}
			return false;
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
			private String shortStr;
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

		        AlertItemView.this.mItemModel.setContent(s.toString());
				if (AlertItemView.this.mEditText.getLineCount() <= 3)
		        {
					this.shortStr = s.toString();
		        }
				else {
					AlertItemView.this.mItemModel.setShortContent(this.shortStr);
				}
		        AlertItemView.this.adjustHeight();
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
		this.mClock = (LinearLayout)findViewById(R.id.item_clock);
		this.mClockTime= (TextView)findViewById(R.id.item_clock_time); 
		this.mShare = (TextView)findViewById(R.id.item_share);
		this.mColor = (TextView)findViewById(R.id.item_color);
		
		this.mClock.setOnTouchListener(itemTouchListener);
		this.mClock.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertItemView.this.AddClockMenu();
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
				int colorId = AlertItemView.this.mItemModel.getBgColorId();
				colorId += 1;
				if(colorId > BgColor.COLOR_COUNT)
					colorId = 1;
				AlertItemView.this.changeBgColor(colorId);
			}
			
		});
		
	}


	public void setModel(AlertItemModel itemModel){

		this.mItemModel = itemModel;
		this.mEditText.setText(this.mItemModel.getContent());
		this.mTextView.setText(this.mItemModel.getShortContentForTextView());
		
		this.changeBgColor(this.mItemModel.getBgColorId());

		this.mModifyTime.setText(DateUtils.formatDateTime(this.getContext(),
                this.mItemModel.getModifyDate(), DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_SHOW_YEAR));
		
		if(this.mItemModel.getAlertDate() > 0){
			this.mClockTime.setText(getFormatClockTime(this.mItemModel.getAlertDate()));
		}
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
			cv.put(AlertItemModel.SHORT_CONTENT, AlertItemView.this.getShortContent());
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
		
		this.status = STATUS_NORMAL;
	}
	
	public boolean deleteItem(){
		DBHelperModel dbHelper = new DBHelperModel(AlertItemView.this.getContext());
		boolean result = dbHelper.delete(
				AlertItemModel.ID + " = " + AlertItemView.this.mItemModel.getId(), 
					null);

		dbHelper.close();
		return result;
	}
	
	public void AddClockMenu(){
		this.mMenu.setVisibility(View.GONE);
		final ClockView mClockView = new ClockView(this.getContext());
		
		Builder mAlertDialog = new AlertDialog.Builder(this.getContext()).
				setTitle(R.string.select_time).
				setView(mClockView).
				setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						long time = mClockView.getClockTime();
						AddClockFunction(time);
					}
				}).setNegativeButton(R.string.dialog_cancel, null);
		if(this.mItemModel.getAlertDate() > 0){
			mAlertDialog.setNeutralButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AlertItemView.this.deleteClock();
				}
			});
		}
		mAlertDialog.show();
	}
	
	public void AddClockFunction(long alertTime){
		DBHelperModel dbHelper = new DBHelperModel(this.getContext());
		ContentValues cv = new ContentValues();
		cv.put(AlertItemModel.ALERT_DATE, alertTime);
		dbHelper.update(cv, 
				AlertItemModel.ID + " = " + this.mItemModel.getId(), 
				null);
		dbHelper.close();
		
		Intent intent = new Intent(this.getContext(), ClockReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putLong("com.gesuper.lightclock.ALERT_ID", this.mItemModel.getId());
		intent.putExtras(bundle);
		
		//intent.setDataAndType(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mClockModel.getId()));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getContext(), 0, intent, 0);
        AlarmManager am = (AlarmManager) this.getContext().getSystemService(Context.ALARM_SERVICE);
        //设置闹钟
        am.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
        
        this.mClockTime.setText(getFormatClockTime(this.mItemModel.getAlertDate()));
	}

	protected void deleteClock() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this.getContext(), ClockReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putLong("com.gesuper.lightclock.ALERT_ID", this.mItemModel.getId());
		intent.putExtras(bundle);
		
		//intent.setDataAndType(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mClockModel.getId()));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getContext(), 0, intent, 0);
        AlarmManager am = (AlarmManager) this.getContext().getSystemService(Context.ALARM_SERVICE);
        
        am.cancel(pendingIntent);
        
        DBHelperModel dbHelper = new DBHelperModel(this.getContext());
		ContentValues cv = new ContentValues();
		cv.put(AlertItemModel.ALERT_DATE, 0);
		dbHelper.update(cv, 
				AlertItemModel.ID + " = " + this.mItemModel.getId(), 
				null);
		dbHelper.close();

		this.mClockTime.setText("");
	}
	
	public void shareContent(){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TITLE, "Share my alert...");
		intent.putExtra(Intent.EXTRA_TEXT, "I have a event to share: " + this.mItemModel.getContent() + "\nFrom lightclock");
		this.getContext().startActivity(Intent.createChooser(intent, "Share"));
	}
	
	public void setBgColor(){
		this.mMenu.setVisibility(View.GONE);
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
		
		this.mItemModel.setBgColorId(colorId);
		this.mContent.setBackgroundColor(color);
	}
	
	public void updateSequence(){
		DBHelperModel dbHelper = new DBHelperModel(AlertItemView.this.getContext());
		ContentValues cv = new ContentValues();
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
	
	private String getShortContent() {
		// TODO Auto-generated method stub
		return this.mItemModel.getShortContent();
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
	
	public void setRandBgColor() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		this.changeBgColor(rand.nextInt(BgColor.COLOR_COUNT) + 1);
	}

	public void setStatusNormal() {
		// TODO Auto-generated method stub
		this.status = STATUS_NORMAL;
	}
	
	@SuppressWarnings("deprecation")
	private String getFormatClockTime(long clockTime) {
		// TODO Auto-generated method stub
		Date dateClock = new Date(clockTime);
		Date dateNow = new Date();
		String format = "";
		if(dateClock.getYear() > dateNow.getYear()){
			format += String.valueOf(dateClock.getYear()) + "-";
			format += "" + (dateClock.getMonth()+1) + "-" + dateClock.getDate();
		}
		else if(dateClock.getMonth() > dateNow.getMonth() || dateClock.getDate() > dateNow.getDate())
			format = "" + (dateClock.getMonth()+1) + "-" + dateClock.getDate();
		else if(dateClock.getMonth() == dateNow.getMonth() && dateClock.getDate() == dateNow.getDate())
			format = "" + dateClock.getHours() + ":" + dateClock.getMinutes();
		else {
			if(dateClock.getYear() < dateNow.getYear())
				format += String.valueOf(dateClock.getYear()) + "-";
			format += "" + (dateClock.getMonth()+1) + "-" + dateClock.getDate();
		}
		return format;
	}
}
