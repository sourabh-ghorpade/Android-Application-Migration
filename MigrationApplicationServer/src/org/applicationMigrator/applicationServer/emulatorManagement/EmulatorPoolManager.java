package org.applicationMigrator.applicationServer.emulatorManagement;

import javax.naming.directory.InvalidAttributesException;

import org.omg.CORBA.NO_RESOURCES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class EmulatorPoolManager
{
    private static final String NO_COMPATIBLE_EMULATOR_FOUND = "No Compatible Emulator Found";
    private static final String NO_APPLICATION = "";
    private static EmulatorPoolManager ourInstanceEmulatorPoolManager = null;
    private List<Emulator> freeEmulatorsList;
    private Thread resourceManagerThread;
    private EmulatorPoolLoadBalancer loadBalancer;
    private int numberOfEmulatorsInUse;
    private Hashtable<Integer, Integer> allocatedPortNumbers;
    private Object numberOfEmulatorsCounterLock;

    public static EmulatorPoolManager getInstance()
    {
	if (ourInstanceEmulatorPoolManager == null)
	{
	    try
	    {
		ourInstanceEmulatorPoolManager = new EmulatorPoolManager();
	    } catch (InterruptedException e)
	    {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
	return ourInstanceEmulatorPoolManager;
    }

    private EmulatorPoolManager() throws InterruptedException
    {
	freeEmulatorsList = new ArrayList<Emulator>();
	allocatedPortNumbers = new Hashtable<Integer, Integer>();
	numberOfEmulatorsInUse = 0;
	numberOfEmulatorsCounterLock = new Object();
	loadBalancer = new EmulatorPoolLoadBalancer();
	startLoadBalancer();
    }

    public Emulator allocateEmulator(
	    HashMap<String, String> applicationRequirements,
	    String currentApplicationPackageName,
	    String setupPathOfCurrentApplication) throws Exception
    {
	Emulator selectedEmulator;
	if (freeEmulatorsList.isEmpty())
	    selectedEmulator = loadBalancer.getEmulator();
	else
	{
	    List<Emulator> emulators = findCompatibleEmulators(applicationRequirements);
	    if (emulators.isEmpty())
	    {
		passMessageToPrioritySchedular(applicationRequirements);
		throw new Exception(NO_COMPATIBLE_EMULATOR_FOUND);
	    }
	    selectedEmulator = selectOptimalEmulator(emulators,
		    currentApplicationPackageName);
	    synchronized (freeEmulatorsList)
	    {
		freeEmulatorsList.remove(selectedEmulator);
	    }
	}

	if (!selectedEmulator.getPreviouslyRunApplicationPackageName().equals(
		currentApplicationPackageName))
	{
	    String previousApplicationPackageName = selectedEmulator
		    .getPreviouslyRunApplicationPackageName();
	    if (!previousApplicationPackageName.equals(NO_APPLICATION))
		selectedEmulator
			.uninstallApplication(previousApplicationPackageName);
	    selectedEmulator.installApplication(setupPathOfCurrentApplication);
	    selectedEmulator
		    .setPreviouslyRunApplicationPackageName(currentApplicationPackageName);
	}
	synchronized (numberOfEmulatorsCounterLock)
	{
	    selectedEmulator.setInUse(true);
	    numberOfEmulatorsInUse++;
	}
	return selectedEmulator;
    }

    public Emulator selectOptimalEmulator(List<Emulator> emulators,
	    String currentApplicationPackageName)
    {
	int FIRST_EMULATOR = 0;
	for (Emulator emulator : emulators)
	{
	    if (emulator.getPreviouslyRunApplicationPackageName().equals(
		    currentApplicationPackageName))
		return emulator;
	}
	Emulator selectedEmulator = emulators.get(FIRST_EMULATOR);
	return selectedEmulator;
    }

    private void passMessageToPrioritySchedular(
	    HashMap<String, String> applicationRequirements)
    {

    }

    private List<Emulator> findCompatibleEmulators(
	    HashMap<String, String> applicationRequirements)
    {
	List<Emulator> compatibleEmulators = new ArrayList<Emulator>();
	synchronized (freeEmulatorsList)
	{
	    for (Emulator emulator : freeEmulatorsList)
	    {
		if (emulator.isCompatible(applicationRequirements))
		    compatibleEmulators.add(emulator);
	    }
	}
	return compatibleEmulators;
    }

    public void startLoadBalancer()
    {
	resourceManagerThread = new Thread(loadBalancer);
	resourceManagerThread.start();
    }

    public void freeEmulator(Emulator emulator, String applicationName)
	    throws IOException, InterruptedException
    {
	emulator.setInUse(false);
	emulator.runApplication(Emulator.SERVER_AGENT_PACKAGE_NAME,
		Emulator.SERVER_AGENT_ACTIVITY_NAME);
	synchronized (freeEmulatorsList)
	{
	    freeEmulatorsList.add(emulator);
	    synchronized (numberOfEmulatorsCounterLock)
	    {
		numberOfEmulatorsInUse--;
	    }
	}
    }

    private int allocatePortNumber() throws NO_RESOURCES
    {
	final int FIRST_POSSIBLE_PORT_NUMBER = 5554, LAST_POSSIBLE_PORT_NUMBER = 5584;
	synchronized (allocatedPortNumbers)
	{
	    for (int currentPortNumber = FIRST_POSSIBLE_PORT_NUMBER; currentPortNumber < LAST_POSSIBLE_PORT_NUMBER; currentPortNumber += 2)
	    {
		if (!allocatedPortNumbers.contains(currentPortNumber))
		{
		    allocatedPortNumbers.put(currentPortNumber,
			    currentPortNumber);
		    return currentPortNumber;
		}
	    }
	}
	throw new NO_RESOURCES("No Port Available");
    }

    private void freePortNumber(int portNumber)
    {
	synchronized (allocatedPortNumbers)
	{
	    allocatedPortNumbers.remove(portNumber);
	}
    }

    private class EmulatorPoolLoadBalancer implements Runnable
    {
	private double previousLoadRatio;
	private List<Emulator> launchedEmulatorsList;
	private static final double MAXIMUM_LOAD_RATIO_CHANGE_PERMISSABLE = .15;
	private static final double MINIMUM_LOAD_RATIO_CHANGE_PERMISSABLE = .05;
	private static final int MINUMUM_NUMBER_OF_RUNNING_EMULATORS = 1;
	private static final double MINIMUM_LOAD_RATIO = 0.4;
	private static final double MAXIMUM_LOAD_PERMISSABLE = 0.8;
	private static final double BUFFER = .05;
	private static final String EMULATOR_NAME = "Server.";

	public EmulatorPoolLoadBalancer()
	{
	    launchedEmulatorsList = new ArrayList<Emulator>();
	    previousLoadRatio = 0;
	}

	public Emulator getEmulator() throws Exception
	{
	    EmulatorStarter emulatorStarter = new EmulatorStarter(EMULATOR_NAME);
	    return emulatorStarter.startEmulator();

	}

	@Override
	public void run()
	{
	    try
	    {
		killAllRunningEmulators();

	    } catch (Exception e)
	    {
		System.exit(-1);
	    }
	    try
	    {
		runEmulatorLoadBalencer();
	    } catch (InvalidAttributesException e)
	    {
		e.printStackTrace();
	    } catch (InterruptedException e)
	    {
		e.printStackTrace();
	    } catch (IOException e)
	    {
		e.printStackTrace();
	    }

	}

	private void runEmulatorLoadBalencer()
		throws InvalidAttributesException, IOException,
		InterruptedException
	{
	    startMinimumNumberOfEmulators();
	    long sleepPeriod = 120000000;
	    while (true)
	    {
		try
		{
		    double currentLoadRatio = balanceLoad();
		    double changeInLoadRatio;
		    if (currentLoadRatio < previousLoadRatio)
			changeInLoadRatio = previousLoadRatio
				- currentLoadRatio;
		    else
			changeInLoadRatio = currentLoadRatio
				- previousLoadRatio;
		    if (changeInLoadRatio > MAXIMUM_LOAD_RATIO_CHANGE_PERMISSABLE)
			sleepPeriod -= (sleepPeriod * changeInLoadRatio);
		    else if (currentLoadRatio < MINIMUM_LOAD_RATIO_CHANGE_PERMISSABLE)
			sleepPeriod += (sleepPeriod * changeInLoadRatio);
		    Thread.sleep(sleepPeriod);
		} catch (InterruptedException e)
		{
		    e.printStackTrace();
		} catch (InvalidAttributesException e)
		{
		    e.printStackTrace();
		} catch (IOException e)
		{
		    e.printStackTrace();
		}
	    }
	}

	private double balanceLoad() throws InvalidAttributesException,
		IOException, InterruptedException
	{
	    int totalEmulators;
	    synchronized (freeEmulatorsList)
	    {
		totalEmulators = numberOfEmulatorsInUse
			+ freeEmulatorsList.size();
	    }
	    if (totalEmulators <= 0)
	    {
		final double NO_LOAD = 0.0;
		startMinimumNumberOfEmulators();
		return NO_LOAD;
	    }
	    double loadRatio = numberOfEmulatorsInUse / totalEmulators;

	    if (loadRatio < MINIMUM_LOAD_RATIO)
	    {
		int numberofEmlatorsToBeShutdown = totalEmulators
			- (int) Math
				.ceil((numberOfEmulatorsInUse / (MINIMUM_LOAD_RATIO + BUFFER)));
		while (numberofEmlatorsToBeShutdown != 0
			&& (totalEmulators > MINUMUM_NUMBER_OF_RUNNING_EMULATORS))
		{
		    removeEmulator();
		    numberofEmlatorsToBeShutdown--;
		    totalEmulators--;
		}
	    } else if (loadRatio > MAXIMUM_LOAD_PERMISSABLE)
	    {
		int numberOfEmulatorsToBeStarted = (int) Math
			.ceil((numberOfEmulatorsInUse / (MAXIMUM_LOAD_PERMISSABLE + BUFFER)))
			- totalEmulators;
		addEmulators(EMULATOR_NAME, numberOfEmulatorsToBeStarted);
	    }
	    return loadRatio;
	}

	private void startMinimumNumberOfEmulators()
		throws InvalidAttributesException, IOException,
		InterruptedException
	{
	    int currentlyRunningEmulators = numberOfEmulatorsInUse
		    + freeEmulatorsList.size();
	    // stub
	    String emulatorName = "Server";
	    // </stub>
	    if (currentlyRunningEmulators < MINUMUM_NUMBER_OF_RUNNING_EMULATORS)
		addEmulators(emulatorName, MINUMUM_NUMBER_OF_RUNNING_EMULATORS
			- currentlyRunningEmulators);
	}

	private void addEmulators(String emulatorName, int numberOfEmulators)
		throws InterruptedException
	{
	    Thread emulatorStarterThreads[] = new Thread[numberOfEmulators];
	    int counter;
	    for (counter = 0; counter < numberOfEmulators; counter++)
	    {
		emulatorStarterThreads[counter] = new Thread(
			new EmulatorStarter(emulatorName));
		emulatorStarterThreads[counter].start();
	    }
	    counter--;
	    while (counter >= 0)
	    {
		emulatorStarterThreads[counter].join();
		counter--;
	    }
	}

	private void removeEmulator() throws IOException, InterruptedException
	{
	    Emulator selectedEmulator = selectEmulatorForShutDown();
	    selectedEmulator.shutdown();
	    synchronized (freeEmulatorsList)
	    {
		synchronized (launchedEmulatorsList)
		{
		    freeEmulatorsList.remove(selectedEmulator);
		    launchedEmulatorsList.remove(selectedEmulator);
		}
	    }
	    freePortNumber(selectedEmulator.getPortNumber());
	}

	private Emulator selectEmulatorForShutDown()
	{
	    return freeEmulatorsList.get(0);
	}

	private void killAllRunningEmulators() throws IOException,
		InterruptedException
	{
	    synchronized (launchedEmulatorsList)
	    {
		for (Emulator emulator : launchedEmulatorsList)
		{
		    emulator.shutdown();
		}
		launchedEmulatorsList.clear();
	    }
	}

	private class EmulatorStarter implements Runnable
	{
	    private String emulatorName;
	    private static final String APP_REPOSITORY_PATH = "C:\\AndroidMigration\\AppRepo";
	    private static final String SERVER_AGENT_APPLICATION_PATH = "\\ServerAgent\\Server_Agent.apk";

	    public EmulatorStarter(String emulatorName)
	    {
		this.emulatorName = emulatorName;
	    }

	    @Override
	    public void run()
	    {
		try
		{
		    Emulator emulator = startEmulator();
		    addEmulatorToFreeEmulatorLists(emulator);
		} catch (Exception ignored)
		{
		    ignored.printStackTrace();
		}
	    }

	    private void addEmulatorToFreeEmulatorLists(Emulator emulator)
	    {
		synchronized (freeEmulatorsList)
		{
		    freeEmulatorsList.add(emulator);
		}
	    }

	    public Emulator startEmulator() throws Exception
	    {
		Emulator emulator;
		int emulatorPortNumber = EmulatorPoolManager.this
			.allocatePortNumber();
		try
		{
		    emulator = Emulator.startEmulator(emulatorName,
			    emulatorPortNumber, APP_REPOSITORY_PATH
				    + SERVER_AGENT_APPLICATION_PATH);
		} catch (Exception e)
		{
		    EmulatorPoolManager.this.freePortNumber(emulatorPortNumber);
		    throw e;
		}
		synchronized (launchedEmulatorsList)
		{
		    launchedEmulatorsList.add(emulator);
		}
		return emulator;
	    }
	}
    }
}
