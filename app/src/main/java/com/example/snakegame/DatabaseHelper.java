package com.example.snakegame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "snakegame.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "scores";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_APPLES = "apples";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_APPLES + " INTEGER NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Ajoute un score dans la base de données
     * @param name Nom du joueur
     * @param apples Nombre de pomme mangées
     */
    public void addScore(String name, int apples) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_APPLES, apples);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /**
     * Retourne un curseur contenant tous les scores triés par ordre décroissant des pommes mangées
     * @return Cursor contenant tous les scores
     */
    public Cursor getAllScores() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_APPLES + " DESC";
        return db.rawQuery(query, null);
    }

    /**
     * Supprime la base de données et la recrée
     */
    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
