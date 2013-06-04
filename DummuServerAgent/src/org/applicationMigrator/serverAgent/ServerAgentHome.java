package org.applicationMigrator.serverAgent;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import org.applicationMigrator.serverAgent.communication.ServerSocketConnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class ServerAgentHome extends Activity implements Runnable
{
    private static final String PROCESS_COMPUTATION_ACTION = "org.applicationMigrator.serverAgent.processComputation";
    private static final int REQUEST_CODE = 0;
    private static final String FORCE_UPDATE_VALUE = "FORCE_UPDATE";
    private static final int SERVER_PORT_NUMBER = 9092;
    private ServerSocketConnection serverSocketConnection;
    private ServerSocket serverSocket;
    private List<String> outputFilePathsList;
    private static final String ADMIN_CREDENTIALS_PATH = "mnt/sdcard/ApplicationMigrator/AWSCredentials.properties";
    private static final String DATA_FILES_PATHS = "DATA_FILES_PATHS";
    // private static final String FORCE_UPDATE_VALUE = "FORCE_UPDATE";
    private static final String APPLICATION_NAME = "APP_NAME";
    private static final String USERNAME = "userName";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_home);
	Thread requestHandlerThread = new Thread(this);
	requestHandlerThread.start();
    }

    private void handleClientRequest() throws IOException, InterruptedException
    {
	try
	{
	    serverSocketConnection = getClientConnection();
	    Log.d("Server", "Server Started");
	    List<Object> intentExtrasList = serverSocketConnection
		    .getObjectsList();
	    Log.e("SERVER", "Objects recieved from client");
	    Intent intent = IntentConverter
		    .convertObjectsToIntent(intentExtrasList);
	    outputFilePathsList = getDataFiles(intent);
	    intent.setAction(PROCESS_COMPUTATION_ACTION);
	    startActivityForResult(intent, REQUEST_CODE);
	} catch (Exception e)
	{
	    // TODO: Handle Exceptions by informing the server
	    serverSocketConnection.sendObjectToClient("ERROR");
	    serverSocketConnection.sendObjectToClient(e.getMessage());
	    e.printStackTrace();
	    Log.e("ServerAgentErrors", e.getMessage());
	}
    }

    private List<String> getDataFiles(Intent intent) throws IOException,
	    InterruptedException, InvalidAttributeValueException
    {
	List<String> outputFilesPathsList = new ArrayList<String>();
	String outputFilesPaths[] = intent
		.getStringArrayExtra(DATA_FILES_PATHS);
	if (outputFilesPaths == null)
	    return outputFilesPathsList;
	String applicationName = intent.getStringExtra(APPLICATION_NAME);
	String userName = intent.getStringExtra(USERNAME);
	if (applicationName == null || userName == null)
	    throw new InvalidAttributeValueException();
	ServerAgentFileTransferClient fileTransferClient = new ServerAgentFileTransferClient(
		applicationName, userName);
	fileTransferClient.downloadFiles(outputFilesPaths,
		ADMIN_CREDENTIALS_PATH);
	for (int counter = 0; counter < outputFilesPaths.length; counter++)
	{
	    outputFilesPathsList.add(outputFilesPaths[counter]);
	}
	return outputFilesPathsList;
    }

    private ServerSocketConnection getClientConnection() throws IOException
    {
	try
	{
	    serverSocket = new ServerSocket(SERVER_PORT_NUMBER);
	} catch (IOException e1)
	{
	    // TODO Auto-generated catch block
	    Log.e("Server", e1.getMessage());
	    e1.printStackTrace();
	    finish();
	}
	Socket connection = serverSocket.accept();
	ServerSocketConnection serverSocketConnection = new ServerSocketConnection(
		connection);
	return serverSocketConnection;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
	    Intent resultIntent)
    {
	try
	{
	    if (resultCode == RESULT_OK)
	    {
		try
		{
		    String applicationName = resultIntent
			    .getStringExtra(APPLICATION_NAME);
		    ServerAgentFileTransferClient fileTransferClient = new ServerAgentFileTransferClient(
			    applicationName, ADMIN_CREDENTIALS_PATH);
		    String[] dataFilesPaths = resultIntent
			    .getStringArrayExtra(DATA_FILES_PATHS);
		    boolean[] forceUploadValues = resultIntent
			    .getBooleanArrayExtra(FORCE_UPDATE_VALUE);
		    fileTransferClient.uploadFiles(dataFilesPaths,
			    forceUploadValues, ADMIN_CREDENTIALS_PATH);

		    List<Object> intentParametersList = IntentConverter
			    .convertIntentToObjects(resultIntent);
		    String acknowledgementString;
		    serverSocketConnection
			    .sendObjectsList(intentParametersList);
		    Object acknowledgementObject = serverSocketConnection
			    .getObjectFromClient();
		    acknowledgementString = (String) acknowledgementObject;
		    if (!acknowledgementString.equals("Done"))
			Log.e("Server", "Message Not Recieved");
		} catch (Exception e)
		{
		    Log.e("Server Error ", e.getMessage());
		}
	    } else
	    {
		sendErrorMessage();
	    }
	} finally
	{
	    try
	    {
		serverSocketConnection.close();
		serverSocket.close();
		for (String filePath : outputFilePathsList)
		{
		    File outputFile = new File(filePath);
		    if (outputFile.exists())
			outputFile.delete();
		}
	    } catch (IOException e)
	    {
		Log.e("Server Errors", e.getMessage());
		e.printStackTrace();
	    }
	}
	finish();
    }

    private void sendErrorMessage()
    {
	List<Object> resultObjects = new ArrayList<Object>();
	resultObjects.add("ERROR");
	try
	{
	    serverSocketConnection.sendObjectsList(resultObjects);
	} catch (Exception ignored)
	{
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	getMenuInflater().inflate(R.menu.activity_home, menu);
	return true;
    }

    public void run()
    {
	try
	{
	    handleClientRequest();
	} catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InterruptedException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}