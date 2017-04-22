package com.facefengshui;

import com.google.analytics.tracking.android.EasyTracker;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Map;

public class ResultActivity extends Activity {

	FaceFengshuiResult result = null;
	ListView listViewResult = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_result);

		listViewResult = (ListView) this.findViewById(R.id.listViewResults);
		
		try {
			result = (FaceFengshuiResult) getIntent().getSerializableExtra("FaceFengshuiResult");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		final String[] matrix = {"_id", "label", "faceFengshuiResult"};
		final String[] columns = {"label", "faceFengshuiResult"};
		final int[] layouts = {R.id.columnLeft, R.id.columnRight};

		MatrixCursor cursor = new MatrixCursor(matrix);
		int key = 0;
		for (Map.Entry<String, String> entry : result.getResults().entrySet()) {
			cursor.addRow(new Object[] {key++, entry.getKey(), entry.getValue()});
		}

		// If flags parameter is not set, it defaults to FLAG_AUTO_REQUERY, which results in Cursor queries being performed on the application's UI thread
		// Hence the constructor without flags parameter has been deprecated
		SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.two_column_list_item, cursor, columns, layouts, 0);
		listViewResult.setAdapter(simpleCursorAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.face_fengshui_result, menu);
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

}
