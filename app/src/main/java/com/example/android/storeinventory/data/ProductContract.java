package com.example.android.storeinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Eileen on 4/11/2017.
 */

public final class ProductContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.storeinventory";
    /**
     * Base of all URI's which apps will use to contact the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCT = "products";

    private ProductContract() {
    }

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static class ProductEntry implements BaseColumns {
        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * Name of database table for product
         */
        public static final String TABLE_NAME = "products";

        /**
         * Column in table
         */
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_WEIGHT = "weight";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public static final String COLUMN_PRODUCT_EMAIL = "email";
        public static final String COLUMN_PRODCUT_PHONE = "phone";
        public static final String COLUMN_PRODUCT_PHOTO = "photo";

        /**
         * Maximum and minimum quantity of product
         */
        public static final int MIN_QUANTITY = 0;
        public static final int MAX_QUANTITY = 10000;
    }
}
