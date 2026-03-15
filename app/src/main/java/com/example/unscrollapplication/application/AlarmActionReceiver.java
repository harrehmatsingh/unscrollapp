package com.example.unscrollapplication.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (DoomscrollAlarm.ACTION_STOP_ALARM.equals(action)) {
            DoomscrollAlarm.stop(context);
        }
    }
}
