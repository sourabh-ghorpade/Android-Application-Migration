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
package org.applicationMigrator.migrationclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.applicationMigrator.migrationclient.communication.ClientSocketConnection;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

public class User {

	private AWSCredentials awsCredentials;
	private String userNameString;

	private static final String GET_CREDENTIALS = "G_CRED";
	private static final int ACCESS_KEY_POSITION = 0;
	private static final int SECRET_KEY_POSITION = 1;
	private static final int USER_NAME_POSITION = 2;
	private static final int USER_MANAGEMENT_SERVER_PORT_NUMBER = 7070;
	private final String ANDROID_ID;
	private String credentialsFilePath;

	public User(String ANDROID_ID,String credentialsFilePath) throws ClassNotFoundException, IOException 
	{
		this.ANDROID_ID=ANDROID_ID;
		this.credentialsFilePath=credentialsFilePath;
		this.awsCredentials=getCredentials();
		this.userNameString=getUserNameFromFile();
	}

	private String getUserNameFromFile() throws IOException {
		int lineNumberOfUserNameField = 2;
		File propertiesFile = new File(credentialsFilePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(propertiesFile)));
		for (int lineNumber = 0; lineNumber < lineNumberOfUserNameField; lineNumber++)
			reader.readLine();
		String userNameString = reader.readLine();
		reader.close();
		return userNameString.split("=")[1];
	}

	private AWSCredentials getCredentials()
			throws IOException, ClassNotFoundException {
		AWSCredentials awsCredentials;
		try {
			awsCredentials = readCredentialsFromPropertiesFile(credentialsFilePath);
		} catch (FileNotFoundException e) {
			List<Object> registerationDetails = getCredentialsFromServer();
			createCredentialsFile(registerationDetails, credentialsFilePath);
			awsCredentials = readCredentialsFromPropertiesFile(credentialsFilePath);
		}
		return awsCredentials;
	}

	private void createCredentialsFile(List<Object> registerationDetails,
			String credentialsFilePath) throws IOException {
		String accessKey = (String) registerationDetails
				.get(ACCESS_KEY_POSITION);
		String secretKey = (String) registerationDetails
				.get(SECRET_KEY_POSITION);
		String userNameString = (String) registerationDetails
				.get(USER_NAME_POSITION);
		//new File(credentialsFileDirectoryPath).mkdir();
		File credentialsFile = new File(credentialsFilePath);
		
		try {
				
			credentialsFile.createNewFile();
			BufferedWriter credentialsFileWriter = new BufferedWriter(
					new FileWriter(credentialsFile));
			credentialsFileWriter.write("secretKey=" + secretKey);
			credentialsFileWriter.newLine();
			credentialsFileWriter.write("accessKey=" + accessKey);
			credentialsFileWriter.newLine();
			credentialsFileWriter.write("userName=" + userNameString);
			credentialsFileWriter.close();
		} catch (IOException e) {
			credentialsFile.delete();
			throw e;
		}
	}

	private List<Object> getCredentialsFromServer() throws IOException,
			ClassNotFoundException {
		try {
			ClientSocketConnection connection = new ClientSocketConnection(
					ClientAgentHome.HOST_NAME, USER_MANAGEMENT_SERVER_PORT_NUMBER);
			List<Object> registerationDetails=new ArrayList<Object>();
			registerationDetails.add(GET_CREDENTIALS);
			registerationDetails.add(ANDROID_ID);
			connection.sendObjectsListToServer(registerationDetails);
			List<Object> credentialsList = connection
					.getObjectsListFromServer();
			return credentialsList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	private AWSCredentials readCredentialsFromPropertiesFile(
			String credentialsFilePath) throws FileNotFoundException,
			IOException {
		AWSCredentials credentials = new PropertiesCredentials(new File(
				credentialsFilePath));
		return credentials;
	}

	public AWSCredentials getAwsCredentials() {
		return awsCredentials;
	}

	public String getUserNameString() {
		return userNameString;
	}

}
