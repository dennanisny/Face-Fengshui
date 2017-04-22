package com.facefengshui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.Response;
import com.megvii.cloud.mylibrary.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class FaceppDetect {
    // Log tag
    private final String TAG = "FaceppDetect";

    // Api key and secret
    private final String api_key = BuildConfig.FACEPP_API_KEY;
    private final String api_secret = BuildConfig.FACEPP_API_SECRET;

    private byte[] buff;

    private DetectCallback callback = null;

    void setDetectCallback(DetectCallback callback)
    {
        this.callback = callback;
    }

    FaceppDetect(Bitmap img)
    {
        if (img != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            float scale = Math.min(1, Math.min(600f/img.getWidth(), 600f/img.getHeight()));
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
            imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            buff = stream.toByteArray();

            // CLose ByteArrayOutputStream to avoid memory leak
            try {
                stream.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Erro when initiating FaceppDetect", e);
            }
        }
    }

    void detect()
    {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = null;

                        try{
                            CommonOperate commonOperate = new CommonOperate(api_key, api_secret, false);
                            Response response = commonOperate.detectByte(buff, 1, null);

                            String content = new String(response.getContent());
                            obj = new JSONObject(content);
                            if (!obj.has("faces")) {
                                throw new Exception("No face detected");
                            }

                            if (callback != null)
                            {
                                callback.PaintResult(obj);
                            }
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Error during face detect", e);
                        }
                    }
                }
        ).start();
    }

    interface DetectCallback
    {
        void PaintResult(JSONObject obj) throws JSONException;
    }
}
