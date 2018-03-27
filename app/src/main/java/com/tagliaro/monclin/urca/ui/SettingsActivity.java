package com.tagliaro.monclin.urca.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tagliaro.monclin.urca.background.SyncSetter;
import com.tagliaro.monclin.urca.R;
import com.tagliaro.monclin.urca.background.SyncService;

//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceFragmentCompat;
//import android.support.v7.preference.PreferenceManager;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private final String TAG = getClass().getSimpleName();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    SharedPreferences.OnSharedPreferenceChangeListener preferencesChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Log.d(TAG, "Key : " + key);
                    if(key.equals("sync_frequency")) {
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), SyncSetter.class);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        SettingsFragment settingsFragment = SettingsFragment.newInstance(sharedPreferences.getString("lastUpdate", null));

        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChanged);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        public static SettingsFragment newInstance(String arg) {
            SettingsFragment sf = new SettingsFragment();
            Bundle args = new Bundle();
            args.putString("lastUpdate", arg);
            sf.setArguments(args);
            return sf;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_preferences);

            String lastUpdate = getArguments().getString("lastUpdate");
            Preference lastSync = findPreference("last_sync");

            lastSync.setSummary(String.format(getResources().getString(R.string.last_sync), lastUpdate));

            Preference btn = findPreference("force_sync_btn");
            btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d("SettingsFragment", "Sending intent as user forced sync");
                    Intent syncIntent = new Intent(getActivity().getApplicationContext(), SyncService.class);
                    SyncService.enqueueWork(getActivity().getApplicationContext(), syncIntent);

                    return true;
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if(Build.VERSION.SDK_INT > 21) {
                findPreference("sync_frequency").setIcon(R.drawable.ic_sync_black_24dp);
                findPreference("ical_file").setIcon(R.drawable.ic_link_black_24dp);
                findPreference("enable_reminders").setIcon(R.drawable.ic_notifications_black_24dp);
                findPreference("reminders_before").setIcon(R.drawable.ic_alarm_black_24dp);
                findPreference("force_sync_btn").setIcon(R.drawable.ic_force_sync_black_24dp);
            }

            return super.onCreateView(inflater, container, savedInstanceState);
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
