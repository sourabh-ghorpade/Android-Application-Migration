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
package org.applicationMigrator.serverAgent.communication;

import java.io.IOException;
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

    public void sendObjectToClient(Object objectToBeTransmitted) throws IOException, InterruptedException
    {
	ObjectTransmitter objectTransmitter=new ObjectTransmitter(connection,objectToBeTransmitted);
	Thread transmissionThread=new Thread(objectTransmitter);
	transmissionThread.start();
	transmissionThread.join();
	objectTransmitter.transmissionSuccessful();
    }

    public Object getObjectFromClient() throws Exception
    {
	ObjectReceiver objectReceiver=new ObjectReceiver(connection);
	Thread receivingThread=new Thread(objectReceiver);
	receivingThread.start();
	receivingThread.join();
	return objectReceiver.getReceivedObject();
    }

    public void sendWorkFinishedMessage() throws IOException, InterruptedException
    {
	sendObjectToClient(WORK_FINISHED_MESSAGE);
    }

    public void sendObjectsList(List<Object> resultObjects) throws IOException, InterruptedException
    {
	for (Object object : resultObjects)
	{
	    sendObjectToClient(object);
	}
	sendWorkFinishedMessage();
    }

    public List<Object> getObjectsList() throws Exception
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

    public void close() throws IOException
    {
	connection.close();
    }
}