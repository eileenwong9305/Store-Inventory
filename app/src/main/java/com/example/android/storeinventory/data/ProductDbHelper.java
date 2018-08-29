package com.example.android.storeinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.storeinventory.data.ProductContract.ProductEntry;

/**
 * Created by Eileen on 4/11/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    public static final String DATABASE_NAME = "store.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_WEIGHT + " REAL NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL DEFAULT 0.0, " +
                ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_EMAIL + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODCUT_PHONE + " TEXT, " +
                ProductEntry.COLUMN_PRODUCT_PHOTO + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
