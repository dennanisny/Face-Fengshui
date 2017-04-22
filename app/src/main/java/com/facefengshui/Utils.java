package com.facefengshui;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by haiyangyu on 15/4/17.
 */

public class Utils {
    public static Bitmap retrievePhotoByUri(ContentResolver resolver, Uri photoUri) throws Exception {
        InputStream photoStream = resolver.openInputStream(photoUri);
        Options options = new Options();

        // Read the size of the photo
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(photoStream, null, options);

        // Determine the scale ratio
        options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(options.outWidth / 1920f, options.outHeight / 1080f)));
        options.inJustDecodeBounds = false;

        // PhotoStream can't be reused...
        photoStream.close();
        photoStream = resolver.openInputStream(photoUri);

        // Actually decoding the photo based on scale ratio
        return BitmapFactory.decodeStream(photoStream, null, options);
    }
}
