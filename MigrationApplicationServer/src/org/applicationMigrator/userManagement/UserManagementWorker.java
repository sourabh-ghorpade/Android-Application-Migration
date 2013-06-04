package org.applicationMigrator.userManagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.naming.NameNotFoundException;

import org.applicationMigrator.applicationServer.communication.ServerSocketConnection;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.policy.Action;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.CreateUserResult;
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserRequest;
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest;

public class UserManagementWorker implements Runnable
{

    private static final String GET_CREDENTIALS = "G_CRED";
    private static final String USER_LIST_FILEPATH = "C:\\AndroidMigration\\UserList\\Credentials.txt";
    private static final String BUCKET_NAME = "arn:aws:s3:::application.migrater.bucket";
    private ServerSocketConnection connectionwithClient;

    public UserManagementWorker(Socket connection) throws IOException
    {
	connectionwithClient = new ServerSocketConnection(connection);
    }

    @Override
    public void run()
    {
	try
	{
	    String commandString = (String) connectionwithClient
		    .getObjectFromClient();
	    if (commandString.equals(GET_CREDENTIALS))
	    {
		final String ANDROID_ID = (String) connectionwithClient
			.getObjectFromClient();

		List<Object> credentials;
		try
		{
		    credentials = getCredentialsFromFile(ANDROID_ID);
		} catch (NameNotFoundException e)
		{
		    createUser(ANDROID_ID);
		    try
		    {
			credentials = getCredentialsFromFile(ANDROID_ID);
		    } catch (NameNotFoundException e1)
		    {
			connectionwithClient.sendObjectToClient("ERROR");
			return;
		    }
		}
		connectionwithClient.sendObjectsList(credentials);
	    }
	} catch (ClassNotFoundException | IOException e)
	{
	    try
	    {
		connectionwithClient.sendObjectToClient("ERROR");
	    } catch (IOException ignored)
	    {
	    }
	}
    }

    private List<Object> getCredentialsFromFile(String ANDROID_ID)
	    throws IOException, NameNotFoundException
    {
	org.applicationMigrator.applicationServer.FileReader fileReader = new org.applicationMigrator.applicationServer.FileReader(
		USER_LIST_FILEPATH);
	try
	{
	    while (true)
	    {
		String word = fileReader.readWord();
		if (word.equals(""))
		    break;
		if (word.equals(ANDROID_ID))
		{
		    String accessKey = fileReader.readWord();
		    String secretKey = fileReader.readWord();
		    String userName = fileReader.readWord();
		    List<Object> credentialsList = new ArrayList<Object>();
		    credentialsList.add(accessKey);
		    credentialsList.add(secretKey);
		    credentialsList.add(userName);
		    return credentialsList;
		}
	    }
	} finally
	{
	    fileReader.close();
	}
	throw new NameNotFoundException();
    }

    private void createUser(String ANDROID_ID) throws FileNotFoundException,
	    IllegalArgumentException, IOException
    {
	Random randomizer = new Random(System.currentTimeMillis());
	String userName = "User" + randomizer.nextDouble();
	CreateUserRequest user = new CreateUserRequest();
	user.setUserName(userName);
	AWSCredentials credentials = new PropertiesCredentials(new File(
		"C:\\AndroidMigration\\Credentials\\AwsCredentials.properties"));
	AmazonIdentityManagementClient client = new AmazonIdentityManagementClient(
		credentials);
	CreateUserResult result = null;
	AccessKey accessKey = null ;
	try
	{

	    boolean userCreatedSuccessfully = false;
	    while (!userCreatedSuccessfully)
	    {
		try
		{
		    result = client.createUser(user);
		    userCreatedSuccessfully = true;
		} catch (EntityAlreadyExistsException exception)
		{
		    user.setUserName(userName + randomizer.nextDouble());
		    userCreatedSuccessfully = false;
		}
	    }

	    CreateAccessKeyRequest accessKeyRequest = new CreateAccessKeyRequest();
	    accessKeyRequest.setUserName(result.getUser().getUserName());
	    CreateAccessKeyResult accessKeyResult = client
		    .createAccessKey(accessKeyRequest);
	    accessKey= accessKeyResult.getAccessKey();

	    grantPermissions(user, client);

	    File userList = new File(USER_LIST_FILEPATH);
	    BufferedWriter userListFileWriter = new BufferedWriter(
		    new FileWriter(userList));

	    // Concurrency ?
	    userListFileWriter.write(ANDROID_ID + " ");
	    userListFileWriter.write(accessKey.getAccessKeyId() + " ");
	    userListFileWriter.write(accessKey.getSecretAccessKey() + " ");
	    userListFileWriter.write(user.getUserName() + " ");
	    userListFileWriter.close();
	} catch (Exception e)
	{
	    if(accessKey!=null)
	    {
	    DeleteAccessKeyRequest deleteAccessKeyRequest=new DeleteAccessKeyRequest(accessKey.getAccessKeyId());
	    deleteAccessKeyRequest.setUserName(user.getUserName());
	    client.deleteAccessKey(deleteAccessKeyRequest);
	    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(
		    user.getUserName());
	    
	    client.deleteUser(deleteUserRequest);
	    }
	    throw e;
	}
    }

    public void grantPermissions(CreateUserRequest user,
	    AmazonIdentityManagementClient client)
    {
	Resource resource = new Resource(BUCKET_NAME + "/" + user.getUserName()
		+ "/*");
	Statement statement = new Statement(Effect.Allow);

	Action deleteObjectAction = S3Actions.DeleteObject;
	Action getObjectaAction = S3Actions.GetObject;
	Action putObjectAction = S3Actions.PutObject;

	Collection<Action> actions = new ArrayList<Action>();
	actions.add(deleteObjectAction);
	actions.add(getObjectaAction);
	actions.add(putObjectAction);
	
	statement.setActions(actions);
	Collection<Resource> resources = new ArrayList<Resource>();
	resources.add(resource);

	statement.setResources(resources);
	Policy userPolicy = new Policy();
	
	Collection<Statement> statements = new ArrayList<Statement>();
	statements.add(statement);
	userPolicy.setStatements(statements);

	PutUserPolicyRequest putUserPolicyRequest = new PutUserPolicyRequest();
	putUserPolicyRequest.setPolicyDocument(userPolicy.toJson());
	putUserPolicyRequest.setPolicyName(new Date().getTime() + "Policy");
	putUserPolicyRequest.setUserName(user.getUserName());
	client.putUserPolicy(putUserPolicyRequest);
    }
}
