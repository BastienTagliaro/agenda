package com.tagliaro.monclin.urca;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Boolean downloaded = false;
        System.out.println("Service started");

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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("Service stopped");
    }

    private static class DownloadTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            int count;
            Boolean res = false;
            String path = params[1];

            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                File file = new File(path);
                if(file.createNewFile() || file.exists()) {
                    System.out.println("doInBackground | Downloading file");
                    InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream outputStream = new FileOutputStream(path);

                    byte data[] = new byte[1024];

                    while((count = inputStream.read(data)) != -1) {
                        outputStream.write(data, 0, count);
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                    res = true;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return res;
        }
    }
}
