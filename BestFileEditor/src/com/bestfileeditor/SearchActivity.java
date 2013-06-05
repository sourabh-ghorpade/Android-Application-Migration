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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class SearchActivity extends Activity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		new Thread(this).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public void run() {
		Intent intent = getIntent();
		Log.e("File Searcher", "Starting the search" + new Date().toString());
		String filePaths[] = intent.getStringArrayExtra("DATA_FILES_PATHS");
		String searchQuery = intent.getStringExtra("searchQuery");
		if (filePaths == null || searchQuery == null ) {
			setResult(RESULT_CANCELED);
			finish();
		}
		long frequency=-1;
		try {
			frequency = searchFrequencyInFile(searchQuery, filePaths[0]);
		} catch (Exception e) {
			setResult(RESULT_CANCELED);
			finish();
		}
		Log.e("SEARCH_STRING", "Searching Finished at "
				+ new Date().toString());
		Intent resultIntent = new Intent();
		resultIntent.putExtra("DONE", "Done");
		resultIntent.putExtra("FREQUENCY", frequency);
		setResult(RESULT_OK, resultIntent);
		finish();
	}

	private long searchFrequencyInFile(String searchQuery, String filePath)
			throws IOException {
		File file = new File(filePath);
		if(!file.exists())
			throw new FileNotFoundException();
		WordReader wordReader = new WordReader(filePath);
		long frequency = 0;
		while (true) {
			String word = wordReader.readWord();
			if (word.equals(""))
				break;
			if (searchQuery.equals(word))
				frequency++;
		}
		return frequency;
	}

}
