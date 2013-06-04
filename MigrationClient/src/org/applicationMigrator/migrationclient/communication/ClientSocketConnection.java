package org.applicationMigrator.migrationclient.communication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSocketConnection {
	public static String WORK_FINISHED_MESSAGE = "DONE";
	private Socket connection;

	public ClientSocketConnection(String hostname, int portNumber)
			throws IOException {
		InetAddress address = InetAddress.getByName(hostname);
		connection = new Socket(address, portNumber);
	}

	public void sendObjectToServer(Object object) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new BufferedOutputStream(connection.getOutputStream()));
		objectOutputStream.writeObject(object);
		objectOutputStream.flush();
	}

	public Object getObjectFromServer() throws IOException,
			ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(
				connection.getInputStream());
		Object receivedObject = objectInputStream.readObject();
		return receivedObject;
	}

	public void sendObjectsListToServer(List<Object> intentParameters)
			throws IOException {

		for (Object parameterObject : intentParameters) {
			sendObjectToServer(parameterObject);
		}
		sendObjectToServer("DONE");
	}

	public List<Object> getObjectsListFromServer() throws IOException,
			ClassNotFoundException {
		Object recievedObject;
		List<Object> recievedObjects = new ArrayList<Object>();
		while (true) {
			recievedObject = getObjectFromServer();
			if (recievedObject.equals(WORK_FINISHED_MESSAGE))
				break;

			recievedObjects.add(recievedObject);
		}
		return recievedObjects;
	}
}
