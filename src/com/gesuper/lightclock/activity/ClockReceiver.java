package com.gesuper.lightclock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClockReceiver extends BroadcastReceiver {
	@Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, ClockAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

