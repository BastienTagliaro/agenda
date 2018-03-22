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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
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
                    // Reset tables and their content since we now have the new calendar content
                    databaseHandler.doTable();

                    Pattern date = Pattern.compile("([0-9]{4})([0-9]{2})([0-9]{2})T([0-9]{2})([0-9]{2})([0-9]{2})");
                    List<Cours> cours = new ArrayList<>();
                    int index = 0;

                    for(Iterator i = calendar.getComponents().iterator(); i.hasNext(); index++) {
                        Component component = (Component) i.next();
                        cours.add(new Cours());

                        for(Iterator j = component.getProperties().iterator(); j.hasNext();) {
                            Cours currentCours = cours.get(index);
                            Property property = (Property) j.next();

                            switch(property.getName()) {
                                case "DTSTART":
                                case "DTEND":
                                    Matcher m = date.matcher(property.getValue());

                                    if(m.matches()) {
                                        String year = m.group(1);
                                        String month = m.group(2);
                                        String day = m.group(3);
                                        String hours = m.group(4);
                                        String minutes = m.group(5);

                                        currentCours.setDate(day + "-" + month + "-" + year);

                                        if(property.getName().equals("DTSTART"))
                                            currentCours.setHeureDebut(hours + ":" + minutes);
                                        else
                                            currentCours.setHeureFin(hours + ":" + minutes);
                                    }
                                    break;

                                case "SUMMARY":
                                    currentCours.setNomCours(property.getValue());
                                    break;
                                case "DESCRIPTION":
                                    currentCours.setDescription(property.getValue());
                                    break;
                                case "LOCATION":
                                    currentCours.setSalle(property.getValue());
                                    break;
                            }
                        }
                    }

                    for(int i = 0; i < cours.size(); ++i) {
                        Cours currentCours = cours.get(i);
                        databaseHandler.ajouter(new Cours(currentCours.getNomCours(), currentCours.getSalle(), currentCours.getDescription(),
                                currentCours.getDate(), currentCours.getHeureDebut(), currentCours.getHeureFin()));
                    }


                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.FRANCE);
                    String lastUpdate = dateFormat.format(java.util.Calendar.getInstance().getTime());

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lastUpdate", lastUpdate);
                    editor.apply();

                    Intent updateIntent = new Intent("urca.UPDATE_CALENDAR");
                    sendBroadcast(updateIntent);
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
