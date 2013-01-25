package com.gesuper.lightclock.activity;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.activity.ClockDialog.OnClockSelectListener;
import com.gesuper.lightclock.view.MainView;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	
	private MainView mMainView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mMainView = new MainView(this);
		this.setContentView(this.mMainView);
	}
	
	public View getView(){
		return this.mMainView;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void getTime(){
		
		ClockDialog d = new ClockDialog(this);
        d.setOnClockSelectListener(new OnClockSelectListener() {
            public void OnDateTimeSelected(AlertDialog dialog, long date) {
                Toast.makeText(MainActivity.this, String.valueOf(date), Toast.LENGTH_SHORT).show();
            }
        });
        d.show();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		this.mMainView.saveSequence();
	}
	
	@Override
	protected void onStop()
	{
	    super.onStop();
	    this.mMainView.saveSequence();
	}
	
	@Override
	protected void onDestroy()
	{
	    super.onDestroy();
	    this.mMainView.saveSequence();
	}
}
