/*
 * Copyright 2013 Sourabh Ghorpade

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/   

package com.bestfileeditor;

import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String ERROR_MESSAGE = "Some Error Occured :(";
	public static final String LOCAL_EXECUTION_ACTION = "com.bestfileeditor.EXECUTE_ON_DEVICE";
	public static final String CLOUD_EXECUTION_ACTION = "org.applicationMigrator.migrationClient.executeOnCloud";
	private static final CharSequence WAITING_FOR_RESULT_MESSAGE = "Please wait while Result is being Caluclated";
	private Date workStartDate, workFinishDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void cloudExecution(View view) {
		execute(CLOUD_EXECUTION_ACTION);
	}

	public void localExecution(View view) {
		execute(LOCAL_EXECUTION_ACTION);
	}

	private void execute(String action) {
		EditText searchEditText = (EditText) findViewById(R.id.editText1);
		String searchTextString = searchEditText.getText().toString();
		TextView outputTextView = (TextView) findViewById(R.id.searchResult);
		outputTextView.setText(WAITING_FOR_RESULT_MESSAGE);
		TextView startTimeTextView = (TextView) findViewById(R.id.startTimeValue);
		Intent intent = new Intent();
		intent.putExtra("searchQuery", searchTextString);
		intent.putExtra("APP_NAME", "com.bestfileeditor");
		String[] dataFilesPaths = { Environment.getExternalStorageDirectory()
				.getPath() + "/ApplicationMigrator/sampleFile.txt" };
		intent.putExtra("DATA_FILES_PATHS", dataFilesPaths);
		boolean[] forceUpdateValues = { false };
		intent.putExtra("FORCE_UPDATE", forceUpdateValues);
		intent.setAction(action);
		workStartDate = new Date();
		startTimeTextView
				.setText("Work started at:" + workStartDate.toString());
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		TextView resultTextView = (TextView) findViewById(R.id.searchResult);
		if (resultCode == RESULT_OK) {
			long frequency = data.getLongExtra("FREQUENCY", -1);
			resultTextView.setText("The Word has occurred " + frequency
					+ " times");
			workFinishDate = new Date();
			TextView finishTimeTextView = (TextView) findViewById(R.id.finishTimeValue);
			finishTimeTextView.setText("Work finished at:"
					+ workFinishDate.toString());
		} else {
			resultTextView.setText(ERROR_MESSAGE);
		}

	}
}
