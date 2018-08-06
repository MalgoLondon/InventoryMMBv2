package com.example.android.inventorymmbv2;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventorymmbv2.PhoneContract.PhoneEntry;

/**
 * Database helper for Pets app. Manages database creation and version management.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "smartphones.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link DbHelper}.
     *
     * @param context of the app
     */
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PHONES_TABLE =  "CREATE TABLE " + PhoneEntry.TABLE_NAME + " ("
                + PhoneEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PhoneEntry.COLUMN_PHONE_NAME + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + PhoneEntry.COLUMN_SUPPLIER + " TEXT NOT NULL DEFAULT 0, "
                + PhoneEntry.COLUMN_SUPPLIER_NUMBER + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_QUANTITY + " INTEGER NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PHONES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
