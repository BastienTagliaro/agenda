package com.tagliaro.monclin.urca;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

public class AlarmSetter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Log.d("AlarmSetter", "Received broadcast " + action);
        if(action != null && action.equals("com.tagliaro.monclin.urca.SET_SYNC")) {
            Integer intervalInMinutes = Integer.parseInt(sharedPreferences.getString("sync_frequency", "30"));

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.setAction("com.tagliaro.monclin.urca.SYNC");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            alarmManager.cancel(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),intervalInMinutes * 60 * 1000, pendingIntent);
        }

        else if(action != null && action.equals("com.tagliaro.monclin.urca.SET_NOTIFY") && sharedPreferences.getBoolean("enable_reminders", false)){
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.setAction("com.tagliaro.monclin.urca.NOTIFY");

            Log.d("AlarmSetter", "Sending notify intent");

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),60 * 1000, pendingIntent);
        }
    }
}
