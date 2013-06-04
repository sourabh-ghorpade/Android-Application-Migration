package org.applicationMigrator.serverAgent.communication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ObjectTransmitter implements Runnable
{

    private Socket connection;
    private Object objectToBeTransmitted;
    private IOException transmissionException;

    public ObjectTransmitter(Socket connection, Object objectToBeTransmitted)
    {
	this.connection = connection;
	this.objectToBeTransmitted = objectToBeTransmitted;
	transmissionException = null;
    }

    public void run()
    {
	try
	{
	    sendObject();
	} catch (IOException e)
	{
	    transmissionException = e;
	}
    }

    public boolean transmissionSuccessful() throws IOException
    {
	if (transmissionException == null)
	    return true;
	throw transmissionException;
    }

    private void sendObject() throws IOException
    {
	ObjectOutputStream out = new ObjectOutputStream(
		new BufferedOutputStream(connection.getOutputStream()));
	out.writeObject(objectToBeTransmitted);
	out.flush();
    }

}
