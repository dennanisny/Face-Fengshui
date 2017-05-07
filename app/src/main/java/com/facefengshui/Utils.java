package com.facefengshui;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;

/**
 * Created by haiyangyu on 15/4/17.
 */

public class Utils {
    final static String TAG = "Utils";
    final static float downsizeDimensionTarget = 1000f;

    public static Bitmap retrievePhotoByUri(ContentResolver resolver, Uri photoUri) throws Exception {
        InputStream photoStream = resolver.openInputStream(photoUri);
        Options options = new Options();

        // Read the size of the photo
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(photoStream, null, options);
        Log.d(TAG, MessageFormat.format("Image size retrieved. Width: {0}, Height: {1}", options.outWidth, options.outHeight));

        // Determine by how much the image should be subsampled
        int downsizeRatio = (int) Math.ceil(Math.max(options.outWidth/downsizeDimensionTarget, options.outHeight/downsizeDimensionTarget));
        options.inSampleSize = downsizeRatio;
        options.inJustDecodeBounds = false;
        Log.d(TAG, MessageFormat.format("Image to be subsampled by: {0}", options.inSampleSize));

        // PhotoStream can't be reused, hence closing and starting a new one
        photoStream.close();
        photoStream = resolver.openInputStream(photoUri);

        // Actually decoding the photo based on sample size
        return BitmapFactory.decodeStream(photoStream, null, options);
    }
}
