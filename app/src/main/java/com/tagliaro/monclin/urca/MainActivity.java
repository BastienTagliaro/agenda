package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Temporary, this will be up to the user to choose in settings
        editor.putString("agenda", "http://agenda.univ-reims.fr/ical.php?cle=33fc8e511b003f20a2a9cbbb3c5eec2e");
        editor.apply();

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + getPackageName());
        Boolean success = true;

        if(!folder.exists())
            success = folder.mkdirs();

        if(success) {
            System.out.println("Created directory");
            startService(new Intent(this, DownloadService.class));
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, DownloadService.class)); // TEMPORARY, REMOVE LATER

        super.onDestroy();
    }
}
