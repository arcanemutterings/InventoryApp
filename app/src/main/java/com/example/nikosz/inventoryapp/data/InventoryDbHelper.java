package com.example.nikosz.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.nikosz.inventoryapp.data.ItemContract.ItemEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "InventoryDbHelper";

    public static final String DATABASE_NAME = "inventory.db";
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_CREATION =
            "CREATE TABLE " + ItemEntry.TABLE_NAME +
                    " (" + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ItemEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    ItemEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                    ItemEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 1, " +
                    ItemEntry.COLUMN_SUPPLIER_NAME + " TEXT, " +
                    ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(TAG, "onCreate: database creation: " + DATABASE_CREATION);
        sqLiteDatabase.execSQL(DATABASE_CREATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
