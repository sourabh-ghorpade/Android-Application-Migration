package org.applicationMigrator.userManagement;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import org.applicationMigrator.applicationServer.emulatorManagement.EmulatorPoolManager;

public class UserManagementServer implements Runnable
{
    private static final int PORT_NUMBER = 7070;
    private ServerSocket serverSocket;
    private static UserManagementServer ourInstance;

    public static UserManagementServer getInstance() throws IOException
    {
	if (ourInstance == null)
	    ourInstance = new UserManagementServer(PORT_NUMBER);
	return ourInstance;
    }

    private UserManagementServer(int portNumber) throws IOException
    {
	serverSocket = new ServerSocket(portNumber);
    }

    @Override
    public void run()
    {
	try
	{
	    System.out.println("User Management Server Started.");
	    Socket connection;
	    while (true)
	    {
		connection = serverSocket.accept();
		System.out.println(connection.toString() + new Date());
		UserManagementWorker worker=new UserManagementWorker(connection);
		new Thread(worker).start();
	    }
	} catch (Exception e)
	{
	    e.printStackTrace();
	    System.exit(-2);
	}
    }
}
