package com.example.nikosz.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikosz.inventoryapp.data.ItemContract.ItemEntry;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "DetailsActivity";

    Uri mCurrentItemUri;

    // fields
    TextView mProductNameTextView;
    TextView mProductPriceTextView;
    TextView mProductQuantityTexView;
    TextView mSupplierNameTextView;
    TextView mSupplierPhoneTextView;

    public static final int EXISTING_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setTitle(R.string.details);

        mCurrentItemUri = getIntent().getData();

        // Find views
        mProductNameTextView = findViewById(R.id.product_name_textview);
        mProductPriceTextView = findViewById(R.id.product_price_textview);
        mProductQuantityTexView = findViewById(R.id.product_quantity_textview);
        mSupplierNameTextView = findViewById(R.id.supplier_name_textview);
        mSupplierPhoneTextView = findViewById(R.id.supplier_phone_textview);


        getSupportLoaderManager().initLoader(EXISTING_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(mCurrentItemUri);
                startActivity(intent);
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_item_dialog);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "onClick: delete clicked");
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int numOfDeletedItems = getContentResolver().delete(mCurrentItemUri, null, null);
            if (numOfDeletedItems > 0) {
                Toast.makeText(this, R.string.succesful_delete_msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.delete_error_msg, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_PRODUCT_NAME,
                ItemEntry.COLUMN_PRICE,
                ItemEntry.COLUMN_QUANTITY,
                ItemEntry.COLUMN_SUPPLIER_NAME,
                ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };
        return new android.support.v4.content.CursorLoader(
                this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            data.moveToFirst();
            final int productId = data.getInt(data.getColumnIndexOrThrow(ItemEntry._ID));
            String productName = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_PRODUCT_NAME));
            String productPrice = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE));
            String productQuantity = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_QUANTITY));
            String supplierName = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_SUPPLIER_NAME));
            final String supplierPhone = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER));
            final int quantity = Integer.parseInt(productQuantity);

            mProductNameTextView.setText(productName);
            mProductPriceTextView.setText(getPrice(productPrice));
            mProductQuantityTexView.setText(productQuantity);
            mSupplierNameTextView.setText(supplierName);
            mSupplierPhoneTextView.setText(supplierPhone);

            Button plusButton = findViewById(R.id.plus_button);
            Button minusButton = findViewById(R.id.minus_button);
            Button callButton = findViewById(R.id.call_button);

            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity == 0) {
                        Toast.makeText(DetailsActivity.this, R.string.less_than_zero_toast, Toast.LENGTH_SHORT).show();
                    } else {

                        Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, productId);

                        ContentValues values = new ContentValues();
                        values.put(ItemEntry.COLUMN_QUANTITY, quantity - 1);

                        getContentResolver().update(currentItemUri, values, null, null);
                    }
                }
            });

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, productId);

                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_QUANTITY, quantity + 1);

                    getContentResolver().update(currentItemUri, values, null, null);

                }
            });

            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + supplierPhone));
                    startActivity(intent);
                }
            });
        }

    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private String getPrice(String priceString) {

        double price = Double.parseDouble(priceString);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return "$" + nf.format(price) + "/item";
    }
}
