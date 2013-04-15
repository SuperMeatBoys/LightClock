package com.gesuper.lightclock.model;

import java.util.ArrayList;
import java.util.List;

import com.gesuper.lightclock.view.AlertItemView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class AlertListAdapter extends ArrayAdapter<AlertItemModel> {
	public static final String TAG = "AlertListAdapter";
	private ArrayList<AlertItemView> mViewArray;
	public AlertListAdapter(Context context, int resource, List<AlertItemModel> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.mViewArray = new ArrayList<AlertItemView>();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		AlertItemView mItemView;
		if(convertView != null){
			mItemView = (AlertItemView) convertView;
		}
		else mItemView  = new AlertItemView(this.getContext());
		
		AlertItemModel mItemModel = getItem(position);
		mItemView.setModel(mItemModel);
		if(mItemView.getModel().getId() == -1){
			//mItemView.showFastMenu();
			Log.d(TAG, "" + mItemView.getMHeight());
			mItemView.setPadding(0, - mItemView.getMHeight(), 0, 0);
			mItemView.setMHeight(mItemView.getHeight());
		}
        return mItemView;
	}
}
