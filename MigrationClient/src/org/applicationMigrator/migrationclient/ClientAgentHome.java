package org.applicationMigrator.migrationclient;

import java.io.IOException;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.NoSuchAttributeException;

import org.applicationMigrator.exceptions.NonSerializableExtraException;
import org.applicationMigrator.migrationclient.communication.ClientSocketConnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;

public class ClientAgentHome extends Activity implements Runnable {

	private static final String DATA_FILES_PATHS = "DATA_FILES_PATHS";
	private static final String FORCE_UPDATE_VALUE = "FORCE_UPDATE";
	private static final String CREDENTIALS_FILE_PATH = Environment
			.getExternalStorageDirectory().getPath()
			+ "/ApplicationMigrator/AwsCredentials.properties";
	private static final String APPLICATION_NAME = "APP_NAME";
	private static final String MIGRATE_ACTION = "org.applicationMigrator.migrationClient.executeOnCloud";
	static final String HOST_NAME = "192.168.204.1";//"ec2-54-251-124-244.ap-southeast-1.compute.amazonaws.com";//"192.168.43.59";//"10.0.2.2";//"192.168.1.5";//;//"192.168.43.59";// "192.168.1.2";//"10.0.2.2";//"192.168.1.4";//"192.168.43.59";//"192.168.1.2";////
	// "ec2-54-251-110-170.ap-southeast-1.compute.amazonaws.com";

	private static final int PORT_NUMBER = 9090;
	private static final boolean ACCEPTED = true;
	private Intent resultIntent, recievedIntent;
	private int resultCode;
	private ClientSocketConnection clientSocketConnection;
	private Thread migrationThread;

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (migrationThread != null)
			migrationThread.interrupt();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_agent_home);
		recievedIntent = getIntent();
		resultCode = RESULT_CANCELED;
		resultIntent = null;
		if (recievedIntent.getAction().equals(MIGRATE_ACTION)) {
			migrationThread = new Thread(this);
			migrationThread.start();
		}
	}

	private void performMigration() {
		try {
			if (migrateRequest() == ACCEPTED) {
				resultIntent = migrate();
				resultCode = RESULT_OK;
			}
		} catch (Exception e) {
			resultIntent = new Intent();
			resultIntent.putExtra("EXCEPTION", e);
		}
		setResult(resultCode, resultIntent);
		finish();
	}

	private Intent migrate() throws IOException, ClassNotFoundException,
			NonSerializableExtraException, NoSuchAttributeException, InvalidAttributeValueException, InterruptedException {

		String applicationName = recievedIntent
				.getStringExtra(APPLICATION_NAME);
		final String ANDROID_ID=Settings.Secure.ANDROID_ID;
		FileTransferClient fileTransferClient = new FileTransferClient(
				applicationName, CREDENTIALS_FILE_PATH,ANDROID_ID);
		String[] dataFilesPaths = recievedIntent
				.getStringArrayExtra(DATA_FILES_PATHS);
		boolean[] forceUploadValues = recievedIntent
				.getBooleanArrayExtra(FORCE_UPDATE_VALUE);
		fileTransferClient.uploadFiles(dataFilesPaths, forceUploadValues);
		String userNameString = fileTransferClient.getUserName();
		recievedIntent.putExtra("userName", userNameString);
		sendIntent();
		Intent resultIntent = recieveResultIntent();
		downloadDataFiles(resultIntent);
		return resultIntent;
	}

	private Intent recieveResultIntent() throws IOException,
			ClassNotFoundException {
		List<Object> recievedObjects = clientSocketConnection
				.getObjectsListFromServer();
		return IntentConverter.convertObjectsToIntent(recievedObjects);
	}

	private void sendIntent() throws NonSerializableExtraException, IOException {
		List<Object> intentParameters = IntentConverter
				.convertIntentToObjects(recievedIntent);
		clientSocketConnection.sendObjectsListToServer(intentParameters);
	}

	private boolean migrateRequest() throws IOException {
		clientSocketConnection = new ClientSocketConnection(HOST_NAME,
				PORT_NUMBER);
		Object responseObject = null;
		try {
			clientSocketConnection.sendObjectToServer("API_LEVEL");
			clientSocketConnection.sendObjectToServer("2");
			String packageNameString = recievedIntent
					.getStringExtra(APPLICATION_NAME);
			clientSocketConnection.sendObjectToServer(APPLICATION_NAME);
			clientSocketConnection.sendObjectToServer(packageNameString);
			clientSocketConnection.sendObjectToServer("DONE");
			responseObject = clientSocketConnection.getObjectFromServer();
			
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			return false;
		}
		String response = (String) responseObject;
		return response.equals("ACCEPTED");
	}

	private void downloadDataFiles(Intent intent) throws IOException,
			InterruptedException, InvalidAttributeValueException, ClassNotFoundException {
		String outputFilesPaths[] = intent
				.getStringArrayExtra(DATA_FILES_PATHS);
		if (outputFilesPaths == null)
			return;
		String applicationName = intent.getStringExtra(APPLICATION_NAME);
		if (applicationName == null)
			throw new InvalidAttributeValueException();
		final String ANDROID_ID=Settings.Secure.ANDROID_ID;
		FileTransferClient fileTransferClient = new FileTransferClient(
				applicationName,CREDENTIALS_FILE_PATH,ANDROID_ID);
		fileTransferClient.downloadFiles(outputFilesPaths);
	}

	@Override
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_client_agent_home, menu);
		return true;
	}

	public void run() {
		performMigration();
	}
}
