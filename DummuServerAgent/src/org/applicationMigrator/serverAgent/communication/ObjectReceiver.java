package org.applicationMigrator.serverAgent.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;

class ObjectReceiver implements Runnable
{

    private Socket connection;
    private Object receivedObject;
    private Exception transmissionException;

    public ObjectReceiver(Socket connection)
    {
	this.connection=connection;
    }

    public void run()
    {
	try
	{
	    receivedObject=receiveObject();
	} catch (Exception e)
	{
	    transmissionException=e;
	}
	
    }
    
    public Object getReceivedObject() throws Exception
    {
	if(transmissionException==null)
	    return receivedObject;
	throw transmissionException;
    }

    private Object receiveObject() throws InterruptedException, StreamCorruptedException, IOException, ClassNotFoundException
    {
	ObjectInputStream objectInputStream = new ObjectInputStream(
		connection.getInputStream());
	Object receivedObject = objectInputStream.readObject();
	return receivedObject;
    }

}
