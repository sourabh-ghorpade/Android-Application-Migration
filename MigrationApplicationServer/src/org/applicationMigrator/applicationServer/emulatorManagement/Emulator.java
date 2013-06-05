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
package org.applicationMigrator.applicationServer.emulatorManagement;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.naming.directory.InvalidAttributesException;

public class Emulator
{
    private static final String SUCCESSFULL_SHUTDOWN_MESSAGE = "OK: killing emulator, bye bye";
    private static final String STARTUP_COMMAND = "F:\\Android SDK\\tools\\emulator -avd ";
    public static final String SERVER_AGENT_PACKAGE_NAME = "org.applicationMigrator.serverAgent";
    public static final String SERVER_AGENT_ACTIVITY_NAME = ".ServerAgentHome";
    private static final int STARTUP_WAITING_TIME = 1000;
    private static final int INITIAL_PORT_NUMBER_FOR_EMULATOR = 9092;
    private static final int MAXIMUM_PORT_NUMBER = 10000;
    private static final String NONE = "";
    private static final String SD_CARD_FOLDER_PATH = "C:\\AndroidMigration\\SDCards\\";
    private static final String SD_CARD_SIZE = "100M";
    private static final String CREDENTIALS_FILE_PATH = "C:\\AndroidMigration\\Credentials\\AwsCredentials.properties";
    private String previouslyRunApplication;
    private static HashMap<Integer, Integer> allocatedPortNumbers;
    //private int apiLevel;
    private boolean isInUse;
    private String serialNumber;
    private Command startCommand = new Command(STARTUP_COMMAND);
    private int serverAgentsPortNumber;
    private String sdCardImagePathString;

    static
    {
	allocatedPortNumbers = new HashMap<Integer, Integer>();
    }

    boolean isInUse()
    {
	return isInUse;
    }

    void setInUse(boolean isInUse)
    {
	this.isInUse = isInUse;
    }

    public boolean isCompatible(HashMap<String, String> applicationRequirements)
    {
	if (isInUse)
	    return false;
	return true;
    }

    private Emulator(String name, String serialNumber,
	    int portNumberForServerAgent, String sdCardImagePathString)
    {
	this.serialNumber = serialNumber;
	isInUse = false;
	previouslyRunApplication = NONE;
	this.serverAgentsPortNumber = portNumberForServerAgent;
	this.sdCardImagePathString = sdCardImagePathString;
	startCommand = new Command(STARTUP_COMMAND + name + " -port "
		+ getPortNumber() + " -sdcard " + this.sdCardImagePathString);

    }

    static Emulator startEmulator(String emulatorName,
	    int portNumberToBeAssigned, final String serverAgentSetupPath)
	    throws Exception

    {
	Emulator emulator = null;
	try
	{
	    emulator = getEmulatorInstance(emulatorName, portNumberToBeAssigned);
	    emulator.start();
	} catch (InvalidAttributesException | IOException
		| InterruptedException e1)
	{
	    e1.printStackTrace();
	    try
	    {
		emulator.shutdown();
	    } catch (IOException | InterruptedException e)
	    {
		e.printStackTrace();
	    }
	    throw e1;
	}
	boolean emulatorStarted = false;
	int numberOfTries = 0;
	final int MAX_NUMBER_OF_TRIES = 200;
	while (!emulatorStarted)
	{
	    try
	    {
		while (!emulator.isBooted())
		{
		    emulator.waitInitialLoadingTime(STARTUP_WAITING_TIME);
		}
		emulatorStarted = true;
	    } catch (Exception e)
	    {
		if (numberOfTries > MAX_NUMBER_OF_TRIES)
		{
		    emulator.shutdown();
		    throw e;
		}
		numberOfTries++;
		emulator.waitInitialLoadingTime(4000);
	    }
	}
	emulator.waitInitialLoadingTime(3000);
	emulator.copyCredentialsFile();
	emulator.issueRedirectCommand();

	emulator.installApplication(serverAgentSetupPath);
	emulator.runApplication(SERVER_AGENT_PACKAGE_NAME,
		SERVER_AGENT_ACTIVITY_NAME);
	return emulator;
    }

    private void copyCredentialsFile() throws IOException, InterruptedException
    {
	final String CREDENTIALS_PATH_ON_EMULATOR=" /sdcard/ApplicationMigrator";
	String createFolderCommandString = "adb -s " + serialNumber + " shell"
		+ " mkdir "  +CREDENTIALS_PATH_ON_EMULATOR;
	Command createFolderCommand = new Command(createFolderCommandString);
	createFolderCommand.execute();
	String copyCredentialsFileCommandString="adb -s " + serialNumber + 
		" push " + CREDENTIALS_FILE_PATH + " " + CREDENTIALS_PATH_ON_EMULATOR;
	new Command(copyCredentialsFileCommandString).execute();
    }

    private void issueRedirectCommand() throws IOException,
	    InterruptedException
    {
	String redirectionCommandString = "adb -s " + serialNumber
		+ " forward tcp:" + serverAgentsPortNumber + " tcp:9092";
	Command redirectionCommand = new Command(redirectionCommandString);
	redirectionCommand.execute();
	List<String> result = redirectionCommand.getProcessResult();
	for (String string : result)
	{
	    System.out.println(string);
	}

    }

