package com.androidImageProcessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.androidImageProcessor.imageProcessors.ImageProcessors;

public class ProcessImage extends Activity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process_image);
		new Thread(this).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.process_image, menu);
		return true;
	}

	@Override
	public void run() {
		Intent intent = getIntent();
		Log.e("Image Processor",
				"Starting image processing @ " + new Date().toString());
		String filePaths[] = intent.getStringArrayExtra("DATA_FILES_PATHS");
		if (filePaths == null) {
			setResult(RESULT_CANCELED);
			finish();
		}
		String inputImageFilePathString = filePaths[0];
		String outputImagePathString=intent.getStringExtra("OUTPUT_FILE_PATH");
		Intent resultIntent = new Intent();
		int resultCode=RESULT_OK;
		try{
			if(inputImageFilePathString ==null || outputImagePathString == null)
				throw new IOException("Input or Output Files not Specified");
		processImage(inputImageFilePathString,outputImagePathString);
		resultIntent.putExtra("APP_NAME", "com.androidImageProcessor");
		String[] dataFilesPaths = { outputImagePathString };
		resultIntent.putExtra("DATA_FILES_PATHS", dataFilesPaths);
		boolean[] forceUpdateValues = { true };
		resultIntent.putExtra("FORCE_UPDATE", forceUpdateValues);
		
		}
		catch(IOException e)
		{
			resultIntent.putExtra("EXCEPTION", "Exception is :" + e.getMessage());
			resultCode=RESULT_CANCELED;
		}
		setResult(resultCode, resultIntent);
		finish();
	}

	private void processImage(String imageFilePath,String outputImagePath) throws IOException {
	 Date startDate=new Date();
	 Log.e("Image Processing", "Processing Started @ " + startDate);
	 Bitmap sourceImageBitmap=BitmapFactory.decodeFile(imageFilePath);
	 Bitmap outputImageBitmap=ImageProcessors.emboss(sourceImageBitmap);
	 sourceImageBitmap.recycle();
	 FileOutputStream out = new FileOutputStream(outputImagePath);
     outputImageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
     Date finishDate=new Date();
     Log.e("Image Processing", "Processing Completed @ " + finishDate);
	}

}
