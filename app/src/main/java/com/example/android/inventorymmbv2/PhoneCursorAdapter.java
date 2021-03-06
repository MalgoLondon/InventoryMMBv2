package com.example.android.inventorymmbv2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link PhoneCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of phone data in the {@link Cursor}.
 */
public class PhoneCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PhoneCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PhoneCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the phone data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current phone can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name_tv);
        TextView priceTextView = view.findViewById(R.id.price_tv);
        final TextView quantityTextView = view.findViewById(R.id.in_stock_tv);
        ImageView cartImageView = view.findViewById(R.id.cart_view);
        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PHONE_NAME);
        int priceColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry._ID);

        // Read the pet attributes from the Cursor for the current phone
        String phoneName = cursor.getString(nameColumnIndex);
        String phonePrice = cursor.getString(priceColumnIndex);
        String phoneQuantity = cursor.getString(quantityColumnIndex);
        final int phoneId = cursor.getInt(idColumnIndex);

        // Update the TextViews with the attributes for the current phone
        nameTextView.setText(phoneName);
        priceTextView.setText("Price: " + phonePrice);
        quantityTextView.setText(phoneQuantity);

        cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stock = Integer.valueOf(quantityTextView.getText().toString());
                stock = stock - 1;

                // Create a ContentValues object where column names are the keys,
                // and phone attributes from the editor are the values.
                ContentValues values = new ContentValues();
                values.put(PhoneContract.PhoneEntry.COLUMN_QUANTITY, stock);

                Uri currentPhoneUri = ContentUris.withAppendedId(PhoneContract.PhoneEntry.CONTENT_URI, phoneId);
                context.getContentResolver().update(currentPhoneUri, values, null, null);
                Log.d("malgo", "the id is " + phoneId);
            }
        });
    }
}