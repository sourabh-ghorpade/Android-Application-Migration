package com.androidImageProcessor;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends Activity {

	private Bitmap imageBitmap;
	private ImageView imageView;
	public static final String LOCAL_EXECUTION_ACTION = "com.androidImageProcessor.EXECUTE_ON_DEVICE";
	public static final String CLOUD_EXECUTION_ACTION = "org.applicationMigrator.migrationClient.executeOnCloud";
	private String inputImageFilePathString = Environment
			.getExternalStorageDirectory().getPath()
			+ "/ApplicationMigrator/trialImage.jpg";
	private String outputImageFilePathString = Environment
			.getExternalStorageDirectory().getPath()
			+ "/ApplicationMigrator/outputImage.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		renderImage(inputImageFilePathString);
	}

	private void renderImage(String imageFilePathString) {
		File imageFile = new File(imageFilePathString);
		if (imageFile.exists()) {
			imageBitmap = BitmapFactory.decodeFile(imageFilePathString);
			imageView = (ImageView) findViewById(R.id.imageView1);
			if (imageView != null)
				{
				imageView.setImageBitmap(imageBitmap);
				}
		}
	}

	public void cloudExecution(View view) {
		processImage(CLOUD_EXECUTION_ACTION);
	}

	public void localExecution(View view) {
		processImage(LOCAL_EXECUTION_ACTION);
	}

	private void processImage(String action) {
		TextView errorMessageTextView = (TextView) findViewById(R.id.textView1);
		errorMessageTextView.setVisibility(View.INVISIBLE);
		//imageBitmap.recycle();
		Intent intent = new Intent();

		intent.putExtra("APP_NAME", "com.androidImageProcessor");
		String[] dataFilesPaths = { inputImageFilePathString };
		intent.putExtra("DATA_FILES_PATHS", dataFilesPaths);
		boolean[] forceUpdateValues = { true };
		intent.putExtra("FORCE_UPDATE", forceUpdateValues);
		intent.putExtra("OUTPUT_FILE_PATH", outputImageFilePathString);
		intent.setAction(action);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			renderImage(outputImageFilePathString);
		} else {
			TextView errorMessageTextView = (TextView) findViewById(R.id.textView1);
			errorMessageTextView.setVisibility(View.VISIBLE);
		}

	}

}
