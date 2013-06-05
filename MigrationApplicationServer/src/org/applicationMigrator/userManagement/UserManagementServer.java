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
package org.applicationMigrator.userManagement;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

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
