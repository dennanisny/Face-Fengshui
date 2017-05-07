package com.facefengshui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

class FaceppDetect {
    // Log tag
    final String TAG = "FaceppDetect";
    final float downsizeDimensionTarget = 600f;

    // Api key and secret
    final String api_key = BuildConfig.FACEPP_API_KEY;
    final String api_secret = BuildConfig.FACEPP_API_SECRET;

    byte[] buff;
    float scale;

    Thread.UncaughtExceptionHandler handler;
    DetectCallback detectCallback = null;
    ErrorCallback errorCallback = null;

    FaceppDetect(Bitmap img)
    {
        Log.d(TAG, MessageFormat.format("Image size of photo passed to {0}. Width: {1}, Height: {2}.", TAG, img.getWidth(), img.getHeight()));

        this.handler = handler;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scale = Math.min(1, Math.min(downsizeDimensionTarget/img.getWidth(), downsizeDimensionTarget/img.getHeight()));

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
        imgSmall.compress(Bitmap.CompressFormat.PNG, 100, stream);
        buff = stream.toByteArray();

        try {
            // CLose ByteArrayOutputStream to avoid memory leak
            stream.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Error when closing the OutputStream.", e);
        }
    }

    void setDetectCallback(DetectCallback callback) {
        this.detectCallback = callback;
    }

    void setErrorCallback(ErrorCallback callback) {
        this.errorCallback = callback;
    }

    void detect()
    {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj;

                        try{
                            CommonOperate commonOperate = new CommonOperate(api_key, api_secret, false);
                            Response response = commonOperate.detectByte(buff, 1, null);

                            String content = new String(response.getContent());
                            Log.d(TAG, MessageFormat.format("Raw Json response from Face++ server. /n {0}", content));

                            obj = new JSONObject(content);
                            if (!obj.has("faces") || obj.getJSONArray("faces").length() == 0) {
                                throw new NoFaceDetectedException("No face detected.");
                            }

                            if (detectCallback != null) {
                                detectCallback.PaintResult(obj, scale);
                            }
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Error during face detect", e);
                            if (errorCallback != null) {
                                errorCallback.UpdateErrorMessageInUI(e);
                            }
                        }
                    }
                }
        ).start();
    }

    interface DetectCallback {
        void PaintResult(JSONObject obj, float scale) throws JSONException;
    }

    interface ErrorCallback {
        void UpdateErrorMessageInUI(Exception e);
    }
}

class NoFaceDetectedException extends Exception {
    public NoFaceDetectedException(String message) { super(message); }
}
