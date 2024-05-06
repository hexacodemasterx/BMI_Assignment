package com.example.assignment;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBCon extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bmicalc.db";
    private static final int DATABASE_VERSION = 1;

    public DBCon(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS user_table (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date TEXT, bmi REAL)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS user_table (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date DATE, bmi REAL)";
        db.execSQL(createTableQuery);
    }
}
