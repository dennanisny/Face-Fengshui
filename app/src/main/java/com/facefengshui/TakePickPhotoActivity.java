package com.facefengshui;

import android.app.Activity;
import android.app.ProgressDialog;
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

import com.facefengshui.FaceppDetect.DetectCallback;
import com.facefengshui.FaceppDetect.ErrorCallback;
import com.google.analytics.tracking.android.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.regions.Region;

public class TakePickPhotoActivity extends Activity {
	private final String TAG = "TakePickPhotoActivity";

	// Activity request codes
	final int REQUEST_TAKE_PHOTO = 100;
	final int REQUEST_PICK_PHOTO = 101;

	long startTime;
	FaceFengshuiResult faceFengshuiResult = null;

	// File url to store image
	Uri photoUri;
	Bitmap img;
	
	ImageView imageView;
	Button btnTakePhoto;
	Button btnDetect;
	Button btnResult;
	TextView txtViewStatus;
	TextView txtViewFaceDetectTime;
	TextView txtViewTotalTime;

	ProgressDialog dialog;

	CognitoCachingCredentialsProvider credentialsProvider;
	AmazonDynamoDBClient ddbClient;
	DynamoDBMapper mapper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_take_pick_photo);

		imageView = (ImageView) findViewById(R.id.imageView);
		// In the case of activity recreation on screen rotation, restore image preview
		if (img != null) {
			imageView.setImageBitmap(img);
		}

		btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
		btnDetect = (Button) findViewById(R.id.btnDetect);
		btnDetect.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				FaceppDetect faceppDetect = new FaceppDetect(img);
				faceppDetect.setDetectCallback(detectCallback);
				faceppDetect.setErrorCallback(errorCallback);
				faceppDetect.detect();

				// Display progress dialog
				dialog.show();

				startTime = System.currentTimeMillis();
				txtViewStatus.setText("Face detection started.");
				txtViewStatus.setTextColor(Color.BLACK);
			}
		});
		btnResult = (Button) findViewById(R.id.btnResult);

		txtViewStatus = (TextView) findViewById(R.id.txtViewStatus);
		txtViewFaceDetectTime = (TextView) findViewById(R.id.txtViewFaceDetectTime);
		txtViewTotalTime = (TextView) findViewById(R.id.txtViewTotalTime);

		// Disable Take Photo button if the device doesn't have a camera
		if (!doesDeviceSupportCamera()) {
			btnTakePhoto.setEnabled(false);
			txtViewStatus.setText("Please use Pick Photo button to select a photo from Gallery to perform face fengshui.");
			Toast.makeText(this,
					"Your phone doesn't have a camera. Please refer to instructions below to use Pick Photo button to perform face fengshui.",
					Toast.LENGTH_SHORT).show();
		}

		// Initialize a non-cancellable Progress Dialog for face detection
		// Prevent users from submitted multiple face detection requests at the same time
		// This is confusing especially when not all of them succeeded
		dialog = new ProgressDialog(this);
		dialog.setTitle("Face Detection Progress Dialog");
		dialog.setMessage("Face detection in progress...");

		dialog.setCancelable(false);

		// Setup Dynamo DB client for saving Face Fengshui results to cloud
		credentialsProvider = new CognitoCachingCredentialsProvider(
				getApplicationContext(),    /* get the context for the application */
				BuildConfig.IDENTITY_POOL_ID,    /* Identity Pool ID */
				Regions.AP_NORTHEAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
		);
		ddbClient = new AmazonDynamoDBClient(credentialsProvider);
		ddbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1)); // Dynamo DB is set to Region Singapore
		mapper = new DynamoDBMapper(ddbClient);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
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
		// Take a photo
		Intent intentTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Create the File where the photo should be stored
		File photoFile;
		try {
			photoFile = createImageFile();
		}
		catch (IOException e) {
			txtViewStatus.setText("Error when creating path of image file. Please restart app and try again.");
			txtViewStatus.setTextColor(Color.RED);
			return;
		}

		if (photoFile != null) {
			Log.d(TAG, "Path to save photo created: " + photoFile.getAbsolutePath());

			photoUri = FileProvider.getUriForFile(
					getApplicationContext(),
					"com.facefengshui.fileprovider",
					photoFile
			);
			Log.d(TAG, "Uri to photo created: " + photoUri.getPath());

			intentTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
			startActivityForResult(intentTakePhoto, REQUEST_TAKE_PHOTO);
		}
	}

	public void onClickPickPhoto(View v) {
		// Get a picture from phone
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_PICK_PHOTO);
	}

	public void onClickResult(View v) {
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra("FaceFengshuiResult", faceFengshuiResult);
		startActivity(intent);
	}

	// Retrieve photo taken/picked by Uri
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			Uri uri = null;

			if (requestCode == REQUEST_TAKE_PHOTO) {
				uri = photoUri;
			}
			else if (requestCode == REQUEST_PICK_PHOTO) {
				uri = intent.getData();
			}

			Log.d(TAG, "Retrieve photo by Uri: " + uri.getPath());

			try {
				// Load photo
				img = Utils.retrievePhotoByUri(getContentResolver(), uri);

				// Update view
				imageView.setImageBitmap(img);
				txtViewStatus.setText("Photo loaded. Click Detect.");
				txtViewStatus.setTextColor(Color.BLACK);
				btnDetect.setVisibility(View.VISIBLE);

				// When users choose a new photo after completing a face reading, Result button should be hidden
				if (btnResult.getVisibility() == View.VISIBLE) {
					btnResult.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {
				Log.e(TAG, "Error when retrieving photo by Uri.", e);
			}
		}
		else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
		}
	}

	ErrorCallback errorCallback = new ErrorCallback() {
		@Override
		public void UpdateErrorMessageInUI(final Exception e) {
			dialog.dismiss();

			TakePickPhotoActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (e instanceof UnknownHostException) {
						txtViewStatus.setText("It seems like face detection failed due to no internet access.");
					}
					else if (e instanceof NoFaceDetectedException) {
						txtViewStatus.setText("No face detected. Please try taking/picking a new photo.");
					}
					else if (e instanceof SocketTimeoutException) {
						txtViewStatus.setText("No response from face detection server before request timeout. Please try again.");
					}
					else {
						txtViewStatus.setText(MessageFormat.format("Error during face detection. Please try again or with a different photo. /n {0}", e.getMessage()));
					}
					txtViewStatus.setTextColor(Color.RED);
				}
			});
		}
	};

	DetectCallback detectCallback = new DetectCallback() {
		public void PaintResult(JSONObject obj, float scale) throws JSONException {
			dialog.dismiss();

			final JSONObject faceObj = obj.getJSONArray("faces").getJSONObject(0);
			final JSONObject landmark = faceObj.getJSONObject("landmark");
			final JSONObject faceRectangle = faceObj.getJSONObject("face_rectangle");

			final String faceDetectionTime = String.format("%.2fs", obj.getDouble("time_used")/1000);
			final String totalTime = String.format(Locale.ENGLISH,"%.2fs", (System.currentTimeMillis() - startTime)/1000f);

			// Set paint for face rectangle
			Paint redPaint = new Paint();
			redPaint.setColor(Color.RED);
			redPaint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight())/200f);
			// Set paint for face features
			Paint bluePaint = new Paint();
			bluePaint.setColor(Color.BLUE);
			bluePaint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight())/200f);

			// Create a canvas to highlight face detection key points
			Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(img, new Matrix(), null);

			try {
				float top, left, width, height;
				float left_eye_center_x, left_eye_center_y, right_eye_center_x, right_eye_center_y;
				float mouth_left_corner_x, mouth_left_corner_y, mouth_right_corner_x, mouth_right_corner_y;
				float mouth_upper_lip_top_x, mouth_upper_lip_top_y, nose_contour_lower_middle_x, nose_contour_lower_middle_y;
				float contour_left1_x, contour_left1_y, contour_right1_x, contour_right1_y;
				float contour_left5_x, contour_left5_y, contour_right5_x, contout_right5_y;

				// Get face rectangle
				top = (float) faceRectangle.getDouble("top")/scale;
				left = (float) faceRectangle.getDouble("left")/scale;
				width = (float) faceRectangle.getDouble("width")/scale;
				height = (float) faceRectangle.getDouble("height")/scale;

				// Get key face detection points
				left_eye_center_x = (float) landmark.getJSONObject("left_eye_center").getDouble("x")/scale;
				left_eye_center_y = (float) landmark.getJSONObject("left_eye_center").getDouble("y")/scale;
				right_eye_center_x = (float) landmark.getJSONObject("right_eye_center").getDouble("x")/scale;
				right_eye_center_y = (float) landmark.getJSONObject("right_eye_center").getDouble("y")/scale;
				mouth_left_corner_x = (float) landmark.getJSONObject("mouth_left_corner").getDouble("x")/scale;
				mouth_left_corner_y = (float) landmark.getJSONObject("mouth_left_corner").getDouble("y")/scale;
				mouth_right_corner_x = (float) landmark.getJSONObject("mouth_right_corner").getDouble("x")/scale;
				mouth_right_corner_y = (float) landmark.getJSONObject("mouth_right_corner").getDouble("y")/scale;
				mouth_upper_lip_top_x = (float) landmark.getJSONObject("mouth_upper_lip_top").getDouble("x")/scale;
				mouth_upper_lip_top_y = (float) landmark.getJSONObject("mouth_upper_lip_top").getDouble("y")/scale;
				nose_contour_lower_middle_x = (float) landmark.getJSONObject("nose_contour_lower_middle").getDouble("x")/scale;
				nose_contour_lower_middle_y = (float) landmark.getJSONObject("nose_contour_lower_middle").getDouble("y")/scale;

				// Get face contour
				contour_left1_x = (float) landmark.getJSONObject("contour_left1").getDouble("x")/scale;
				contour_left1_y = (float) landmark.getJSONObject("contour_left1").getDouble("y")/scale;
				contour_right1_x = (float) landmark.getJSONObject("contour_right1").getDouble("x")/scale;
				contour_right1_y = (float) landmark.getJSONObject("contour_right1").getDouble("y")/scale;
				contour_left5_x = (float) landmark.getJSONObject("contour_left5").getDouble("x")/scale;
				contour_left5_y = (float) landmark.getJSONObject("contour_left5").getDouble("y")/scale;
				contour_right5_x = (float) landmark.getJSONObject("contour_right5").getDouble("x")/scale;
				contout_right5_y = (float) landmark.getJSONObject("contour_right5").getDouble("y")/scale;

				// Mark face rectangle
				canvas.drawLine(left, top, left + width, top, redPaint);
				canvas.drawLine(left + width, top, left + width, top + height, redPaint);
				canvas.drawLine(left + width, top + height, left, top + height, redPaint);
				canvas.drawLine(left, top + height, left, top, redPaint);

				// Mark eye distance
                canvas.drawLine(left_eye_center_x, left_eye_center_y, right_eye_center_x, right_eye_center_y, bluePaint);
				// Mark mouth width
                canvas.drawLine(mouth_left_corner_x, mouth_left_corner_y, mouth_right_corner_x, mouth_right_corner_y, bluePaint);
				// Mark philtrum
				canvas.drawLine(nose_contour_lower_middle_x, nose_contour_lower_middle_y, mouth_upper_lip_top_x, mouth_upper_lip_top_y, bluePaint);

				// Calculate face features
				final double eyeDistance = Math.sqrt(Math.pow((right_eye_center_y - left_eye_center_y), 2) + Math.pow((right_eye_center_x - left_eye_center_x), 2));
				final double faceWidthAtEyeLevel = Math.sqrt(Math.pow((contour_right1_y - contour_left1_y), 2) + Math.pow((contour_right1_x - contour_left1_x), 2));
				final double mouthSize = Math.sqrt(Math.pow((mouth_right_corner_y - mouth_left_corner_y), 2) + Math.pow((mouth_right_corner_x - mouth_left_corner_x), 2));
				final double faceWidthAtMouthLevel = Math.sqrt(Math.pow((contout_right5_y - contour_left5_y), 2) + Math.pow((contour_right5_x - contour_left5_x), 2));
				final double philtrumLength = Math.sqrt(Math.pow((mouth_upper_lip_top_y - nose_contour_lower_middle_y), 2) + Math.pow((mouth_upper_lip_top_x - nose_contour_lower_middle_x), 2));

				final double eyeDistanceRatio = eyeDistance/faceWidthAtEyeLevel * 100;
				final double mouthSizeRatio = mouthSize/faceWidthAtMouthLevel * 100;
				final double philtrumLengthRatio = philtrumLength/height * 100;

				// Save Face Fengshui result to database
				Runnable runnable = new Runnable() {
					public void run() {
						try {
							// Save Face Fengshui result to AWS Dynamo DB
							FaceFengshuiResultItem resultItem = new FaceFengshuiResultItem();
							String result_id = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss.SSS z").format(new Date());
							resultItem.setResultId(result_id);
							resultItem.setEyeDistanceRatio(eyeDistanceRatio);
							resultItem.setMouthSizeRatio(mouthSizeRatio);
							resultItem.setPhiltrumLengthRatio(philtrumLengthRatio);

							mapper.save(resultItem);
						} catch (Exception e) {
							Log.e(TAG, MessageFormat.format("Failed to save Face Fengshui result due to: \n {0}", e.getMessage()));
						}
					}
				};
				Thread saveFaceFengshuiResults = new Thread(runnable);
				saveFaceFengshuiResults.start();

				// Populate Face Fengshui results
				faceFengshuiResult = new FaceFengshuiResult();
				faceFengshuiResult.setEyeDistance(eyeDistanceRatio);
				faceFengshuiResult.setMouthSize(mouthSizeRatio);
				faceFengshuiResult.setPhiltrumLength(philtrumLengthRatio);

				// Save new image
				img = bitmap;

				// Update status
				TakePickPhotoActivity.this.runOnUiThread(new Runnable() {

					public void run() {
						// Display time taken
						txtViewFaceDetectTime.setText(faceDetectionTime);
						txtViewTotalTime.setText(totalTime);

						// Show the image
						imageView.setImageBitmap(img);
						txtViewStatus.setText("Analysis complete. Click Result to discover yourself!");
						btnResult.setVisibility(View.VISIBLE);
					}
				});

			} catch (Exception e) {
				Log.e(TAG, "Error during PaintResult", e);
				TakePickPhotoActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						txtViewStatus.setText("Error while analysing face detection result. Please try again.");
						txtViewStatus.setTextColor(Color.RED);
					}
				});
			}
		}
	};

	// Check whether device has camera hardware
	private boolean doesDeviceSupportCamera() {
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	// Create path to store new photo
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
}
