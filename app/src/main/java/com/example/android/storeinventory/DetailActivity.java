package com.example.android.storeinventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.ProductContract.ProductEntry;

/**
 * Displays detail of product stored in the app.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER_ID = 1;

    private TextView nameTextView;
    private TextView weightTextView;
    private TextView priceTextView;
    private TextView quantityTextView;
    private TextView supplierTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private ImageView photoImageView;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri currentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity to obtain uri of
        currentProductUri = getIntent().getData();

        // Find all relevant views
        nameTextView = (TextView) findViewById(R.id.product_name_detail_text);
        weightTextView = (TextView) findViewById(R.id.weight_detail_text);
        priceTextView = (TextView) findViewById(R.id.price_detail_text);
        quantityTextView = (TextView) findViewById(R.id.quantity_detail_text);
        supplierTextView = (TextView) findViewById(R.id.supplier_name_detail_text);
        emailTextView = (TextView) findViewById(R.id.supplier_email_detail_text);
        phoneTextView = (TextView) findViewById(R.id.supplier_phone_detail_text);
        photoImageView = (ImageView) findViewById(R.id.photo_detail_image);
        Button decreaseButton = (Button) findViewById(R.id.decrease_button);
        Button increaseButton = (Button) findViewById(R.id.increase_button);
        ImageButton emailIcon = (ImageButton) findViewById(R.id.email_image);
        ImageButton phoneIcon = (ImageButton) findViewById(R.id.phone_image);

        // Setup listener to the decrease button to decrease quantity of product
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(quantityTextView.getText().toString().trim());
                // Show toast message if quantity is less than 0
                if (quantity == ProductEntry.MIN_QUANTITY) {
                    Toast.makeText(DetailActivity.this, R.string.not_less_than_0,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Decrease quantity and update the database
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    getContentResolver().update(currentProductUri, values, null, null);
                    quantityTextView.setText(Integer.toString(quantity));
                }
            }
        });

        // Setup listener to the increase button to increase quantity of product
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(quantityTextView.getText().toString().trim());
                // Show toast message if quantity is greater than 10000
                if (quantity == ProductEntry.MAX_QUANTITY) {
                    Toast.makeText(DetailActivity.this, R.string.not_more_than_10000,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Increase quantity and update the database
                    quantity++;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    getContentResolver().update(currentProductUri, values, null, null);
                    quantityTextView.setText(Integer.toString(quantity));
                }
            }
        });

        // Setup listener to open email app with stored supplier's email address
        emailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = emailTextView.getText().toString().trim();
                String productName = nameTextView.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + address));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_for, productName));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // Setup listener to open phone app with the stored phone number
        phoneIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneTextView.getText().toString().trim();
                if(!TextUtils.isEmpty(phone)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(DetailActivity.this, R.string.no_phone_number,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize a loader to read the product data from the database
        // and display the current values in the detail
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_product_success, Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Edit Product" menu option
            case R.id.edit_product:
                // Open editor
                Intent editIntent = new Intent(DetailActivity.this, EditorActivity.class);
                editIntent.setData(currentProductUri);
                startActivity(editIntent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete_product:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that contains all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_WEIGHT,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_EMAIL,
                ProductEntry.COLUMN_PRODCUT_PHONE,
                ProductEntry.COLUMN_PRODUCT_PHOTO
        };
        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        if (data.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int weightColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_WEIGHT);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int emailColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_EMAIL);
            int phoneColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODCUT_PHONE);
            int photoColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PHOTO);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            Double weight = data.getDouble(weightColumnIndex);
            Double price = data.getDouble(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String email = data.getString(emailColumnIndex);
            String phone = data.getString(phoneColumnIndex);
            String photoString = data.getString(photoColumnIndex);
            final Uri photoUri = Uri.parse(photoString);

            // Update the views on the screen with the values from the database
            nameTextView.setText(name);
            weightTextView.setText(getString(R.string.weight_unit, String.format("%.3f", weight)));
            priceTextView.setText(getString(R.string.rm_price, String.format("%.2f", price)));
            quantityTextView.setText(Integer.toString(quantity));
            supplierTextView.setText(supplier);
            emailTextView.setText(email);
            phoneTextView.setText(phone);

            ViewTreeObserver viewTreeObserver = photoImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        photoImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        photoImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    if (photoUri == null) {
                        photoImageView.setImageResource(R.drawable.no_image_available);
                    } else {
                        photoImageView.setImageBitmap(Utils.getPhotoBitmap(DetailActivity.this,
                                photoUri, photoImageView));
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameTextView.setText("");
        weightTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        supplierTextView.setText("");
        emailTextView.setText("");
        phoneTextView.setText("");
        photoImageView.setImageResource(R.drawable.no_image_available);
    }
}
