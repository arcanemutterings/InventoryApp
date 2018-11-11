package com.example.nikosz.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.nikosz.inventoryapp.data.ItemContract.ItemEntry;

public class ItemProvider extends ContentProvider {
    private static final String TAG = "ItemProvider";

    public static final int ITEMS = 100;
    public static final int ITEM_ID = 101;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Uknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        // validation of values
        String productName = values.getAsString(ItemEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) throw new IllegalArgumentException("Item requires a name");

        Double price = values.getAsDouble(ItemEntry.COLUMN_PRICE);
        if (price == null || price <= 0) throw new IllegalArgumentException("Price has to be greater than 0");

        Integer quantity = values.getAsInteger(ItemEntry.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0) throw new IllegalArgumentException("Quantity has to be greater or equal to 0");

        String supplierName = values.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) throw new IllegalArgumentException("Supplier requires a name");

        String supplierPhone = values.getAsString(ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (supplierPhone == null) throw new IllegalArgumentException("Supplier requires a phone number");

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for + " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) return 0;

        // Validate values
        if (values.containsKey(ItemEntry.COLUMN_PRODUCT_NAME)){
            String productName = values.getAsString(ItemEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) throw new IllegalArgumentException("Item requires a name");
        }

        if (values.containsKey(ItemEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(ItemEntry.COLUMN_PRICE);
            if (price == null || price <= 0)
                throw new IllegalArgumentException("Price has to be greater than 0");
        }

        if (values.containsKey(ItemEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ItemEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0)
                throw new IllegalArgumentException("Quantity has to be greater or equal to 0");
        }

        if (values.containsKey(ItemEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null)
                throw new IllegalArgumentException("Supplier requires a name");
        }

        if (values.containsKey(ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhone = values.getAsString(ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (supplierPhone == null)
                throw new IllegalArgumentException("Supplier requires a phone number");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
