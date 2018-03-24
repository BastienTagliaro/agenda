package com.tagliaro.monclin.urca;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver updateReceiver;
    private String currentDate;
    CalendarView calendarView;
    ListView classesList;
    private final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        classesList = findViewById(R.id.listView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        else {
            process();
        }
    }

    private Boolean createFolder(String name) {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + name);
        Boolean success = true;

        if(!folder.exists())
            success = folder.mkdirs();

        return success;
    }

    private void process() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        currentDate = dateFormat.format(calendarView.getDate());

        if(createFolder(getPackageName())) {
            setListItems(this, R.layout.classes_view, currentDate);

            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                    month += 1;
                    currentDate = String.format(Locale.FRANCE, "%02d", day) + "-" + String.format(Locale.FRANCE, "%02d", month) + "-" + Integer.toString(year);
                    setListItems(getApplicationContext(), R.layout.classes_view, currentDate);
                }
            });

            classesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Long classID = (Long) view.getTag();

                    Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                    intent.putExtra("id", classID);
                    startActivity(intent);
                }
            });

            updateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("MainActivity","Received update broadcast, refreshing");

                    setListItems(context, R.layout.classes_view, currentDate);
                }
            };

            // This is just to make sure the AlarmSetter is always running the Alarm
            Intent intent = new Intent("com.tagliaro.monclin.urca.RESET_ALARM");
            intent.setClass(this, AlarmSetter.class);
            sendBroadcast(intent);

            registerReceiver(updateReceiver, new IntentFilter("urca.UPDATE_CALENDAR"));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == STORAGE_PERMISSION_CODE) {
            for(int i = 0; i < permissions.length; ++i) {
                String permission = permissions[i];

                if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);

                    if(!showRationale) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                    }
                    else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                        showRationale(R.string.permission_storage_required);
                    }
                }
                else {
                    process();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showRationale(int resource) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(resource).setTitle(R.string.permission_denied);

        builder.setPositiveButton(R.string.retry_permission, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermissions();
                    }
                });

        builder.setNegativeButton(R.string.im_sure_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finishAndRemoveTask();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, STORAGE_PERMISSION_CODE);
    }

    private void setListItems(Context context, int resource, String date) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        ListView classesList = findViewById(R.id.listView);

        List<Cours> cours = databaseHandler.getCours(date);
        ClassesListAdapter customAdapter = new ClassesListAdapter(getApplicationContext(), resource, cours);
        classesList.setAdapter(customAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pref, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_pref:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
