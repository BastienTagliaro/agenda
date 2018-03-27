package com.tagliaro.monclin.urca.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "ClassesDB";

    private static final String NAME_TABLE = "classes";
    private static final String KEY = "id";
    private static final String CLASS_NAME = "className";
    private static final String CLASSROOM = "classroom";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "date";
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";

    private static final String CREATE_TABLE = "CREATE TABLE " + NAME_TABLE + " (" + KEY +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + CLASS_NAME
            + " TEXT, " + CLASSROOM + " TEXT," + DESCRIPTION + " TEXT, " + DATE + " TEXT, "
            + START_TIME + " TEXT, " + END_TIME + " TEXT);";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + NAME_TABLE + ";";

    private SQLiteDatabase mDb = null;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHandler(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    private SQLiteDatabase open() {
        mDb = this.getWritableDatabase();
        return mDb;
    }

    private void delete() {
        mDb.execSQL(DROP_TABLE);
    }

    public void doTable() {
        SQLiteDatabase db = this.open();
        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    public void close() {
        mDb.close();
    }

    public SQLiteDatabase getDb() {
        return mDb;
    }

    public void add(Classes c) {
        ContentValues value = new ContentValues();
        value.put(CLASS_NAME, c.getClassname());
        value.put(CLASSROOM, c.getClassroom());
        value.put(DESCRIPTION, c.getDescription());
        value.put(DATE, c.getDate());
        value.put(START_TIME, c.getStartTime());
        value.put(END_TIME, c.getEndTime());

        SQLiteDatabase db = this.open();
        db.insert(NAME_TABLE, null, value);
        db.close();
    }

    public List<Classes> getClass(String date) {
        List<Classes> classesList = new ArrayList<>();
        SQLiteDatabase db = this.open();

        Cursor cursor = db.query(NAME_TABLE, new String[] { KEY, CLASS_NAME, CLASSROOM, DESCRIPTION, DATE,
                START_TIME, END_TIME}, DATE + "=?", new String[] {date }, null, null, START_TIME);

        if (cursor.moveToFirst()) {
            do {
                Classes classes = new Classes(Integer.parseInt(cursor.getString(0)), cursor.getString(1)
                        , cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6));
                classesList.add(classes);
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return classesList;
    }

    public Classes getClass(Long id) {
        SQLiteDatabase db = this.open();
        Classes classes = null;

        Cursor cursor = db.query(NAME_TABLE,
                new String[] { KEY, CLASS_NAME, CLASSROOM, DESCRIPTION, DATE, START_TIME, END_TIME},
                KEY + "=?", new String[] { Long.toString(id) },
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            classes = new Classes(Integer.parseInt(cursor.getString(0)), cursor.getString(1)
                        , cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6));
        }

        cursor.close();
        db.close();
        return classes;
    }
}