package com.example.android.inventorymmbv2;

import android.view.View;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymmbv2.PhoneContract.PhoneEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the pet data loader */
    private static final int EXISTING_PHONE_LOADER = 0;

    /** Content URI for the existing pet (null if it's a new phone) */
    private Uri mCurrentPhoneUri;

    /** EditText field to enter the phone name */
    private EditText mNameEditText;

    /** EditText field to enter the phone price */
    private EditText mPriceEditText;

    /** EditText field to enter the supplier number */
    private EditText mNumberEditText;

    /** EditText field to enter the stock quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mSupplierSpinner;

    /**
     * The possible valid values are in the PhoneContract.java file.
     */
    private int mSupplier = PhoneEntry.SUPPLIER_UNKNOWN;

    /** Boolean flag that keeps track of whether the phone has been edited (true) or not (false) */
    private boolean mPhoneHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPhoneHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPhoneHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        // If the intent DOES NOT contain a phone content URI, then we know that we are
        // creating a new phone.
        if (mCurrentPhoneUri == null) {
            // This is a new phone, so change the app bar to say "Add a Phone"
            setTitle(getString(R.string.editor_activity_title_new_phone));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a phone that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing phone, so change app bar to say "Edit Phone"
            setTitle(getString(R.string.editor_activity_title_edit_phone));

            // Initialize a loader to read the phone data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PHONE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_phone_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mNumberEditText = (EditText) findViewById(R.id.edit_number);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mNumberEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }


    /**
     * This method is called when the plus button is clicked.
     */

    public void increment(View view) {
        int stock = Integer.valueOf(mQuantityEditText.getText().toString());
        if (stock>10000)
        {
            Toast.makeText(this, "You can't have stock more than 10000", Toast.LENGTH_SHORT).show();
            return;}

        stock = stock + 1;
        displayQuantity(stock);}
    /**
     * This method is called when the minus button is clicked.
     */

    public void decrement(View view) {
        int stock = Integer.valueOf(mQuantityEditText.getText().toString());
        if (stock<0){
            Toast.makeText(this, "You can't have stock less than 0", Toast.LENGTH_SHORT).show();
            return;}

        stock = stock - 1;
        displayQuantity(stock);
    }

    /**
     * This method is called when the phone button is clicked.
     */
    public void call(View view) {
        String callNumber = mNumberEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" +callNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_apple))) {
                        mSupplier = PhoneEntry.SUPPLIER_APPLE;
                    } else if (selection.equals(getString(R.string.supplier_sony))) {
                        mSupplier = PhoneEntry.SUPPLIER_SONY;
                    } else if (selection.equals(getString(R.string.supplier_samsung))) {
                        mSupplier = PhoneEntry.SUPPLIER_SAMSUNG;
                    } else if (selection.equals(getString(R.string.supplier_huawei))) {
                        mSupplier = PhoneEntry.SUPPLIER_HUAWEI;
                    } else {
                        mSupplier = PhoneEntry.SUPPLIER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = PhoneEntry.SUPPLIER_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save phone into database.
     */
    private void savePhone() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String numberString = mNumberEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentPhoneUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(numberString) && TextUtils.isEmpty(quantityString) && mSupplier == PhoneEntry.SUPPLIER_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new phone.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_NAME, nameString);
        values.put(PhoneEntry.COLUMN_PRICE, priceString);
        values.put(PhoneEntry.COLUMN_SUPPLIER, mSupplier);
        values.put(PhoneEntry.COLUMN_SUPPLIER_NUMBER, numberString);
        values.put(PhoneEntry.COLUMN_QUANTITY, quantityString);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        //int weight = 0;
        //if (!TextUtils.isEmpty(weightString)) {
          //  weight = Integer.parseInt(weightString);
        //}
        //values.put(PhoneEntry.COLUMN_PET_WEIGHT, weight);

        // Determine if this is a new or existing pet by checking if mCurrentPhoneUri is null or not
        if (mCurrentPhoneUri == null) {
            // This is a NEW phone, so insert a new phone into the provider,
            // returning the content URI for the new phone.
            Uri newUri = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPhoneUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPhoneUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                savePhone();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPhoneHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPhoneHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_NAME,
                PhoneEntry.COLUMN_PRICE,
                PhoneEntry.COLUMN_SUPPLIER,
                PhoneEntry.COLUMN_SUPPLIER_NUMBER,
                PhoneEntry.COLUMN_QUANTITY };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentPhoneUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of phone attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
            int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_SUPPLIER);
            int numberColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_SUPPLIER_NUMBER);
            int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            String number = cursor.getString(numberColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mNumberEditText.setText(number);
            mQuantityEditText.setText(Integer.toString(quantity));

            // Supplier is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Apple, 2 is Sony etc.).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (supplier) {
                case PhoneEntry.SUPPLIER_APPLE:
                    mSupplierSpinner.setSelection(1);
                    break;
                case PhoneEntry.SUPPLIER_SONY:
                    mSupplierSpinner.setSelection(2);
                    break;
                case PhoneEntry.SUPPLIER_SAMSUNG:
                    mSupplierSpinner.setSelection(3);
                    break;
                case PhoneEntry.SUPPLIER_HUAWEI:
                    mSupplierSpinner.setSelection(4);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mNumberEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierSpinner.setSelection(0); // Select "Unknown"
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this phone.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the phone.
                deletePhone();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the phone in the database.
     */
    private void deletePhone() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentPhoneUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPhoneUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * This method displays the given quantity value on the screen after incrementing or decrementing.
     */
    private void displayQuantity(int number) {
        TextView quantityTextView = (TextView) findViewById(R.id.edit_quantity);
        quantityTextView.setText("" + number);
    }
}
