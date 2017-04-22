package com.facefengshui;

import com.facefengshui.FaceppDetect.DetectCallback;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePickPhotoActivity extends Activity {
	private final String TAG = "TakePickPhotoActivity";

	// Activity request codes
	private static final int REQUEST_TAKE_PHOTO = 100;
	private static final int REQUEST_PICK_PHOTO = 101;

	FaceFengshuiResult faceFengshuiResult = null;
	
	// File url to store image
	private Uri photoUri;
	private Bitmap img = null;
	
	private ImageView imageView;
	private Button btnTakePhoto;
	private Button btnDetect;
	private Button btnResult;
	private TextView txtViewStatus;
	private TextView txtViewFaceDetectTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_take_pick_photo);

		imageView = (ImageView) findViewById(R.id.imageView);

		btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
		btnDetect = (Button) findViewById(R.id.btnDetect);
		btnResult = (Button) findViewById(R.id.btnResult);
		btnResult.setVisibility(View.INVISIBLE);
		
		txtViewStatus = (TextView) findViewById(R.id.txtViewStatus);
		txtViewFaceDetectTime = (TextView) findViewById(R.id.txtViewFaceDetectTime);

		btnDetect.setVisibility(View.INVISIBLE);
		btnDetect.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				FaceppDetect faceppDetect = new FaceppDetect(img);
				faceppDetect.setDetectCallback(detectCallback);
				faceppDetect.detect();

				txtViewStatus.setText("Face detection started.");
			}
		});

		if (!isDeviceSupportCamera()) {
			btnTakePhoto.setEnabled(false);
			txtViewStatus.setText("Please use Pick Photo button to select a photo from Gallery to perform face fengshui.");
			Toast.makeText(getApplicationContext(),
					"Seems like your phone doesn't have a camera. Please use Pick Photo button to perform face fengshui.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.take_photo, menu);
		return true;
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	@Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }

	public void onClickTakePhoto(View v) {
		// capture picture
		Intent intentTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intentTakePhoto.resolveActivity(getPackageManager()) != null)
		{
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException e) {
				// Error occurred while creating the File
				Log.e(TAG, "Error when creating path of image file.", e);
			}
			if (photoFile != null) {
				photoUri = FileProvider.getUriForFile(
						getApplicationContext(),
						"com.facefengshui.fileprovider",
						photoFile
				);
				intentTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
				startActivityForResult(intentTakePhoto, REQUEST_TAKE_PHOTO);
			}
		}
	}

	public void onClickPickPhoto(View v) {
		//get a picture form your phone
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_PICK_PHOTO);
	}

	public void onClickResult(View v) {
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra("FaceFengshuiResult", faceFengshuiResult);
		startActivity(intent);
	}

	/**
	 * Receiving activity faceFengshuiResult method will be called after closing the camera
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// if the faceFengshuiResult is capturing Image
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			try {
				// Load photo
				img = Utils.retrievePhotoByUri(getContentResolver(), photoUri);

				// Update view
				imageView.setImageBitmap(img);
				txtViewStatus.setText("Photo loaded. Click Detect.");
				btnDetect.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				Log.e(TAG, "Error when retrieving photo by Uri.", e);
			}
		}
		else if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK) {
			try {
				// Load photo
				img = Utils.retrievePhotoByUri(getContentResolver(), intent.getData());

				// Update view
				imageView.setImageBitmap(img);
				txtViewStatus.setText("Photo loaded. Click Detect.");
				btnDetect.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				Log.e(TAG, "Error when retrieving photo by Uri.", e);
			}
		}
		else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(),
					"Operation cancelled", Toast.LENGTH_SHORT)
					.show();
		}
		else {
			Toast.makeText(getApplicationContext(),
					"Operation failed", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private DetectCallback detectCallback = new DetectCallback() {
		public void PaintResult(JSONObject obj) throws JSONException {
			final int faceDetectTime = (int) obj.getDouble("time_used");
			final JSONObject faceObj = obj.getJSONArray("faces").getJSONObject(0);
			final JSONObject landmark = faceObj.getJSONObject("landmark");
			final JSONObject faceRectangle = faceObj.getJSONObject("face_rectangle");

			//use the red paint
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight()) / 100f);

			//create a new canvas
			Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(img, new Matrix(), null);

			TakePickPhotoActivity.this.runOnUiThread(new Runnable() {

				public void run() {
					txtViewStatus.setText("Face detection complete. Analysing faceFengshuiResult.");
				}
			});

			try {
				double top, left, width, height;
				double center_x, center_y;
				float left_eye_center_x, left_eye_center_y;
				float right_eye_center_x, right_eye_center_y;
				float mouth_left_corner_x, mouth_left_corner_y;
				float mouth_right_corner_x, mouth_right_corner_y;
				float nose_tip_x, nose_tip_y;

				//get face size
				top = faceRectangle.getDouble("top");
				left = faceRectangle.getDouble("left");
				width = faceRectangle.getDouble("width");
				height = faceRectangle.getDouble("height");

				//get the center point
				center_x = left + width / 2;
				center_y = top + height / 2;

				//get the eye left point
				left_eye_center_x = (float) landmark.getJSONObject("left_eye_center").getDouble("x");
				left_eye_center_y = (float) landmark.getJSONObject("left_eye_center").getDouble("y");

				//get the eye right point
				right_eye_center_x = (float) landmark.getJSONObject("right_eye_center").getDouble("x");
				right_eye_center_y = (float) landmark.getJSONObject("right_eye_center").getDouble("y");

				//get the mouth left point
				mouth_left_corner_x = (float) landmark.getJSONObject("mouth_left_corner").getDouble("x");
				mouth_left_corner_y = (float) landmark.getJSONObject("mouth_left_corner").getDouble("y");

				//get the mouth right point
				mouth_right_corner_x = (float) landmark.getJSONObject("mouth_right_corner").getDouble("x");
				mouth_right_corner_y = (float) landmark.getJSONObject("mouth_right_corner").getDouble("y");

				//get the nose point
                nose_tip_x = (float) landmark.getJSONObject("nose_tip").getDouble("x");
				nose_tip_y = (float) landmark.getJSONObject("nose_tip").getDouble("y");

                canvas.drawPoint(left_eye_center_x, left_eye_center_y, paint);
                canvas.drawPoint(right_eye_center_x, right_eye_center_y, paint);
                canvas.drawPoint(mouth_left_corner_x, mouth_left_corner_y, paint);
                canvas.drawPoint(mouth_right_corner_x, mouth_left_corner_y, paint);
                canvas.drawPoint(nose_tip_x, nose_tip_y, paint);

				//save new image
				img = bitmap;

				// Populate face reading faceFengshuiResult
				final double eyeDistance = (right_eye_center_x - left_eye_center_x) / width * 100;
				final double mouthSize = (mouth_right_corner_x - mouth_left_corner_x) / width * 100;
				final double philtrum = ((mouth_right_corner_y + mouth_left_corner_y) * 0.5 - nose_tip_y) / height * 100;
				final double chinWidth = (center_y + 0.5 * height - 0.5 * (mouth_right_corner_y + mouth_left_corner_y)) / height * 100;
				final double foreheadSize = (0.5 * (left_eye_center_y + right_eye_center_y) - (center_y - 0.5 * height)) / height * 100;

                faceFengshuiResult = new FaceFengshuiResult();

				faceFengshuiResult.setEyeDistance(eyeDistance);
				faceFengshuiResult.setMouthSize(mouthSize);
				faceFengshuiResult.setPhiltrum(philtrum);
				faceFengshuiResult.setChinWidth(chinWidth);
				faceFengshuiResult.setForeheadSize(foreheadSize);

				TakePickPhotoActivity.this.runOnUiThread(new Runnable() {

					public void run() {
						//show the image
						imageView.setImageBitmap(img);
						txtViewStatus.setText("Analysis complete. Click Result to discover yourself!");
						txtViewFaceDetectTime.setText(faceDetectTime + "ms");
						btnResult.setVisibility(View.VISIBLE);
					}
				});

			} catch (Exception e) {
				Log.e(TAG, "Error during PaintResult", e);
				TakePickPhotoActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						txtViewStatus.setText("Error during faceFengshuiResult analysis. Please try again.");
					}
				});
			}
		}
	};
	/**
	 * Checking device has camera hardware or not
	 * */
	private boolean isDeviceSupportCamera() {
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

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
		return image;
	}

	/**
	 * Persist photoUri and image when screen rotates
	 * */
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("photo_uri", photoUri);
        savedInstanceState.putParcelable("image", img);
        savedInstanceState.putSerializable("faceFengshuiResult", faceFengshuiResult);

        super.onSaveInstanceState(savedInstanceState);
    }

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		photoUri = savedInstanceState.getParcelable("photo_uri");
        img = savedInstanceState.getParcelable("image");
        faceFengshuiResult = (FaceFengshuiResult) savedInstanceState.getSerializable("faceFengshuiResult");

		// If image is not null, it means pick/take photo has been performed by user
		// therefore setting the ImageView and show Detect button
        if (img != null) {
            imageView.setImageBitmap(img);
            btnDetect.setVisibility(View.VISIBLE);
        }

        // If faceFengshuiResult is available, it means detect has been performed by user
		// therefore show Result button
        if (faceFengshuiResult != null) {
            btnResult.setVisibility(View.VISIBLE);
        }
	}
}
