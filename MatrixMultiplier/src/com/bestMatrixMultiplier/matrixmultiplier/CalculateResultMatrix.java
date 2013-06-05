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
import android.util.Log;
import android.view.Menu;

public class CalculateResultMatrix extends Activity implements Runnable {

	public static final String RESULT_MATRIX = "RESULT_MATRIX";
	private double matrixOne[][], matrixTwo[][];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculate_result_matrix);
		new Thread(this).start();
	}

	private double[][] calculateResultMatrix(
			final int NUMBER_OF_ROWS_AND_COLUMNS) {
		double resultMatrix[][] = new double[NUMBER_OF_ROWS_AND_COLUMNS][NUMBER_OF_ROWS_AND_COLUMNS];
		for (int rowCounter = 0; rowCounter < NUMBER_OF_ROWS_AND_COLUMNS; rowCounter++) {
			for (int columnCounter = 0; columnCounter < NUMBER_OF_ROWS_AND_COLUMNS; columnCounter++) {
				double sum = 0;
				for (int sumCounter = 0; sumCounter < NUMBER_OF_ROWS_AND_COLUMNS; sumCounter++) {
					sum += matrixOne[rowCounter][sumCounter]
							* matrixTwo[sumCounter][columnCounter];
					Log.d("Partial Result", "Sum is :" + sum);
				}
				Log.d("Partial Result", "Sum is :" + sum);
				resultMatrix[rowCounter][columnCounter] = sum;
			}
		}
		return resultMatrix;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater()
				.inflate(R.menu.activity_calculate_result_matrix, menu);
		return true;
	}

	public void run() {
		Intent intent = getIntent();
		Log.e("MATRIX MULTIPLICATION", "Creating Matrices");
		final int NUMBER_OF_ROWS_AND_COLUMNS = intent.getIntExtra(
				"NUMBER_OF_ROWS_AND_COLUMNS", 0);
		createMatrices(NUMBER_OF_ROWS_AND_COLUMNS);
		Log.e("MATRIX MULTIPLICATION", "Starting Multiplication at "
				+ new Date().toString());
		double resultMatrix[][] = calculateResultMatrix(NUMBER_OF_ROWS_AND_COLUMNS);
		Log.e("MATRIX MULTIPLICATION", "Multiplication Finished at "
				+ new Date().toString());
		Intent resultIntent=new Intent();
		intent.putExtra("DONE", "Done");
		setResult(RESULT_OK,resultIntent);
		finish();
	}

	private void createMatrices(final int NUMBER_OF_ROWS_AND_COLUMNS) {
		matrixOne = new double[NUMBER_OF_ROWS_AND_COLUMNS][NUMBER_OF_ROWS_AND_COLUMNS];
		matrixTwo = new double[NUMBER_OF_ROWS_AND_COLUMNS][NUMBER_OF_ROWS_AND_COLUMNS];
		double difference = NUMBER_OF_ROWS_AND_COLUMNS
				* NUMBER_OF_ROWS_AND_COLUMNS;
		double currentValue = 1;
		for (int rowCounter = 0; rowCounter < NUMBER_OF_ROWS_AND_COLUMNS; rowCounter++) {
			for (int columnCounter = 0; columnCounter < NUMBER_OF_ROWS_AND_COLUMNS; columnCounter++) {
				matrixOne[rowCounter][columnCounter] = currentValue;
				matrixTwo[rowCounter][columnCounter] = currentValue
						+ difference;
				currentValue++;
			}
		}
	}

	private void setResult(double[][] resultMatrix) {
		Intent resultData = new Intent();
		setResult(RESULT_OK, resultData);
		Log.e(RESULT_MATRIX, "calculated");

	}
}
