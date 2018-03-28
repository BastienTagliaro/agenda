package com.tagliaro.monclin.urca.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

public class SyncSetter extends BroadcastReceiver {
    private static final String TAG = "SyncSetter";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Log.d(TAG, "Received broadcast " + action);

        if(action != null && action.equals("com.tagliaro.monclin.urca.SET_SYNC")) {
            Log.d(TAG, "Sending SYNC broadcast");
            Integer intervalInMinutes = Integer.parseInt(sharedPreferences.getString("sync_frequency", "30"));
            Log.d(TAG, "Sync frequency is " + intervalInMinutes);

            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.setType("SYNC");
            alarmIntent.setAction("com.tagliaro.monclin.urca.SYNC");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            if(alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),intervalInMinutes * 60 * 1000, pendingIntent);
            }
        }
    }
}
