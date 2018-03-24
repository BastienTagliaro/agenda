package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences.OnSharedPreferenceChangeListener preferencesChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Log.d("SettingsActivity", "Key : " + key);
                    if(key.equals("sync_frequency") || key.equals("ical_file")) {
                        Log.d("SettingsActivity", "Sending intent as preferences have changed");
                        Intent intent = new Intent("com.tagliaro.monclin.urca.RESET_ALARM");
                        intent.setClass(getApplicationContext(), AlarmSetter.class);
                        sendBroadcast(intent);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChanged);
//        checkValues();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Load the Preferences from the XML file
            addPreferencesFromResource(R.xml.app_preferences);
        }
    }

/*    private void checkValues() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String syncFrequency = preferences.getString("sync_frequency", "30");
        String url = preferences.getString("ical_file", null);
        boolean enableNotifications = preferences.getBoolean("enable_reminders", true);
        String remindBefore = preferences.getString("reminders_before", "15");

        String msg = "Cur values: ";
        msg += "\n frequency = " + syncFrequency;
        msg += "\n url = " + url;
        msg += "\n enableNotifications = " + enableNotifications;
        msg += "\n remindBefore = " + remindBefore;

        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }*/
}
