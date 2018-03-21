package com.tagliaro.monclin.urca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals("com.tagliaro.monclin.urca.SYNC")) {
            System.out.println("Received Broadcast");

            // Call SyncService here
            Intent syncIntent = new Intent(context, SyncService.class);
            SyncService.enqueueWork(context, syncIntent);
        }
    }
}
