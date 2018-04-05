package com.tagliaro.monclin.urca.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;

import com.tagliaro.monclin.urca.utils.Classes;
import com.tagliaro.monclin.urca.utils.DatabaseHandler;
import com.tagliaro.monclin.urca.utils.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action != null && action.equals("com.tagliaro.monclin.urca.SYNC")) {
            Log.d("AlarmReceiver", "Received SYNC");

            // Call SyncService here
            Intent syncIntent = new Intent(context, SyncService.class);
            SyncService.enqueueWork(context, syncIntent);
        }

        else if(action != null && action.equals("com.tagliaro.monclin.urca.NOTIFY")) {
            Log.d("AlarmReceiver", "Received NOTIFY");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            LongSparseArray<Long> classesToNotify = new LongSparseArray<>();
            Date now = new Date();
            SimpleDateFormat day = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
            SimpleDateFormat completeDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.FRANCE);
            String today = day.format(now);
            DatabaseHandler databaseHandler = new DatabaseHandler(context);
//
//            databaseHandler.add(new Classes("Test", "2-R06", "desc", "30-03-2018", "18:59", "20:00"));
//            databaseHandler.add(new Classes("TRUC", "2-R06", "desc", "30-03-2018", "19:00", "20:00"));
//            databaseHandler.add(new Classes("TRUC", "2-R06", "desc", "30-03-2018", "19:01", "20:00"));
            List<Classes> classList = databaseHandler.getClass(today);
//            List<Classes> classList = new ArrayList<>();

            long remindersBeforeSeconds = (Long.parseLong(sharedPreferences.getString("reminders_before", "15"))) * 60;
            long secondsBeforeRunnable = 5*60; // If we're 5 min away from the remindersBeforeSeconds we start service with a runnable

            for(Classes c : classList) {
                try {
                    Date date = completeDate.parse(c.getDate() + " " + c.getStartTime());
                    long difference = (date.getTime() - now.getTime())/1000;

                    if(difference != 0 && (difference < remindersBeforeSeconds + secondsBeforeRunnable && difference > remindersBeforeSeconds)) {
                        classesToNotify.put(c.getId(), (difference - remindersBeforeSeconds) + 5);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            long[] classesIds = new long[classesToNotify.size()];
            long[] timeLeft = new long[classesToNotify.size()];

            for(int i = 0; i < classesToNotify.size(); ++i) {
                classesIds[i] = classesToNotify.keyAt(i);
                timeLeft[i] = classesToNotify.get(classesIds[i]);
            }

            if(classesToNotify.size() > 0) {
                Log.d(TAG, classesToNotify.size() + " event(s) to notify");

                for(int i = 0; i < classesToNotify.size(); ++i) {
                    Log.d(TAG, "Event " + classesIds[i] + " should be notified in " + timeLeft[i]);

                    Intent notifyIntent = new Intent(context, NotifyService.class);
                    notifyIntent.setAction("com.tagliaro.monclin.urca.NOTIFY");
                    notifyIntent.putExtra("id", classesIds[i]);
                    notifyIntent.putExtra("timeLeft", timeLeft[i]);
                    context.startService(notifyIntent);
                }

            }
            else {
                Log.d(TAG, "Nothing to notify");
            }
        }
    }
}
