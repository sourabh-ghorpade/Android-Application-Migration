package org.applicationMigrator.applicationServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import org.applicationMigrator.applicationServer.emulatorManagement.EmulatorPoolManager;

public class Server implements Runnable
{
    private static final int PORT_NUMBER = 9090;
    private ServerSocket serverSocket;
    private static Server ourInstance;

    public static Server getInstance() throws IOException
    {
	if (ourInstance == null)
	    ourInstance = new Server(PORT_NUMBER);
	return ourInstance;
    }

    private Server(int portNumber) throws IOException
    {
	serverSocket = new ServerSocket(portNumber);
    }

    @Override
    public void run()
    {
	try
	{
	    System.out.println("Server Started.");
	    Socket connection;
	    EmulatorPoolManager.getInstance();
	    while (true)
	    {
		connection = serverSocket.accept();
		System.out.println(connection.toString() + new Date());
		new MigrationWorker(connection).start();
	    }
	} catch (Exception e)
	{
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
}
