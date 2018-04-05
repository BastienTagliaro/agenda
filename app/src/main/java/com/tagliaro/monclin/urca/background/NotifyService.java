package com.tagliaro.monclin.urca.background;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.tagliaro.monclin.urca.R;
import com.tagliaro.monclin.urca.ui.DetailsActivity;
import com.tagliaro.monclin.urca.utils.Classes;
import com.tagliaro.monclin.urca.utils.DatabaseHandler;
import com.tagliaro.monclin.urca.utils.Log;

public class NotifyService extends Service {
    public static final String PRIMARY_CHANNEL = "default";
    private final String TAG = getClass().getSimpleName();
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            long id = bundle.getLong("id", 0);
            long timeLeft = bundle.getLong("timeLeft", 0);
            Log.d("ServiceHandler", "Received id " + id + " with time left " + timeLeft);

            try {
                Thread.sleep(timeLeft * 1000);
                showNotification(id);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            stopSelf(msg.arg1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
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

        Log.d(TAG, "Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HandlerThread thread = new HandlerThread("NotifyThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper serviceLooper = thread.getLooper();
        ServiceHandler serviceHandler = new ServiceHandler(serviceLooper);

        long id = intent.getLongExtra("id", 0);
        long timeLeft = intent.getLongExtra("timeLeft", 0);

        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        bundle.putLong("timeLeft", timeLeft);

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.setData(bundle);
        serviceHandler.sendMessage(msg);

/*        class NotifyTask implements Runnable {
            long classId;
            NotifyTask(long c) { classId = c; }

            @Override
            public void run() {}
        }

        Log.d(TAG, Arrays.toString(classesIds));

        for(int i = 0; i < classesIds.length; ++i) {
            Log.d(TAG, "Event " + classesIds[i] + " in " + timeLeft[i] + " seconds");
//            Toast.makeText(getApplicationContext(), "Started runnable with time left : " + timeLeft[i], Toast.LENGTH_LONG).show();
            (new Handler()).postDelayed(new NotifyTask(classesIds[i]) {
                @Override
                public void run() {
                    showNotification(this.classId);
                }
            }, timeLeft[i] * 1000);
        }*/

        return Service.START_STICKY;
    }

    private void showNotification(long classId) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        Classes c = databaseHandler.getClass(classId);

        Intent intentActivity = new Intent(getApplicationContext(), DetailsActivity.class);
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentActivity.putExtra("id", classId);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) classId, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(c.getClassname())
                .setContentText(c.getClassroom())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) c.getId(), mBuilder.build());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopped");

        super.onDestroy();
    }
}
