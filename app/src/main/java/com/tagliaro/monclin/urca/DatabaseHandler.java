package com.tagliaro.monclin.urca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "CoursDB";

    private static final String NAME_TABLE = "cours";
    private static final String KEY = "id";
    private static final String NOMCOURS = "nomCours";
    private static final String SALLE = "salle";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "date";
    private static final String HEUREDEBUT = "heureDebut";
    private static final String HEUREFIN = "heureFin";

    private static final String CREATE_TABLE = "CREATE TABLE " + NAME_TABLE + " (" + KEY +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOMCOURS
            + " TEXT, " + SALLE + " TEXT," + DESCRIPTION + " TEXT, " + DATE + " TEXT, "
            + HEUREDEBUT + " TEXT, " + HEUREFIN + " TEXT);";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + NAME_TABLE + ";";

    private SQLiteDatabase mDb = null;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    public void ajouter(Cours c) {
        ContentValues value = new ContentValues();
        value.put(NOMCOURS, c.getNomCours());
        value.put(SALLE, c.getSalle());
        value.put(DESCRIPTION, c.getDescription());
        value.put(DATE, c.getDate());
        value.put(HEUREDEBUT, c.getHeureDebut());
        value.put(HEUREFIN, c.getHeureFin());

        SQLiteDatabase db = this.open();
        db.insert(NAME_TABLE, null, value);
        db.close();
    }

    public List<Cours> getCours(String date) {
        List<Cours> coursList = new ArrayList<>();
        SQLiteDatabase db = this.open();

        Cursor cursor = db.query(NAME_TABLE, new String[] { KEY, NOMCOURS, SALLE, DESCRIPTION, DATE,
        HEUREDEBUT, HEUREFIN}, DATE + "=?", new String[] {date }, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Cours cours = new Cours(Integer.parseInt(cursor.getString(0)), cursor.getString(1)
                        , cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6));
                coursList.add(cours);
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return coursList;
    }

    /*
    public List<Cours> getAllCours() {
        List<Cours> coursList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + NAME_TABLE;

        SQLiteDatabase db = this.open();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Cours cours = new Cours();
                cours.setId(Integer.parseInt(cursor.getString(0)));
                cours.setNomCours(cursor.getString(1));
                cours.setSalle(cursor.getString(2));
                cours.setDescription(cursor.getString(3));
                cours.setDate(cursor.getString(4));
                cours.setHeureDebut(cursor.getString(5));
                cours.setHeureFin(cursor.getString(6));

                coursList.add(cours);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return coursList;
    }


    public int getCoursCount() {
        String query = "SELECT * FROM " + NAME_TABLE;
        SQLiteDatabase db = this.open();
        Cursor cursor = db.rawQuery(query, null);
        cursor.close();

        return cursor.getCount();
    }

    public void updateCours(Cours c) {
        SQLiteDatabase db = this.open();

        ContentValues values = new ContentValues();
        values.put(NOMCOURS, c.getNomCours());
        values.put(SALLE, c.getSalle());
        values.put(DESCRIPTION, c.getDescription());
        values.put(DATE, c.getDate());
        values.put(HEUREDEBUT, c.getHeureDebut());
        values.put(HEUREFIN, c.getHeureFin());

        db.update(NAME_TABLE, values, KEY + " = ?", new String[] {String.valueOf(c.getId())});
        db.close();
    }

    public void deleteCours(Cours c) {
        SQLiteDatabase db = this.open();

        db.delete(NAME_TABLE, KEY + " = ?",
                new String[] { String.valueOf(c.getId())});
        db.close();
    }

    */
}