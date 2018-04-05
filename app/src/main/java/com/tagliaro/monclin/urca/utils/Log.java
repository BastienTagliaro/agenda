package com.tagliaro.monclin.urca.utils;

import android.os.Environment;

import com.tagliaro.monclin.urca.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class Log {
    static final boolean LOG = true;
    private static File file = new File(Environment.getExternalStorageDirectory() + File.separator + BuildConfig.APPLICATION_ID + File.separator + "debug.log");

    public static void i(String tag, String string) {
        if (LOG) android.util.Log.i(tag, string);
    }
    public static void e(String tag, String string) {
        if (LOG) android.util.Log.e(tag, string);
    }
    public static void d(String tag, String string) {
        if (LOG) {
            android.util.Log.d(tag, string);
            try {
                write(file, tag, string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void v(String tag, String string) {
        if (LOG) android.util.Log.v(tag, string);
    }
    public static void w(String tag, String string) {
        if (LOG) android.util.Log.w(tag, string);
    }

    private static void write(File file, String tag, String content) throws IOException {
        if(file.createNewFile() || file.exists()) {
            FileOutputStream outputStream = new FileOutputStream(file, true);
            Calendar calendar = Calendar.getInstance();
            String date = calendar.getTime().toString();
            String concat = "["  + date + "] " + tag + " : " + content + "\n";

//            System.out.println("concat = " + concat);

            try {
                outputStream.write(concat.getBytes());
            } finally {
                outputStream.close();
            }
        }
    }
}
