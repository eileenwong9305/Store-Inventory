package com.example.android.storeinventory;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.storeinventory.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    /**
     * Identifier for the editor data loader
     */
    private static final int EDITOR_LOADER = 2;
    /**
     * Request code for camera
     */
    private static final int CAMERA_REQUEST_CODE = 1;
    /**
     * Request code for picking image
     */
    private static final int CHOOSE_IMAGE_REQUEST = 2;
    /**
     * Camera permission constant
     */
    private static final int CAMERA_PERMISSION = 100;
    /** Key for saved state*/
    private static final String PHOTO_URI_KEY = "photoURI";
    private static final String HAS_CHANGED_KEY = "hasChanged";

    private static final String FILE_PROVIDER_AUTHORITY= "com.example.android.fileprovider";
    /**
     * Path of photo in phone storage
     */
    private String mCurrentPhotoPath;
    // EditText field
    private EditText nameEditText;
    private EditText weightEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private ImageView photoImageView;
    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri currentProductUri;
    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean productHasChanged = false;
    /**
     * Uri of photo selected
     */
    private Uri photoUri;
    /**
     * Uri of photo taken
     */
    private Uri takePhotoUri;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and change the productHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            productHasChanged = true;
            return false;
        }
    };

    /**
     * Method is used for checking valid email id format.
     * Credit to: https://stackoverflow.com/questions/6119722/how-to-check-edittexts-text-is-email-address-or-not
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    private boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // to figure out if new product is created or editing an existing one is needed
        currentProductUri = getIntent().getData();

        // If the intent DOES NOT contain a product content URI, a new product should be created
        if (currentProductUri == null) {
            // Change app bar to say "Add New Product" if is a new product
            setTitle(R.string.title_add_product);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Change app bar to say "Edit Product" if is an existing product
            setTitle(R.string.title_edit_product);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EDITOR_LOADER, null, this);
        }

        // Find all relevant views to read user input from
        nameEditText = (EditText) findViewById(R.id.product_name_edit);
        weightEditText = (EditText) findViewById(R.id.weight_edit);
        priceEditText = (EditText) findViewById(R.id.price_edit);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit);
        supplierEditText = (EditText) findViewById(R.id.supplier_name_edit);
        emailEditText = (EditText) findViewById(R.id.supplier_email_edit);
        phoneEditText = (EditText) findViewById(R.id.supplier_phone_edit);
        photoImageView = (ImageView) findViewById(R.id.photo_image);
        ImageView takePhotoImageView = (ImageView) findViewById(R.id.take_photo_image);
        ImageView galleryImageView = (ImageView) findViewById(R.id.gallery_image);

        // Setup OnTouchListeners on all the input fields to determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        nameEditText.setOnTouchListener(touchListener);
        weightEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierEditText.setOnTouchListener(touchListener);
        emailEditText.setOnTouchListener(touchListener);
        phoneEditText.setOnTouchListener(touchListener);
        photoImageView.setOnTouchListener(touchListener);

        // Setup take photo icon to open camera app
        takePhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productHasChanged = true;
                checkPermission();
            }
        });

        // Setup gallery icon to open gallery for choosing photo
        galleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productHasChanged = true;
                chooseImage();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(photoUri!=null){
            outState.putString(PHOTO_URI_KEY, photoUri.toString());
            outState.putBoolean(HAS_CHANGED_KEY, productHasChanged);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(!TextUtils.isEmpty(savedInstanceState.getString(PHOTO_URI_KEY))){
            photoUri = Uri.parse(savedInstanceState.getString(PHOTO_URI_KEY));

            // Set photo to photoimageview. If no photo saved in database, use default photo.
            Utils.setPhoto(this, photoImageView, photoUri);
        }
        productHasChanged = savedInstanceState.getBoolean(HAS_CHANGED_KEY);
    }

    /**
     * Check permission to use camera to take photo.
     * Credit to: https://www.androidhive.info/2016/11/android-working-marshmallow-m-runtime-permissions/
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //Show Information about why you need the permission

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION);
            }
        } else {
//                takePhoto();
        }
    }

    /**
     * Open camera app to take photo
     */
    private void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(LOG_TAG, ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePhotoUri = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoUri);
                startActivityForResult(takePhotoIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /**
     * Create image file for photo taken using camera
     * Credit to: https://developer.android.com/training/camera/photobasics.html
     * @return file of image
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Open gallery to choose photo
     * Credit to: https://github.com/crlsndrsjmnz/MyShareImageExample
     */
    private void chooseImage() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE_REQUEST);
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct() {
        // Read from input fields and use trim to eliminate leading or trailing white space
        String name = nameEditText.getText().toString().trim();
        String weightString = weightEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplier = supplierEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Check if this is supposed to be a new product and check if all the fields in the editor
        // are blank
        if (currentProductUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(weightString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplier) && TextUtils.isEmpty(email) &&
                TextUtils.isEmpty(phone) && photoUri == null) {
            finish();
            return;
        }

        // Notify user if the name field is empty
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.product_no_name));
            Toast.makeText(this, R.string.product_no_name,Toast.LENGTH_SHORT).show();
            return;
        }

        // If the weight is not provided by the user, use 0.0 by default.
        Double weight = 0.0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Double.parseDouble(weightString);
        }

        // If the price is not provided by the user, use 0.0 by default.
        Double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        // If the quantity is not provided by the user, use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // If quantity is out of limit, notify user
        if (quantity < ProductEntry.MIN_QUANTITY || quantity > ProductEntry.MAX_QUANTITY) {
            quantityEditText.setError(getString(R.string.quantity_limit));
            Toast.makeText(this, R.string.quantity_limit,Toast.LENGTH_SHORT).show();
            return;
        }

        // Notify user if the supplier's name field is empty
        if (TextUtils.isEmpty(supplier)) {
            supplierEditText.setError(getString(R.string.product_no_supplier));
            Toast.makeText(this, R.string.product_no_supplier,Toast.LENGTH_SHORT).show();
            return;
        }

        // Notify user if the email field is empty
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.product_no_email));
            Toast.makeText(this, R.string.product_no_email,Toast.LENGTH_SHORT).show();
            return;
        }

        // Notify user if the email format is invalid.
        if (!isEmailValid(email)) {
            emailEditText.setError(getString(R.string.invalid_email_format));
            return;
        }

        // Notify user if no photo is attached
        if (photoUri == null) {
            Toast.makeText(this, R.string.product_no_photo, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_WEIGHT, weight);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplier);
        values.put(ProductEntry.COLUMN_PRODUCT_EMAIL, email);
        values.put(ProductEntry.COLUMN_PRODCUT_PHONE, phone);
        values.put(ProductEntry.COLUMN_PRODUCT_PHOTO, photoUri.toString());

        // Determine if this is a new or existing product by checking
        // if currentProductUri is null or not
        if (currentProductUri == null) {
            // insert a new pet into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                Toast.makeText(this, R.string.editor_insert_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_insert_product_success, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update the product with currentProductUri and pass in the new ContentValues.
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.editor_update_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_update_product_success, Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        NavUtils.navigateUpFromSameTask(this);
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
        NavUtils.navigateUpFromSameTask(this);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            }
        } else {
            Toast.makeText(this, R.string.unable_get_permission, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            photoUri = takePhotoUri;
            photoImageView.setImageBitmap(Utils.getPhotoBitmap(this, photoUri, photoImageView));
        } else if (requestCode == CHOOSE_IMAGE_REQUEST) {
            if (data != null) {
                photoUri = data.getData();
                photoImageView.setImageBitmap(Utils.getPhotoBitmap(this, photoUri, photoImageView));
            }
        }
        Toast.makeText(EditorActivity.this, R.string.image_loaded, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product_detail);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                // Save product to database
                saveProduct();
                return true;
            case R.id.delete_product_detail:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // If there are unsaved changes, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that contains all columns from the pet table
        String[] projection = {
                ProductEntry.COLUMN_ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_WEIGHT,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_EMAIL,
                ProductEntry.COLUMN_PRODCUT_PHONE,
                ProductEntry.COLUMN_PRODUCT_PHOTO
        };

        // This loader will execute the ContentProvider's query method on a background thread
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
            // Find the columns of pet attributes that we're interested in
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

            photoUri = Uri.parse(photoString);

            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            weightEditText.setText(String.format("%.3f", weight));
            priceEditText.setText(String.format("%.2f", price));
            quantityEditText.setText(Integer.toString(quantity));
            supplierEditText.setText(supplier);
            emailEditText.setText(email);
            phoneEditText.setText(phone);

            // Set photo to photoimageview. If no photo saved in database, use default photo.
            Utils.setPhoto(this, photoImageView, photoUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.setText("");
        weightEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        photoImageView.setImageResource(R.drawable.no_image_available);
    }
}
