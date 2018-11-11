package com.example.nikosz.inventoryapp;

import android.support.v4.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikosz.inventoryapp.data.ItemContract.ItemEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EditorActivity";

    private Uri mCurrentItemUri;

    // fields
    TextView mProductNameTextView;
    TextView mProductPriceTextView;
    TextView mProductQuantityTexView;
    TextView mSupplierNameTextView;
    TextView mSupplierPhoneTextView;

    public static final int EXISTING_LOADER = 0;

    // Checking if the Item was changed or can be discarded
    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mCurrentItemUri = getIntent().getData();

        // Check if it's a new item or one is being edited
        if (mCurrentItemUri == null) {
            setTitle(R.string.editor_activity_title_add);
        } else {
            setTitle(R.string.editor_activity_title_edit);
            getSupportLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        // Find views
        mProductNameTextView = findViewById(R.id.edit_product_name);
        mProductPriceTextView = findViewById(R.id.edit_product_price);
        mProductQuantityTexView = findViewById(R.id.edit_product_quantity);
        mSupplierNameTextView = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneTextView = findViewById(R.id.edit_supplier_phone);

        // Setting the on touch listener to check if field was changed
        mProductNameTextView.setOnTouchListener(mTouchListener);
        mProductPriceTextView.setOnTouchListener(mTouchListener);
        mProductQuantityTexView.setOnTouchListener(mTouchListener);
        mSupplierNameTextView.setOnTouchListener(mTouchListener);
        mSupplierPhoneTextView.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // if it's a new item, remove delete button
        if (mCurrentItemUri == null) {
            MenuItem delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mItemHasChanged) {
                    finish();
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

            case R.id.action_save:
                if (saveItem()) {
                    finish();
                    return true;
                }
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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

    // return true if saving is successful
    private boolean saveItem() {
        ContentValues values = new ContentValues();

        String productNameString = mProductNameTextView.getText().toString().trim();
        String productPriceString = mProductPriceTextView.getText().toString().trim();
        String productQuantityString = mProductQuantityTexView.getText().toString().trim();
        String supplierNameString = mSupplierNameTextView.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneTextView.getText().toString().trim();

        // Check for empty fields
        if (TextUtils.isEmpty(productNameString)
                || TextUtils.isEmpty(productPriceString)
                || TextUtils.isEmpty(productQuantityString)
                || TextUtils.isEmpty(supplierNameString)
                || TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(this, R.string.all_fields_must_be_filled_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        int quantity = Integer.parseInt(productQuantityString);
        double price = Double.parseDouble(productPriceString);

        values.put(ItemEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ItemEntry.COLUMN_PRICE, price);
        values.put(ItemEntry.COLUMN_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);

        // add the DB if it's a new item
        if (mCurrentItemUri == null) {
            Uri newRowUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
            if (newRowUri == null) {
                Toast.makeText(this, R.string.error_saving_item_msg, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(this, R.string.successful_saving_item_msg, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        // update DB if it's an existing item
        else {
            int numOfRowsUpdated = getContentResolver().update(mCurrentItemUri, values, null, null);
            if (numOfRowsUpdated > 0) {
                Toast.makeText(this, R.string.successful_update_msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_update_msg, Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_item_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            data.moveToFirst();
            String productName = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_PRODUCT_NAME));
            String productPrice = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE));
            String productQuantity = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_QUANTITY));
            String supplierName = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_SUPPLIER_NAME));
            String supplierPhone = data.getString(data.getColumnIndexOrThrow(ItemEntry.COLUMN_SUPPLIER_PHONE_NUMBER));

            mProductNameTextView.setText(productName);
            mProductPriceTextView.setText(productPrice);
            mProductQuantityTexView.setText(productQuantity);
            mSupplierNameTextView.setText(supplierName);
            mSupplierPhoneTextView.setText(supplierPhone);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameTextView.setText("");
        mProductPriceTextView.setText("");
        mProductQuantityTexView.setText("");
        mSupplierNameTextView.setText("");
        mSupplierPhoneTextView.setText("");
    }
}
