package com.example.android.storeinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.ProductContract.ProductEntry;

/**
 * {@link ProductCursorAdapter} is an adapter for a list view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
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
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name_text);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_ID);

        // Read the pet attributes from the Cursor for the current pet
        String name = cursor.getString(nameColumnIndex);
        String price = String.format("%.2f", cursor.getDouble(priceColumnIndex));
        String quantityString = Integer.toString(cursor.getInt(quantityColumnIndex));
        final int id = cursor.getInt(idColumnIndex);

        nameTextView.setText(name);
        priceTextView.setText(context.getString(R.string.rm_price, price));
        quantityTextView.setText(quantityString);

        ImageView salesIcon = (ImageView) view.findViewById(R.id.sales_button);
        salesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(quantityTextView.getText().toString().trim());
                if (quantity == ProductEntry.MIN_QUANTITY) {
                    Toast.makeText(context, R.string.not_less_than_0,
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                    context.getContentResolver().update(currentProductUri, values, null, null);
                    quantityTextView.setText(Integer.toString(quantity));
                }
            }
        });
    }
}
