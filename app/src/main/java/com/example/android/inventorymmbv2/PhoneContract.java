package com.example.android.inventorymmbv2;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

//**API Contract for the Inventory app.//
public final class PhoneContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PhoneContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.inventorymmbv2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PHONES = "phones";

    /**
     * Inner class that defines constant values for the phones database table.
     * Each entry in the table represents a single phone.
     */
    public static final class PhoneEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of phones.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single phone.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /** The content URI to access the phone data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PHONES);

        /** Name of database table for phones */
        public final static String TABLE_NAME = "phones";

        /**
         * Unique ID number for the phone (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the phone model.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PHONE_NAME ="name";

        /**
         * Price of the phone.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRICE = "price";

        /**
         * Supplier of the phone.
         *
         * The only possible values are {@link #SUPPLIER_UNKNOWN}, {@link #SUPPLIER_APPLE,},
         * or {@link #SUPPLIER_SONY} or {@link #SUPPLIER_SAMSUNG}or {@link #SUPPLIER_HUAWEI}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_SUPPLIER = "supplier";

        /**
         * Supplier number.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NUMBER = "number";

        /**
         * Quantity in stock.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_QUANTITY = "quantity";


        /**
         * Possible values for the supplier of the phone.
         */
        public static final int SUPPLIER_UNKNOWN = 0;
        public static final int SUPPLIER_APPLE = 1;
        public static final int SUPPLIER_SONY = 2;
        public static final int SUPPLIER_HUAWEI = 3;
        public static final int SUPPLIER_SAMSUNG = 4;

        /**
         * Returns whether or not the given supplier is {@link #SUPPLIER_UNKNOWN}, {@link #SUPPLIER_APPLE,},
         * or {@link #SUPPLIER_SONY} or {@link #SUPPLIER_SAMSUNG}or {@link #SUPPLIER_HUAWEI}.
         */
        public static boolean isValidSupplier(int supplier) {
            return supplier == SUPPLIER_UNKNOWN || supplier == SUPPLIER_APPLE || supplier == SUPPLIER_SONY || supplier == SUPPLIER_HUAWEI || supplier == SUPPLIER_SAMSUNG;
        }
    }

}
