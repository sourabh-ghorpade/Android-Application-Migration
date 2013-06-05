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
package com.factorialCalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MigrationHome extends Activity {

	private static final String NUMBER = "NUMBER";
	private static final String FACTORIAL = "FACTORIAL";
	private static final String MIGRATE_ACTION = "org.applicationMigrator.migrationClient.executeOnCloud";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}

	public void sendMessage(View view) {
		Intent intent = new Intent(MIGRATE_ACTION);
		intent.putExtra("APP_NAME", "Factorial_Calculator");
		TextView inputTextView = (TextView) findViewById(R.id.textViewInput);
		String numberString = inputTextView.getText().toString();
		double number = Double.valueOf(numberString);
		intent.putExtra(NUMBER, number);
		intent.putExtra("APP_NAME", "com.factorialCalculator");
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		TextView outputTextView = (TextView) findViewById(R.id.textViewOutput);
		if (resultCode == RESULT_OK) {
			double resultOfFactorialOperation = Double.valueOf(data
					.getDoubleExtra(FACTORIAL, 0));
			outputTextView.setText(resultOfFactorialOperation + "");
		} else {
			outputTextView.setText("ERROR");
		}
	}
}
