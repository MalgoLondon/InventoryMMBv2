package com.example.android.inventorymmbv2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventorymmbv2.PhoneContract.PhoneEntry;

/**
 * {@link ContentProvider} for Phone inventory app.
 */
public class PhoneProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PhoneProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the phones table
     */
    private static final int PHONES = 100;

    /**
     * URI matcher code for the content URI for a single phone in the phone table
     */
    private static final int PHONE_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES, PHONES);
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES + "/#", PHONE_ID);
    }

    /**
     * Database helper that will provide us access to the database
     */
    private DbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                // For the PHONES code, query the phones table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the phone table.
                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PHONE_ID:
                // For the PHONE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventorymmbv2/phones/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the phones table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PhoneContract.PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return insertPhone(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a phone into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPhone(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Phone requires a name");
        }

        // Check that the supplier is valid
        Integer supplier = values.getAsInteger(PhoneEntry.COLUMN_SUPPLIER);
        if (supplier == null || !PhoneEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException("Phone requires valid supplier");
        }

        // Check that the supplier phone number is not null
        String number = values.getAsString(PhoneEntry.COLUMN_SUPPLIER_NUMBER);
        if (number == null) {
            throw new IllegalArgumentException("Phone number of supplier is required");
        }

        // If the price is provided, check that it's greater than or equal to 0 €
        Integer price = values.getAsInteger(PhoneEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Phone requires valid price");
        }

        // If the quantity is provided, check that it's greater than or equal to 0  units
        Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Stock number needs to be valid");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new phone with the given values
        long id = database.insert(PhoneEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the phones content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return updatePhone(uri, contentValues, selection, selectionArgs);
            case PHONE_ID:
                // For the PHONE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePhone(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update phones in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more phones).
     * Return the number of rows that were successfully updated.
     */
    private int updatePhone(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PhoneEntry#COLUMN_PHONE_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PhoneEntry.COLUMN_PHONE_NAME)) {
            String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Phone requires a name");
            }
        }

        // If the {@link PhoneEntry#COLUMN_SUPPLIER} key is present,
        // check that the supplier value is valid.
        if (values.containsKey(PhoneEntry.COLUMN_SUPPLIER)) {
            Integer supplier = values.getAsInteger(PhoneEntry.COLUMN_SUPPLIER);
            if (supplier == null || !PhoneEntry.isValidSupplier(supplier)) {
                throw new IllegalArgumentException("Phone requires valid supplier");
            }
        }

        // If the {@link PhoneEntry#COLUMN_SUPPLIER_NUMBER} key is present,
        // check that the name value is not null.
        if (values.containsKey(PhoneEntry.COLUMN_SUPPLIER_NUMBER)) {
            String number = values.getAsString(PhoneEntry.COLUMN_SUPPLIER_NUMBER);
            if (number == null) {
                throw new IllegalArgumentException("Supplier requires a phone number");
            }
        }

        // If the {@link PhoneEntry#COLUMN_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(PhoneEntry.COLUMN_PRICE)) {
            // Check that the price is greater than or equal to 0 €
            Integer price = values.getAsInteger(PhoneEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Phone requires valid price");
            }
        }
        // If the {@link PhoneEntry#COLUMN_QUANTITY} key is present,
        // check that the price value is valid.
        if (values.containsKey(PhoneEntry.COLUMN_QUANTITY)) {
            // Check that the price is greater than or equal to 0 €
            // If the quantity is provided, check that it's greater than or equal to 0  units
            Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Stock number needs to be valid");
            }
        }
            // If there are no values to update, then don't try to update the database
            if (values.size() == 0) {
                return 0;
            }
            // Otherwise, get writeable database to update the data
            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            // Perform the update on the database and get the number of rows affected
            int rowsUpdated = database.update(PhoneEntry.TABLE_NAME, values, selection, selectionArgs);

            // If 1 or more rows were updated, then notify all listeners that the data at the
            // given URI has changed
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            // Return the number of rows updated
            return rowsUpdated;
        }

        @Override
        public int delete (Uri uri, String selection, String[]selectionArgs){
            // Get writeable database
            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            // Track the number of rows that were deleted
            int rowsDeleted;
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PHONES:
                    // Delete all rows that match the selection and selection args
                    rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case PHONE_ID:
                    // Delete a single row given by the ID in the URI
                    selection = PhoneEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Deletion is not supported for " + uri);
            }

            // If 1 or more rows were deleted, then notify all listeners that the data at the
            // given URI has changed
            if (rowsDeleted != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            // Return the number of rows deleted
            return rowsDeleted;
        }

        @Override
        public String getType (Uri uri){
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PHONES:
                    return PhoneEntry.CONTENT_LIST_TYPE;
                case PHONE_ID:
                    return PhoneEntry.CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
            }
        }
    }