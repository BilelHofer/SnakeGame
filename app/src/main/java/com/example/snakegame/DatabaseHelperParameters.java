package com.example.snakegame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

public class DatabaseHelperParameters extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "snakegameparameters.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "parameter";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SPEED_UP = "speed_up";
    public static final String COLUMN_NUM_APPLES = "num_apples";

    public DatabaseHelperParameters(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SPEED_UP + " REAL NOT NULL,"
                + COLUMN_NUM_APPLES + " INTEGER NOT NULL"
                + ")";
        db.execSQL(createTableQuery);

        // Insérer une première ligne avec des valeurs par défaut
        ContentValues values = new ContentValues();
        values.put(COLUMN_SPEED_UP, 0.5f);
        values.put(COLUMN_NUM_APPLES, 1);
        db.insert(TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public Pair<Float, Integer> getParameter() {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COLUMN_SPEED_UP + ", " + COLUMN_NUM_APPLES + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        Float speedUp = null;
        Integer numApples = null;

        if (cursor != null && cursor.moveToFirst()) {
            speedUp = cursor.getFloat(0);
            numApples = cursor.getInt(1);
        }

        cursor.close();
        db.close();

        return new Pair<>(speedUp, numApples);
    }

    public boolean updateParameter(float acceleration, int numApples) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SPEED_UP, acceleration);
        values.put(COLUMN_NUM_APPLES, numApples);

        int rowsUpdated = db.update(TABLE_NAME, values, COLUMN_ID + " = 1", null);

        db.close();

        return rowsUpdated > 0;
    }
}
