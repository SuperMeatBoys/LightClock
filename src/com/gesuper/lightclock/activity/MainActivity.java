package com.gesuper.lightclock.activity;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.view.MainView;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

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
