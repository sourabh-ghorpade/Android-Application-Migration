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
package com.bestMatrixMultiplier.matrixmultiplier;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String ERROR_MESSAGE = "Some Error Occured :(";
	public static final String LOCAL_EXECUTION_ACTION = "com.bestMatrixMultiplier.matrixmultiplier.EXECUTE_ON_DEVICE";
	public static final String CLOUD_EXECUTION_ACTION = "org.applicationMigrator.migrationClient.executeOnCloud";
	private static final CharSequence INVALID_VALUE_OF_N_ERROR_MESSAGE = "Please Enter valid value of N";
	private static final CharSequence WAITING_FOR_RESULT_MESSAGE = "Please wait while Result is being Caluclated";
	private Date workStartDate, workFinishDate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void cloudExecution(View view) {
		execute(CLOUD_EXECUTION_ACTION);
	}

	public void localExecution(View view) {
		execute(LOCAL_EXECUTION_ACTION);
	}

	private void execute(String action) {
		EditText valueOfNTextView = (EditText) findViewById(R.id.textFieldValueOfN);
		int numberOfRowsAndColumns;
		try {
			numberOfRowsAndColumns = Integer.parseInt(valueOfNTextView
					.getText().toString());
		} catch (Exception e) {
			valueOfNTextView.setText(INVALID_VALUE_OF_N_ERROR_MESSAGE);
			return;
		}
		TextView outputTextView = (TextView) findViewById(R.id.textViewFinishTimeValue);
		outputTextView.setText(WAITING_FOR_RESULT_MESSAGE);
		TextView startTimeTextView = (TextView) findViewById(R.id.textViewStartTimeValue);
		Intent intent = new Intent();
		intent.putExtra("NUMBER_OF_ROWS_AND_COLUMNS", numberOfRowsAndColumns);
		intent.putExtra("APP_NAME", "com.bestMatrixMultiplier.matrixmultiplier");
		intent.setAction(action);
		workStartDate = new Date();
		startTimeTextView.setText(workStartDate.toString());
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		TextView outputTextView = (TextView) findViewById(R.id.textViewFinishTimeValue);
		if (resultCode == RESULT_OK) {
			workFinishDate = new Date();
			outputTextView.setText(workFinishDate.toString());
		} else {
			outputTextView.setText(ERROR_MESSAGE);
		}

	}
}
