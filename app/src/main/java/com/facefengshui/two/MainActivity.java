package com.facefengshui.two;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

import com.facefengshui.two.R;
import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true; 
	}

	public void onClickTake(View v) {
		Intent intent = new Intent(this, TakePickPhotoActivity.class);
		startActivity(intent);
	}
	
	public void onClickAccredition(View v) {
		Intent intent = new Intent(this, AccreditionActivity.class);
		startActivity(intent);
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

}
