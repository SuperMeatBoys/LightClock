package com.gesuper.lightclock.model;

import java.util.List;

import com.gesuper.lightclock.view.AlertItemView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class AlertListAdapter extends ArrayAdapter<AlertItemModel> {
	public static final String TAG = "AlertListAdapter";
	
	public AlertListAdapter(Context context, int resource, List<AlertItemModel> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		AlertItemView mItemView;
		if (convertView != null) {
			mItemView = (AlertItemView)convertView;
			Log.d(TAG, "old");
		}
		else {
			mItemView  = new AlertItemView(this.getContext());
		}
		AlertItemModel mItemModel = getItem(position);
		mItemView.setModel(mItemModel);
		Log.d(TAG, "position:" + position + " content:" + mItemView.getContent());
		if(mItemView.getModel().getId() == -1){
			//mItemView.showFastMenu();
			mItemView.setPadding(0, - mItemView.getHeight(), 0, 0);
			mItemView.setMHeight(mItemView.getHeight());
			Log.d(TAG, "show menu:" + mItemView.getContent());
		}
        return mItemView;
	}
}
