package com.tagliaro.monclin.urca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.CalendarView;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver updateReceiver;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CalendarView calendarView = findViewById(R.id.calendarView);
        final ListView classesList = findViewById(R.id.listView);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        currentDate = dateFormat.format(calendarView.getDate());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Temporary, this will be up to the user to choose in settings
        editor.putString("agenda", "http://agenda.univ-reims.fr/ical.php?cle=33fc8e511b003f20a2a9cbbb3c5eec2e");
        editor.putInt("interval", 2);
        editor.apply();

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + getPackageName());
        Boolean success = true;

        if(!folder.exists())
            success = folder.mkdirs();

        setListItems(this, R.layout.classes_view, currentDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                month += 1;
                currentDate = String.format(Locale.FRANCE, "%02d", day) + "-" + String.format(Locale.FRANCE, "%02d", month) + "-" + Integer.toString(year);
                setListItems(getApplicationContext(), R.layout.classes_view, currentDate);
            }
        });

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received broadcast, refreshing");

                setListItems(context, R.layout.classes_view, currentDate);
            }
        };

        // This is just to make sure the AlarmSetter is always running the Alarm
        Intent intent = new Intent("com.tagliaro.monclin.urca.RESET_ALARM");
        intent.setClass(this, AlarmSetter.class);
        sendBroadcast(intent);

        registerReceiver(updateReceiver, new IntentFilter("urca.UPDATE_CALENDAR"));
    }

    private void setListItems(Context context, int resource, String date) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        ListView classesList = findViewById(R.id.listView);

        List<Cours> cours = databaseHandler.getCours(date);
        ListAdapter customAdapter = new ListAdapter(getApplicationContext(), resource, cours);
        classesList.setAdapter(customAdapter);
    }

    @Override
    protected void onDestroy() {
        if(updateReceiver != null) {
            unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }
        super.onDestroy();
    }
}
