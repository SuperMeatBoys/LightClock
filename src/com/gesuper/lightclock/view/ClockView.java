package com.gesuper.lightclock.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.gesuper.lightclock.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.TimePicker;

public class ClockView extends LinearLayout {

	private EditText etCalendar;
	private Button btnCalendar;
	
	private EditText etTime;
	private Button btnTime;
	private Date time;
	
	public ClockView(Context context){
		super(context);
	}
	
	public ClockView(Context context, long alertTime) {
		super(context);
		// TODO Auto-generated constructor stub
		
		inflate(context, R.layout.activity_clock, this);
		if(alertTime > 0)
			this.time = new Date(alertTime);
		else this.time = new Date();
		initResource();
		
	}

	@SuppressLint("SimpleDateFormat")
	private void initResource() {
		// TODO Auto-generated method stub
		this.etCalendar = (EditText)findViewById(R.id.clock_calendar_et);
		this.btnCalendar = (Button)findViewById(R.id.clock_calendar_btn);
		
		this.etTime = (EditText)findViewById(R.id.clock_time_et);
		this.btnTime = (Button)findViewById(R.id.clock_time_btn);
		
		this.btnCalendar.setOnClickListener(new OnClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LinearLayout mCalendarView = 
						(LinearLayout) LayoutInflater.from(ClockView.this.getContext()).
						inflate(R.layout.activity_clock_calendar, null);
				
				final DatePicker localDatePicker = (DatePicker)mCalendarView.findViewById(R.id.clock_datepicker);
				
		        localDatePicker.init(1900 + ClockView.this.time.getYear(), 
		        		ClockView.this.time.getMonth(), ClockView.this.time.getDate(), null);
				new AlertDialog.Builder(ClockView.this.getContext()).
					setTitle(R.string.clock_calendar).
					setView(mCalendarView).
					setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
							ClockView.this.time.setYear(-1900 + localDatePicker.getYear());
							ClockView.this.time.setMonth(localDatePicker.getMonth());
							ClockView.this.time.setDate(localDatePicker.getDayOfMonth());
							SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
							ClockView.this.etCalendar.setText(mDateFormat.format(ClockView.this.time.getTime()));
						}
						
					}).
					setNegativeButton(R.string.dialog_cancel, null).show();
			}
			
		});
		
		this.btnTime.setOnClickListener(new OnClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LinearLayout mTimeView = 
						(LinearLayout) LayoutInflater.from(ClockView.this.getContext()).
						inflate(R.layout.activity_clock_time, null);
				final TimePicker localTimePicker = (TimePicker)mTimeView.findViewById(R.id.clock_timepicker);
		        localTimePicker.setIs24HourView(Boolean.valueOf(true));
		        localTimePicker.setCurrentHour(Integer.valueOf(ClockView.this.time.getHours()));
		        localTimePicker.setCurrentMinute(Integer.valueOf(ClockView.this.time.getMinutes()));
				new AlertDialog.Builder(ClockView.this.getContext()).
					setTitle(R.string.clock_time).
					setView(mTimeView).
					setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							ClockView.this.time.setHours(localTimePicker.getCurrentHour().intValue());
							ClockView.this.time.setMinutes(localTimePicker.getCurrentMinute().intValue());
							ClockView.this.time.setSeconds(0);
							SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
				            ClockView.this.etTime.setText(mDateFormat.format(ClockView.this.time.getTime()));
				            
						}
						
					}).
					setNegativeButton(R.string.dialog_cancel, null).show();
			}
			
		});
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		this.etCalendar.setText(mDateFormat.format(this.time.getTime()));

		mDateFormat = new SimpleDateFormat("HH:mm");
        this.etTime.setText(mDateFormat.format(this.time.getTime()));
	}

	public long getClockTime(){
		return this.time.getTime();
	}
}