    private void waitInitialLoadingTime(final int WAITING_TIME)
	    throws InterruptedException
    {
	Thread.sleep(WAITING_TIME);
    }

    private void start() throws IOException, InterruptedException
    {
	startCommand.execute();
    }

    public boolean isBooted() throws IOException, InterruptedException
    {
	final String BOOTING_COMPLETED = "stopped";
	String isBootedCommandString = "adb -s " + this.serialNumber
		+ " shell getprop init.svc.bootanim";
	Command isBootedCommand = new Command(isBootedCommandString);
	isBootedCommand.execute();
	List<String> resultList = isBootedCommand.getProcessResult();
	for (String result : resultList)
	    System.out.println(result);
	if (resultList.size() != 0
		&& resultList.get(0).equals(BOOTING_COMPLETED))
	    return true;
	return false;
    }

    public void runApplication(String packageName, String activityName)
	    throws IOException, InterruptedException
    {
	String commandString = "adb -s " + this.serialNumber
		+ " shell am start -n  " + packageName + "/" + activityName;
	Command runApplicationCommand = new Command(commandString);
	runApplicationCommand.execute();
	List<String> result = runApplicationCommand.getProcessResult();
	for (String string : result)
	{
	    System.out.println(string);
	}
    }

    public void installApplication(String pathToApplication)
	    throws IOException, InterruptedException
    {
	String commandString = "adb -s " + this.serialNumber + " install "
		+ pathToApplication;
	Command command = new Command(commandString);
	command.execute();
	List<String> resultsList = command.getProcessResult();
	for (String string : resultsList)
	{
	    System.out.println(string);
	}
    }

    private static Emulator getEmulatorInstance(String emulatorName,
	    int portNumberToBeAssigned) throws IOException,
	    InterruptedException, InvalidAttributesException
    {
	String serialNumber = "emulator-" + portNumberToBeAssigned;
	int portNumberForServerAgent = selectPortNumber();
	String sdCardImagePath = createSDCard();
	String AVD_NAME = "Server";
	Emulator emulator = new Emulator(AVD_NAME, serialNumber,
		portNumberForServerAgent, sdCardImagePath);
	return emulator;
    }

    private synchronized static String createSDCard()
    {
	String sdCardNameString = "sdCard"
		+ new Random(System.currentTimeMillis()).nextInt();
	final String IMAGE_PATH = SD_CARD_FOLDER_PATH + sdCardNameString
		+ ".img";
	String createSDCardCommandString = "mksdcard -l " + sdCardNameString
		+ " " + SD_CARD_SIZE + " " + IMAGE_PATH;
	try
	{
	    Command createSDCardCommand = new Command(createSDCardCommandString);
	    createSDCardCommand.execute();
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
	return IMAGE_PATH;
    }

    private static int selectPortNumber()
    {
	synchronized (allocatedPortNumbers)
	{
	    for (int currentPortNumber = INITIAL_PORT_NUMBER_FOR_EMULATOR; currentPortNumber < MAXIMUM_PORT_NUMBER; currentPortNumber++)
	    {
		if (!allocatedPortNumbers.containsKey(currentPortNumber))
		{
		    allocatedPortNumbers.put(currentPortNumber,
			    currentPortNumber);
		    return currentPortNumber;
		}
	    }
	}
	return 0;
    }

    void shutdown() throws IOException, InterruptedException
    {
	try
	{
	    uninstallApplication(previouslyRunApplication);
	} catch (Exception ignored)
	{
	}
	int emulatorPortNumber = getPortNumber();
	Command command = new Command(
		"cmd.exe /C echo kill | C:\\AndroidMigration\\nc111nt\\nc.exe -w 2 localhost "
			+ emulatorPortNumber);
	command.execute();
	List<String> resultList = command.getProcessResult();
	if (resultList.get(resultList.size() - 1).equals(
		SUCCESSFULL_SHUTDOWN_MESSAGE))
	{
	    new File(sdCardImagePathString).delete();
	    synchronized (allocatedPortNumbers)
	    {
		allocatedPortNumbers.remove(serverAgentsPortNumber);
	    }
	}
    }

    public int getPortNumber()
    {
	String numberString = serialNumber.split("-")[1];
	return Integer.parseInt(numberString);
    }

    public int getServerAgentsPortNumber()
    {
	return serverAgentsPortNumber;
    }

    public void uninstallApplication(String packageName) throws IOException,
	    InterruptedException
    {
	String uninstallCommandString = "adb -s " + serialNumber
		+ " uninstall " + packageName;
	Command uninstallCommand = new Command(uninstallCommandString);
	uninstallCommand.execute();
    }

    String getPreviouslyRunApplicationPackageName()
    {
	return previouslyRunApplication;
    }

    void setPreviouslyRunApplicationPackageName(String currentApplication)
    {
	previouslyRunApplication = currentApplication;
    }

}
