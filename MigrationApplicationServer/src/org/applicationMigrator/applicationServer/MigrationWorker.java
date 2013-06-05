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

// JOB: To handle a request asynchronously

package org.applicationMigrator.applicationServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.applicationMigrator.applicationServer.communication.ClientSocketConnection;
import org.applicationMigrator.applicationServer.communication.ServerSocketConnection;
import org.applicationMigrator.applicationServer.emulatorManagement.Emulator;
import org.applicationMigrator.applicationServer.emulatorManagement.EmulatorPoolManager;

class MigrationWorker extends Thread
{

    // private static final int SERVER_AGENT_PORT_NUMBER = 9091;
    private static final String LOCAL_MACHINE = "localhost";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid Request Parameters";
    private static final String ERROR_MESSAGE = "Following Error Occurred :";
    private static final int ERROR_READING_INDEX_FILE = -2;
    private static final Object EMPTY_STRING = "";
    private ServerSocketConnection connectionwithClient;
    private static HashMap<String, String> supportedApplications;
    private final String PACKAGE_NAME = "APP_NAME";

    static
    {
	try
	{
	    initialiseSupportedApplications();
	} catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.exit(ERROR_READING_INDEX_FILE);
	}
    }

    public MigrationWorker(Socket connection) throws IOException
    {
	connectionwithClient = new ServerSocketConnection(connection);
    }

    private static void initialiseSupportedApplications() throws IOException
    {
	supportedApplications = new HashMap<String, String>();
	String indexFilePath = "C:\\AndroidMigration\\AppRepo\\installedAppsList.txt";
	FileReader fileReader = new FileReader(indexFilePath);
	while (true)
	{
	    String applicationName = fileReader.readWord();
	    if (applicationName.equals(EMPTY_STRING)
		    || applicationName.equals("\n"))
		break;
	    String setupPathString = fileReader.readWord();
	    supportedApplications.put(applicationName, setupPathString);
	}
    }

    private void handleRequest() throws ClassNotFoundException, IOException
    {
	HashMap<String, String> applicationRequirements = getApplicationRequirements();
	if (!isValidRequest(applicationRequirements))
	{
	    rejectClientRequest(INVALID_REQUEST_MESSAGE);
	    return;
	} else
	    acceptClientRequest();

	List<Object> receivedObjects = connectionwithClient.getObjectsList();
	EmulatorPoolManager emulatorPoolManager = EmulatorPoolManager
		.getInstance();
	Emulator emulator;
	String applicationPackageName;
	try
	{
	    applicationPackageName = applicationRequirements.get(PACKAGE_NAME);
	    emulator = emulatorPoolManager.allocateEmulator(
		    applicationRequirements, applicationPackageName,
		    supportedApplications.get(applicationPackageName));
	} catch (Exception e)
	{
	    rejectClientRequest(e.getMessage());
	    return;
	}
	List<Object> resultObjects;
	try
	{
	    resultObjects = transferToServerAgent(receivedObjects,
		    emulator.getServerAgentsPortNumber());
	    connectionwithClient.sendObjectsList(resultObjects);
	} catch (Exception e)
	{
	    e.printStackTrace();
	} finally
	{
	    try
	    {
		emulatorPoolManager.freeEmulator(emulator,
			applicationPackageName);
	    } catch (Exception ignored)
	    {
	    }
	}
	System.out.println("Work finshed at " + new Date());
	System.out.println(" Thread " + this.getId() + " finished work at "
		+ new Date());
    }

    private void rejectClientRequest(String message) throws IOException
    {
	connectionwithClient.sendObjectToClient(message);
    }

    private HashMap<String, String> getApplicationRequirements()
	    throws ClassNotFoundException, IOException
    {
	HashMap<String, String> applicationRequirements = new HashMap<String, String>();
	List<Object> list = connectionwithClient.getObjectsList();
	for (int counter = 0; counter < list.size(); counter += 2)
	{
	    String requirementName = (String) list.get(counter);
	    String requirementValue = (String) list.get(counter + 1);
	    applicationRequirements.put(requirementName, requirementValue);
	}
	return applicationRequirements;
    }

    private void acceptClientRequest() throws IOException
    {
	connectionwithClient.sendObjectToClient("ACCEPTED");
    }

    private List<Object> transferToServerAgent(List<Object> recievedObjects,
	    int emulatorPortNumber) throws IOException, ClassNotFoundException
    {
	ClientSocketConnection clientSocketConnection;
	clientSocketConnection = new ClientSocketConnection(LOCAL_MACHINE,
		emulatorPortNumber);
	clientSocketConnection.sendObjectsListToServer(recievedObjects);
	List<Object> objectsReceivedFromServerAgent = clientSocketConnection
		.getObjectsListFromServer();
	clientSocketConnection.sendObjectToServer("Done");
	return objectsReceivedFromServerAgent;
    }

    private boolean isValidRequest(
	    HashMap<String, String> applicationRequirementsList)
    {
	String applicationName = applicationRequirementsList.get("APP_NAME");
	if (supportedApplications.containsKey(applicationName))
	    return true;
	else
	    return false;
    }

    @Override
    public void run()
    {
	try
	{
	    handleRequest();
	} catch (Exception e)
	{
	    e.printStackTrace();
	    try
	    {
		connectionwithClient.sendObjectToClient(ERROR_MESSAGE
			+ e.getMessage());
	    } catch (IOException e1)
	    {
		e1.printStackTrace();
	    }
	}

    }

}
