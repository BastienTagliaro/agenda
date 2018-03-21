package com.tagliaro.monclin.urca;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncService extends JobIntentService {
    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SyncService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Boolean downloaded = false;
        Log.d(getClass().getSimpleName(), "Service is running");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String url = sharedPreferences.getString("agenda", null);
        String path = Environment.getExternalStorageDirectory() + File.separator + getPackageName() + File.separator + "calendar.ics";

        if(url != null) {
            try {
                downloaded = new DownloadTask().execute(url, path).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if(downloaded) {
                System.out.println("onStartCommand | Download finished, continue");
                File file = new File(path);
                FileInputStream fin = null;

                try {
                    fin = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                CalendarBuilder builder = new CalendarBuilder();
                Calendar calendar = null;

                try {
                    calendar = builder.build(fin);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserException e) {
                    e.printStackTrace();
                }

                if (calendar != null) {
                    Pattern date = Pattern.compile("([0-9]{4})([0-9]{2})([0-9]{2})T([0-9]{2})([0-9]{2})([0-9]{2})");
                    List<EventData> events = new ArrayList<>();
                    int index = 0;

                    for(Iterator i = calendar.getComponents().iterator(); i.hasNext(); index++) {
                        Component component = (Component) i.next();
//                    System.out.println("Component [" + component.getName() + "]");

                        events.add(new EventData());

                        for(Iterator j = component.getProperties().iterator(); j.hasNext();) {
                            Property property = (Property) j.next();

                            switch(property.getName()) {
                                case "DTSTART":
                                case "DTEND":
                                    Matcher m = date.matcher(property.getValue());

                                    if(m.matches()) {
//                                    System.out.println(m.group(1) + m.group(2) + m.group(3) + m.group(4) + m.group(5) + m.group(6));
                                        int year = Integer.parseInt(m.group(1));
                                        int month = Integer.parseInt(m.group(2));
                                        int day = Integer.parseInt(m.group(3));
                                        int hours = Integer.parseInt(m.group(4));
                                        int minutes = Integer.parseInt(m.group(5));
                                        int seconds = Integer.parseInt(m.group(6));

                                        String propertyName;

                                        if(property.getName().equals("DTSTART"))
                                            propertyName = "_START";
                                        else
                                            propertyName = "_END";

                                        events.get(index).add(new PropertyData("DAY" + propertyName, Integer.toString(day)));
                                        events.get(index).add(new PropertyData("MONTH" + propertyName, Integer.toString(month)));
                                        events.get(index).add(new PropertyData("YEAR" + propertyName, Integer.toString(year)));
                                        events.get(index).add(new PropertyData("HOURS" + propertyName, Integer.toString(hours)));
                                        events.get(index).add(new PropertyData("MINUTES" + propertyName, Integer.toString(minutes)));
                                        events.get(index).add(new PropertyData("SECONDS" + propertyName, Integer.toString(seconds)));
                                    }
                                    break;

                                case "SUMMARY":
                                case "DESCRIPTION":
                                case "LOCATION":
                                    events.get(index).add(new PropertyData(property.getName(), property.getValue()));
                                    break;
                            }

                            // System.out.println("Property [" + property.getName() + ", " + property.getValue() + "]");
                        }
                    }

                    /*for(int i = 0; i < events.size(); ++i) {
                        System.out.println("Event " + i);
                        for(int j = 0; j < events.get(i).size(); ++j) {
                            System.out.println("Property : " + events.get(i).get(j).getPropertyName() + " ; Value : " + events.get(i).get(j).getPropertyValue());
                        }

                        // Add to database
                    }*/
                }
            }
        }
        else {
            System.out.println("URL doesn't exist");
        }
    }

    @Override
    public void onDestroy() {
        System.out.println("Service is stopping");
        super.onDestroy();
    }

    private static class DownloadTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            int count;
            Boolean res = false;
            String path = params[1];

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection = null;

            try {
                File file = new File(path);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return false;

                if(file.createNewFile() || file.exists()) {
                    System.out.println("doInBackground | Downloading file");
                    inputStream = connection.getInputStream();
                    outputStream = new FileOutputStream(path);

                    byte data[] = new byte[4096];

                    while((count = inputStream.read(data)) != -1) {
                        outputStream.write(data, 0, count);
                    }

                    res = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if(outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    if(inputStream != null)
                        inputStream.close();
                    if(connection != null)
                        connection.disconnect();
                } catch(Exception e) {}
            }

            return res;
        }
    }
}
