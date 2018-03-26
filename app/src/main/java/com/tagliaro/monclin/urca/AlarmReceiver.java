package com.tagliaro.monclin.urca;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String PRIMARY_CHANNEL = "default";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action != null && action.equals("com.tagliaro.monclin.urca.SYNC")) {
            Log.d("AlarmReceiver", "Received Broadcast");

            // Call SyncService here
            Intent syncIntent = new Intent(context, SyncService.class);
            SyncService.enqueueWork(context, syncIntent);
        }

        else if(action != null && action.equals("com.tagliaro.monclin.urca.NOTIFY")) {
            Log.d("AlarmReceiver", "Received notify");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Default";
                String description = "Primary Channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if(notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

            Date now = new Date();
            SimpleDateFormat day = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
            SimpleDateFormat completeDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.FRANCE);
            String today = day.format(now);
            DatabaseHandler databaseHandler = new DatabaseHandler(context);
            List<Cours> classList = databaseHandler.getCours(today);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int remindersBeforeSeconds = (Integer.parseInt(sharedPreferences.getString("reminders_before", "15"))) * 60;
            int limitMax = remindersBeforeSeconds + 60;
            int limitMin = remindersBeforeSeconds - 60;

//            long testTime = 1522051200;

            for(Cours c : classList) {
                try {
                    Date date = completeDate.parse(c.getDate() + " " + c.getHeureDebut());
//                    long dateTime = date.getTime()/1000;
                    long difference = Math.abs(now.getTime() - date.getTime())/1000;
//                    long difference = dateTime - testTime;

                    if(limitMax - difference >= 0 && difference - limitMin >= 0) {
                        Log.d("AlarmReceiver", "Notify!");

                        Intent intentActivity = new Intent(context, DetailsActivity.class);
                        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intentActivity.putExtra("id", c.getId());
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 20, intentActivity, 0);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL)
                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                .setContentTitle(c.getNomCours())
                                .setContentText(c.getSalle())
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // add opening activity on click
                        notificationManager.notify((int) c.getId(), mBuilder.build()); // set static ID I guess? Or find a way to only display this notification once
                    }

                    Log.d("AlarmReceiver", "Now : " + now.toString() + " ; Date : " + date.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int createID(){
        Date now = new Date();
        return Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.FRANCE).format(now));
    }
}
