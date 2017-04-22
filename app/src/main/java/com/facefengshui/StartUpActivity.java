package com.facefengshui;

import com.facefengshui.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class StartUpActivity extends Activity {
	EditText editTxtAge;
	EditText editTxtOccupation;
	RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_start_up);
		
		editTxtAge = (EditText) findViewById(R.id.editTxtAge);
		editTxtOccupation = (EditText) findViewById(R.id.editTxtOccupation);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinnerEthnic);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.strarray_ethnic, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_up, menu);
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
	
	public void onClickSkip(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	public void onClickSubmit(View v) {
		if (radioGroup.getCheckedRadioButtonId() == -1) {
			Toast.makeText(getApplicationContext(), "Please select your gender", Toast.LENGTH_SHORT).show();
		}
		else if (isEditTextEmpty(editTxtAge)) {
			Toast.makeText(getApplicationContext(), "Please enter your age", Toast.LENGTH_SHORT).show();
		}
		else if (isEditTextEmpty(editTxtOccupation)) {
			Toast.makeText(getApplicationContext(), "Please enter your occupation", Toast.LENGTH_SHORT).show();
		}
		else {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}
	
	public boolean isEditTextEmpty(EditText editTxt) {
		if (editTxt.getText().toString().trim().length() > 0)
			return false;
		else
			return true;
	}

}
