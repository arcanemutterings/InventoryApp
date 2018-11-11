package com.example.nikosz.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikosz.inventoryapp.data.ItemContract.ItemEntry;

import java.text.NumberFormat;
import java.util.Locale;

public class ItemCursorAdapter extends CursorAdapter {
    private static final String TAG = "ItemCursorAdapter";

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.item_name);
        TextView quantityTextView = view.findViewById(R.id.quantity_of_items);
        TextView priceTextView = view.findViewById(R.id.item_price);

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_PRODUCT_NAME));
        Double priceDouble = cursor.getDouble(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE));
        final Integer quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_QUANTITY));

        String price = getPrice(priceDouble, quantity);

        nameTextView.setText(name);
        priceTextView.setText(price);
        quantityTextView.setText(quantity.toString());

        Button sellButton = view.findViewById(R.id.sell_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity == 0) {
                    Toast.makeText(context, R.string.less_than_zero_toast, Toast.LENGTH_SHORT).show();
                } else {

                    Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_QUANTITY, quantity - 1);

                    context.getContentResolver().update(currentItemUri, values, null, null);
                }
            }
        });
    }

    private String getPrice(double priceDouble, int quantity) {
        double totalPrice = priceDouble * quantity;
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return "$" + nf.format(totalPrice) + " ($" + nf.format(priceDouble) + "/item)";
    }
}
