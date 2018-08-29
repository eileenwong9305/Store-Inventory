package com.example.android.storeinventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Eileen on 6/11/2017.
 */

public class Utils {

    private Utils() {
    }

    /**
     * Get bitmap from uri to display image in imageview
     * Credit to: https://github.com/crlsndrsjmnz/MyShareImageExample
     *
     * @param context
     * @param photoUri       uri of photo
     * @param photoImageView photo imageView in layout
     * @return
     */
    public static Bitmap getPhotoBitmap(Context context, Uri photoUri, ImageView photoImageView) {
        if (photoUri == null || photoUri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetW = photoImageView.getWidth();
        int targetH = photoImageView.getHeight();

        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(photoUri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bmOptions);
            if (inputStream != null) {
                inputStream.close();
            }

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            inputStream = context.getContentResolver().openInputStream(photoUri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
            if (inputStream != null) {
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    // Set photo to photoimageview. If no photo saved in database, use default photo.
    public static void setPhoto(final Context context, final ImageView photoImageView, final Uri photoUri) {
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
                    photoImageView.setImageBitmap(Utils.getPhotoBitmap(context, photoUri, photoImageView));
                }
            }

        });
    }
}
