package com.gesuper.lightclock.view;

import java.util.Calendar;

import com.gesuper.lightclock.R;

import android.content.Context;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

public class ClockView extends LinearLayout {

	private DatePicker mDate;
	private TimePicker mTime;
	
	public ClockView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
		inflate(context, R.layout.activity_clock, this);
		this.mDate = (DatePicker)findViewById(R.id.clock_date);
		//this.mTime = (TimePicker)findViewById(R.id.clock_time);
	}

	public long getTime(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(
			this.mDate.getDayOfMonth(),
			this.mDate.getMonth(),
			this.mDate.getYear(),
			this.mTime.getCurrentHour(),
			this.mTime.getCurrentMinute()
		);
		
		long clockTime = calendar.getTimeInMillis();
		
		return clockTime;
	}
}
