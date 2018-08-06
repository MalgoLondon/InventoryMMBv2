package com.example.android.inventorymmbv2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymmbv2.PhoneContract.PhoneEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the pet data loader */
    private static final int PHONE_LOADER = 0;

    /** Adapter for the ListView */
    PhoneCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the phone data
        ListView phoneListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        phoneListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new PhoneCursorAdapter(this, null);
        phoneListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        phoneListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific phone that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PhoneEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPhoneUri = ContentUris.withAppendedId(PhoneEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPhoneUri);

                // Launch the {@link EditorActivity} to display the data for the current phone.
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(PHONE_LOADER, null, this);
    }

    /**
     * This method is called when the cart button is clicked.
     */

    public void decrementCart(View view) {
        TextView mStockDisplayView =(TextView) findViewById(R.id.in_stock_tv);
        int stock = Integer.valueOf(mStockDisplayView.getText().toString());
        if (stock==0){
            Toast.makeText(this, "Sorry, it looks like we are out of stock. Please try later", Toast.LENGTH_SHORT).show();
            return;}

        stock = stock - 1;
        displayQuantityAfterBuy(stock);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPhone() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_NAME, "Xperia XZ2");
        values.put(PhoneEntry.COLUMN_PRICE, 500);
        values.put(PhoneEntry.COLUMN_SUPPLIER, PhoneEntry.SUPPLIER_SONY);
        values.put(PhoneEntry.COLUMN_SUPPLIER_NUMBER, "079123456");
        values.put(PhoneEntry.COLUMN_QUANTITY, 50);

        // Insert a new row for Xperia XZ2 into the provider using the ContentResolver.
        // Use the {@link PhoneEntry#CONTENT_URI} to indicate that we want to insert
        // into the phone database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
       Uri newUri = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);
   }

    /**
     * Helper method to delete all phones in the database.
     */
    private void deleteAllPhones() {
        int rowsDeleted = getContentResolver().delete(PhoneEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from phone database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
               insertPhone();
             return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPhones();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_NAME,
                PhoneEntry.COLUMN_PRICE,
                PhoneEntry.COLUMN_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PhoneEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PhoneCursorAdapter} with this new cursor containing updated phone data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
    /**
     * This method displays the given quantity value on the screen after incrementing or decrementing.
     */
    private void displayQuantityAfterBuy(int number) {
        TextView quantityTextView = (TextView) findViewById(R.id.in_stock_tv);
        quantityTextView.setText("" + number);
    }
}