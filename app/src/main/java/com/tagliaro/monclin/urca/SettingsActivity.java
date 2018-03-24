package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    SharedPreferences.OnSharedPreferenceChangeListener preferencesChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Log.d(TAG, "Key : " + key);
                    if(key.equals("sync_frequency")) {
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), AlarmSetter.class);
                        intent.setAction("com.tagliaro.monclin.urca.SET_SYNC");
                        sendBroadcast(intent);
                    }
                    if(key.equals("ical_file")) {
                        Log.d(TAG, "Sending intent as preferences have changed");
                        Intent syncIntent = new Intent(getApplicationContext(), SyncService.class);
                        SyncService.enqueueWork(getApplicationContext(), syncIntent);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        SettingsFragment settingsFragment = SettingsFragment.newInstance(sharedPreferences.getString("lastUpdate", null));

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChanged);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        public static SettingsFragment newInstance(String arg) {
            SettingsFragment sf = new SettingsFragment();
            Bundle args = new Bundle();
            args.putString("lastUpdate", arg);
            sf.setArguments(args);
            return sf;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            String lastUpdate = getArguments().getString("lastUpdate");
            Preference lastSync = findPreference("last_sync");

            lastSync.setSummary(String.format(getResources().getString(R.string.last_sync), lastUpdate));

            return super.onCreateView(inflater, container, savedInstanceState);
        }

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
