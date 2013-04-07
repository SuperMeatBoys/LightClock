package com.gesuper.lightclock.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.gesuper.lightclock.activity.*;
import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.*;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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
	
	private AlertItemModel mItemModel;
	private AlphaAnimation mAlphaAnimation;
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
	private int mMenuHeight;
	private Handler mMenuAnimationHandler = new Handler(){
		public void handleMessage(Message message){
			AlertItemView.this.mMenu.setPadding(0, message.what, 0, 0);
		}
	};
	
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
	    inflate(context, R.layout.activity_alert_item, this);
	    
		this.inputManager =
                (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		this.initResource();
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
		
		this.mMenu = (LinearLayout)findViewById(R.id.item_menu);
		this.measureView(this.mMenu);
		this.mMenuHeight = this.mMenu.getMeasuredHeight();
		this.mMenu.setPadding(0, -this.mMenuHeight, 0, 0);
		
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
		
		this.mAlphaAnimation = new AlphaAnimation(1.0F, 0.5F);
		this.mAlphaAnimation.setDuration(100L);
		this.mAlphaAnimation.setFillAfter(true);
	}


	public void setModel(AlertItemModel itemModel){
		// TODO Auto-generated method stub
		this.mItemModel = itemModel;
		this.mEditText.setText(this.mItemModel.getContent());
		this.mTextView.setText(this.mItemModel.getShortContentForTextView());
		
		this.changeBgColor(this.mItemModel.getBgColorId());

		this.mModifyTime.setText(this.getFormatTime(this.mItemModel.getModifyDate()));
		
		this.setClockTime();
	}
	
	public void setClockTime(){
		if(this.mItemModel.getAlertDate() > 0){
			if(this.mItemModel.getAlertDate() > System.currentTimeMillis())
				this.mClockTime.setText(this.getFormatTime(this.mItemModel.getAlertDate()));
			else this.mClockTime.setText(R.string.item_menu_clock_passed);
		}
	}
	
	public void startEdit(boolean isCreate){
		if(this.status != STATUS_NORMAL)
			return ;
		if(isCreate)
			this.status = STATUS_CREATE;
		else this.status = STATUS_EDIT;
		this.mEditText.setVisibility(View.VISIBLE);
		this.mTextView.setVisibility(View.GONE);
		Log.d(TAG, "edit content: "+this.mItemModel.getContent() );
		this.mEditText.setSelection(this.mEditText.getEditableText().length());
		this.mEditText.requestFocus();
		
		this.inputManager.showSoftInput(this.mEditText, 0);
	}
	
	public void endEdit(){
		this.mTextView.setText(this.mItemModel.getContent());
		this.mTextView.setVisibility(View.VISIBLE);
		this.mEditText.setVisibility(View.GONE);
		
		this.resizeListener = null;
		this.inputManager.hideSoftInputFromWindow(this.mEditText.getWindowToken(), 0);
		if(this.status == STATUS_CREATE){
			this.mItemModel.insert();
		}else if(this.status == STATUS_EDIT){
			this.mItemModel.update();
		}
		this.hideMenu();
		this.status = STATUS_NORMAL;
	}
	
	public void showFastMenu(){
		if(AlertItemView.this.mMenuHeight == 0){
        	AlertItemView.this.mMenuHeight = AlertItemView.this.mMenu.getHeight();
        }
		this.mMenu.setPadding(0, 0, 0, 0);
	}
	
	public void showMenu(){
        if(AlertItemView.this.mMenuHeight == 0){
        	AlertItemView.this.mMenuHeight = AlertItemView.this.mMenu.getHeight();
        }
        new Thread(){
        	public void run(){
        		int paddingTop = -AlertItemView.this.mMenuHeight + 16;
        		while(paddingTop <= 0){
        			AlertItemView.this.mMenuAnimationHandler.sendEmptyMessageDelayed(paddingTop, (AlertItemView.this.mMenuHeight + paddingTop));
        			paddingTop += 16;
        		}
        		AlertItemView.this.mMenuAnimationHandler.sendEmptyMessageDelayed(0, (AlertItemView.this.mMenuHeight));
        	}
        }.start();
	}
	
	public void hideFastMenu(){
		this.mMenu.setPadding(0, -this.mMenuHeight, 0, 0);
	}
	
	public void hideMenu(){
		new Thread(){
        	public void run(){
        		int paddingTop = 16; 
        		while(paddingTop <  AlertItemView.this.mMenuHeight){
        			AlertItemView.this.mMenuAnimationHandler.sendEmptyMessageDelayed(-paddingTop, paddingTop);
        			paddingTop += 16;
        		}

            	AlertItemView.this.mMenuAnimationHandler.sendEmptyMessageDelayed(-AlertItemView.this.mMenuHeight, AlertItemView.this.mMenuHeight);
        	}
        }.start();
		this.status = STATUS_NORMAL;
	}
	
	public boolean deleteItem(){
		return this.mItemModel.delete();
	}
	
	public void AddClockMenu(){
		final ClockView mClockView = new ClockView(this.getContext(), this.mItemModel.getAlertDate());
		
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
        this.mItemModel.setAlertDate(alertTime);
        this.mItemModel.update();
	
        AlarmManager am = (AlarmManager) this.getContext().getSystemService(Context.ALARM_SERVICE);
        //设置闹钟
        am.set(AlarmManager.RTC_WAKEUP, alertTime, this.getPendingIntent());
        
        this.setClockTime();
	}

	protected void deleteClock() {
		// TODO Auto-generated method stub
        AlarmManager am = (AlarmManager) this.getContext().getSystemService(Context.ALARM_SERVICE);
        
        am.cancel(this.getPendingIntent());
        
        this.mItemModel.deleteClock();
        
		this.mClockTime.setText("");
	}
	
	private PendingIntent getPendingIntent(){
		Intent intent = new Intent(this.getContext(), ClockReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putLong("com.gesuper.lightclock.ALERT_ID", this.mItemModel.getId());
		intent.putExtras(bundle);
		
		//intent.setDataAndType(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mClockModel.getId()));
        return PendingIntent.getBroadcast(this.getContext(), 0, intent, 0);
	}
	
	public void shareContent(){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TITLE, "Share my alert...");
		intent.putExtra(Intent.EXTRA_TEXT, "I have a event to share: " + this.mItemModel.getContent() + "\nFrom lightclock");
		this.getContext().startActivity(Intent.createChooser(intent, "Share"));
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
		this.mItemModel.update();
		
		this.mContent.setBackgroundColor(color);
	}
	
	public void updateSequence(){
		this.mItemModel.update();
	}
	
	public void setTranslucence(boolean isSet){
		if(isSet){
		    startAnimation(this.mAlphaAnimation);
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
	
	public void setRandBgColor() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		this.changeBgColor(rand.nextInt(BgColor.COLOR_COUNT) + 1);
	}

	public void setStatusNormal() {
		// TODO Auto-generated method stub
		this.status = STATUS_NORMAL;
	}
	
	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	private String getFormatTime(long time) {
		// TODO Auto-generated method stub
		Date dateClock = new Date(time);
		Date dateNow = new Date();
		SimpleDateFormat mDateFormat;
		if(dateClock.getYear() > dateNow.getYear() || dateClock.getYear() < dateNow.getYear()){
			mDateFormat = new SimpleDateFormat(" yy-MM-dd");
		}
		else if(dateClock.getMonth() > dateNow.getMonth() || dateClock.getDate() > dateNow.getDate()){
			mDateFormat = new SimpleDateFormat(" MM-dd");
		}
		else if(dateClock.getMonth() == dateNow.getMonth() && dateClock.getDate() == dateNow.getDate()){
			mDateFormat = new SimpleDateFormat(" HH:mm");
		}
		else {
			mDateFormat = new SimpleDateFormat(" MM-dd"); 
		}

		return mDateFormat.format(time);
	}
	
	public int getMenuHeight(){
		return this.mMenuHeight;
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
}
