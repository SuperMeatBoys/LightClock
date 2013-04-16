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
	
	private InputMethodManager inputManager;
	private ResizeListener resizeListener;

	private int mHeight;
	
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
		this.mHeight = 0;
		this.mContent = (RelativeLayout)findViewById(R.id.alert_content);
		this.mEditText = (EditText)findViewById(R.id.ed_content);
		this.mTextView = (TextView)findViewById(R.id.tv_content);
		this.status = STATUS_NORMAL;
		this.measureView(this);
		this.mHeight = this.getMeasuredHeight();
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
		
		this.mAlphaAnimation = new AlphaAnimation(1.0F, 0.5F);
		this.mAlphaAnimation.setDuration(100L);
		this.mAlphaAnimation.setFillAfter(true);
	}


	public void setModel(AlertItemModel itemModel){
		// TODO Auto-generated method stub
		this.mItemModel = itemModel;
		this.mEditText.setText(this.mItemModel.getContent());
		this.mTextView.setText(this.mItemModel.getShortContentForTextView());
		
		this.setBgColor(this.mItemModel.getBgColorId());
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
		this.status = STATUS_NORMAL;
	}
	
	public boolean deleteItem(){
		return this.mItemModel.delete();
	}
	
	public void shareContent(){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TITLE, "Share my alert...");
		intent.putExtra(Intent.EXTRA_TEXT, this.mItemModel.getContent() + "\nFrom lightclock");
		this.getContext().startActivity(Intent.createChooser(intent, "Share"));
	}
	
	public void setBgColor(int colorId){
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

		this.mItemModel.setBgColorId(colorId);
	}
	
	public void changeBgColor(int colorId){
		this.setBgColor(colorId);
		
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

	public void setMHeight(int height) {
		// TODO Auto-generated method stub
		this.mHeight = height;
	}
	
	public int getMHeight(){
		return this.mHeight;
	}
}
