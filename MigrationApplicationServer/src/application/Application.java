package application;

import java.io.IOException;

import org.applicationMigrator.applicationServer.Server;
import org.applicationMigrator.userManagement.UserManagementServer;

public class Application
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
	Server server=Server.getInstance();
	UserManagementServer userManagementServer=UserManagementServer.getInstance();
		Thread serverThread=new Thread(server);
	Thread userManagementThread=new Thread(userManagementServer);
	serverThread.start();
	userManagementThread.start();
	serverThread.join();
	userManagementThread.join();
    }
}
