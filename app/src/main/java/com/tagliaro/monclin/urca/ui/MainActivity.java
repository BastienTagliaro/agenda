package com.tagliaro.monclin.urca.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import com.tagliaro.monclin.urca.R;
import com.tagliaro.monclin.urca.background.NotifySetter;
import com.tagliaro.monclin.urca.background.SyncSetter;
import com.tagliaro.monclin.urca.utils.Classes;
import com.tagliaro.monclin.urca.utils.DatabaseHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
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

        if(Build.VERSION.SDK_INT <= 22) {
            calendarView.setLayoutParams(new ConstraintLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels, 320));
        }

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
        Log.d(TAG, "process() called!");
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = new Intent();
        intent.setClass(this, SyncSetter.class);
        intent.setAction("com.tagliaro.monclin.urca.SET_SYNC");
        sendBroadcast(intent);

        Intent intent1 = new Intent();
        intent1.setClass(this, NotifySetter.class);
        intent1.setAction("com.tagliaro.monclin.urca.SET_NOTIFY");
        sendBroadcast(intent1);

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
        }
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received update broadcast, refreshing");

            setListItems(context, R.layout.classes_view, currentDate);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter("urca.UPDATE_CALENDAR"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("urca.UPDATE_CALENDAR"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
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

        List<Classes> cours = databaseHandler.getClass(date);
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
                Intent pref = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(pref);
                return true;
//            case R.id.menu_add:
//                Intent add = new Intent(getApplicationContext(), NewEventActivity.class);
//                startActivity(add);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
