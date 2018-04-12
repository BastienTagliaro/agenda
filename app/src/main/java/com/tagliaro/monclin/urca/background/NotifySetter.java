package com.tagliaro.monclin.urca.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.tagliaro.monclin.urca.utils.Log;

import java.util.Calendar;

public class NotifySetter extends BroadcastReceiver {
    private static final String TAG = "NotifySetter";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Log.d(TAG, "Received broadcast " + action);
        boolean enableReminders = sharedPreferences.getBoolean("enable_reminders", false);

        if(enableReminders && action != null && action.equals("com.tagliaro.monclin.urca.SET_NOTIFY")){
            Log.d(TAG, "Sending NOTIFY broadcast");

            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.setType("NOTIFY");
            alarmIntent.setAction("com.tagliaro.monclin.urca.NOTIFY");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            if(alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5 * 60 * 1000, pendingIntent);
            }
        }
    }
}
