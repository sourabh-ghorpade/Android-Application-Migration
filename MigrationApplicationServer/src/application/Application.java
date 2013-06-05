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
