package com.gesuper.lightclock.activity;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.view.ClockView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ClockDialog extends AlertDialog implements OnClickListener{
	
	private ClockView mClockView;
	private OnClockSelectListener mClock;
	
	public interface OnClockSelectListener{
		void OnDateTimeSelected(AlertDialog dialog, long date);
	}
	
	@SuppressWarnings("deprecation")
	protected ClockDialog(Context context, long date) {
		super(context);
		this.mClockView = new ClockView(context);
		setView(mClockView);
		this.mClock = null;
		setButton(context.getString(R.string.dialog_ok), this);
        setButton2(context.getString(R.string.dialog_cancel), (OnClickListener)null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(this.mClockView);
		
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(this.mClock != null)
			this.mClock.OnDateTimeSelected(this, this.mClockView.getClockTime());
	}
	
	public void setOnClockSelectListener(OnClockSelectListener mClockSelectListener){
		this.mClock = mClockSelectListener;
	}
	
	
}
