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
package org.applicationMigrator.applicationServer.communication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//Job: To provide a wrapper around a Socket so as to send and receive Objects
public class ServerSocketConnection
{
    private Socket connection;
    public static String WORK_FINISHED_MESSAGE = "DONE";

    public ServerSocketConnection(Socket connection)
    {
	this.connection = connection;
    }

    public void sendObjectToClient(Object object) throws IOException
    {
	ObjectOutputStream out = new ObjectOutputStream(
		new BufferedOutputStream(connection.getOutputStream()));
	out.writeObject(object);
	out.flush();
    }

    public Object getObjectFromClient() throws IOException,
	    ClassNotFoundException
    {
	ObjectInputStream objectInputStream = new ObjectInputStream(
		connection.getInputStream());
	Object receivedObject = objectInputStream.readObject();
	return receivedObject;

    }

    public void sendWorkFinishedMessage() throws IOException
    {
	sendObjectToClient(WORK_FINISHED_MESSAGE);
    }

    public void sendObjectsList(List<Object> resultObjects) throws IOException
    {
	for (Object object : resultObjects)
	{
	    sendObjectToClient(object);
	}
	sendWorkFinishedMessage();
    }

    public List<Object> getObjectsList() throws ClassNotFoundException,
	    IOException
    {
	List<Object> recievedObjectsList = new ArrayList<Object>();
	Object recievedObject;
	while (true)
	{
	    recievedObject = getObjectFromClient();
	    if (recievedObject
		    .equals(WORK_FINISHED_MESSAGE))
		break;
	    recievedObjectsList.add(recievedObject);
	}
	return recievedObjectsList;

    }

}