package com.tagliaro.monclin.urca.background;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tagliaro.monclin.urca.R;
import com.tagliaro.monclin.urca.ui.DetailsActivity;
import com.tagliaro.monclin.urca.utils.Classes;
import com.tagliaro.monclin.urca.utils.DatabaseHandler;

public class NotifyService extends Service {
    public static final String PRIMARY_CHANNEL = "default";
    private final String TAG = getClass().getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final long[] classesIds = intent.getLongArrayExtra("classesIds");
        long[] timeLeft = intent.getLongArrayExtra("timeLeft");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default";
            String description = "Primary Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        class NotifyTask implements Runnable {
            long classId;
            NotifyTask(long c) { classId = c; }

            @Override
            public void run() {}
        }

        Log.d(TAG, classesIds.length + " event(s) to notify");

        for(int i = 0; i < classesIds.length; ++i) {
//            Toast.makeText(getApplicationContext(), "Started runnable with time left : " + timeLeft[i], Toast.LENGTH_LONG).show();
            (new Handler()).postDelayed(new NotifyTask(classesIds[i]) {
                @Override
                public void run() {
                    showNotification(this.classId);
                }
            }, timeLeft[i] * 1000);
        }

        return Service.START_NOT_STICKY;
    }

    private void showNotification(long classId) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        Classes c = databaseHandler.getClass(classId);

        Intent intentActivity = new Intent(getApplicationContext(), DetailsActivity.class);
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentActivity.putExtra("id", classId);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 20, intentActivity, 0);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(c.getClassname())
                .setContentText(c.getClassroom())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) classId, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Stopped");
    }
}
