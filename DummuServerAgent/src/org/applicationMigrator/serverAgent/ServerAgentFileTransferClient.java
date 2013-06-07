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
package org.applicationMigrator.serverAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class ServerAgentFileTransferClient {
	private static final String BUCKET_NAME = "application.migrater.bucket";
	private String applicationName;
	private String userName;

	public ServerAgentFileTransferClient(String applicationName,String userName) {
		this.applicationName = applicationName;
		this.userName=userName;
	}

	public void downloadFiles(String outputFilesPaths[],
			String credentialsFilePath) throws IOException,
			InterruptedException {
		if (outputFilesPaths == null) {
			return;
		}
		AWSCredentials awsCredentials = getCredentials(credentialsFilePath);
		Environment.getExternalStorageDirectory().setReadable(true, false);
		Environment.getExternalStorageDirectory().setWritable(true,false);
			
		for (String outputFilePath : outputFilesPaths) {
			String fileName = getFileName(outputFilePath);
			String keyNameString = userName + "/" + applicationName + "/"
					+ fileName;
			AmazonS3 s3client = new AmazonS3Client(awsCredentials);
			S3Object s3Object = s3client.getObject(new GetObjectRequest(
					BUCKET_NAME, keyNameString));
			try {
				writeObjectToFile(s3Object, outputFilePath);
				File outputFile=new File(outputFilePath);
				outputFile.setReadable(true, false);
				outputFile.setWritable(true,false);
				
			} catch (Exception e) {
				/*File erroneousFile = new File(outputFilePath);
				if (erroneousFile.exists())
					erroneousFile.delete();*/
			}
		}
	}

	private void writeObjectToFile(S3Object s3Object, String outputFilePath)
			throws IOException, InterruptedException {
		InputStream inputStream = null;
		inputStream = s3Object.getObjectContent();
		byte[] buf = new byte[1024];
		File outputFile = new File(outputFilePath);
		outputFile.setReadable(true, false);
		outputFile.setWritable(true,false);
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(outputFile);
			int count;
			while ((count = inputStream.read(buf)) != -1) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				outputStream.write(buf, 0, count);
			}
		} finally {
			if (outputStream != null)
				outputStream.close();
			if (inputStream != null)
				inputStream.close();
		}
	}

	public void uploadFiles(String dataFilesPaths[], boolean forceUpload[],
			String credentialsFilePath) throws FileNotFoundException,
			IOException {
		if (dataFilesPaths == null) {
			return;
		}
		AWSCredentials awsCredentials = getCredentials(credentialsFilePath);
		String locationOfFilesString = getLocationString();
		for (int currentFileNumber = 0; currentFileNumber < dataFilesPaths.length; currentFileNumber++) {
			String sourceFilePath = dataFilesPaths[currentFileNumber];
			String fileName = getFileName(sourceFilePath);
			boolean forceUploadCurrentFile = false;
			if (forceUpload != null && currentFileNumber < forceUpload.length)
				forceUploadCurrentFile = forceUpload[currentFileNumber];
			String destinationFilePath = locationOfFilesString + fileName;
			uploadFile(awsCredentials, sourceFilePath, destinationFilePath,
					forceUploadCurrentFile);
		}
	}

	private String getFileName(String filePath) throws FileNotFoundException {
		String[] splittedStrings = filePath.split("/");
		return splittedStrings[splittedStrings.length - 1];
	}

	private String getLocationString() throws FileNotFoundException,IOException
			 {

		if (applicationName == null)
			throw new IOException("No such Application");
		return userName + "/" + applicationName + "/";
	}

	private AWSCredentials getCredentials(String credentialsFilePath)
			throws IOException {
		AWSCredentials awsCredentials;
		try {
			awsCredentials = readCredentialsFromPropertiesFile(credentialsFilePath);
		} catch (FileNotFoundException e) {
			awsCredentials = getCredentialsFromServer();
		}
		return awsCredentials;
	}

	private AWSCredentials getCredentialsFromServer() {
		// TODO Auto-generated method stub

		return null;
	}

	private AWSCredentials readCredentialsFromPropertiesFile(
			String credentialsFilePath) throws FileNotFoundException,
			IOException {
		AWSCredentials credentials = new PropertiesCredentials(new File(
				credentialsFilePath));
		return credentials;
	}

	private void uploadFile(AWSCredentials awsCredentials,
			String sourcePathString, String destinationPathString,
			boolean forceUpload) throws FileNotFoundException {
		// TODO Think about one file being used by many apps (e.g HP1.pdf read
		// through Adobe reader and OpenOffice)
		AmazonS3 s3client = new AmazonS3Client(awsCredentials);
		boolean fileIsPresentOnServer = checkIfFileIsPresentOnServer(s3client,
				BUCKET_NAME, destinationPathString);
		if (fileIsPresentOnServer && !forceUpload)
			return;
		try {
			File file = new File(sourcePathString);
			if (!file.exists())
				throw new FileNotFoundException();
			s3client.putObject(new PutObjectRequest(BUCKET_NAME,
					destinationPathString, file));
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			System.out.println("Error Message: " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code: " + ase.getErrorCode());
			System.out.println("Error Type: " + ase.getErrorType());
			System.out.println("Request ID: " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
			throw ace;
		}
		// TODO:verify completion of upload operation

	}

	public boolean checkIfFileIsPresentOnServer(AmazonS3 s3Client,
			String bucketName, String destinationPathString)
			throws AmazonClientException, AmazonServiceException {
		boolean isValidFile = true;
		try {
			s3Client.getObjectMetadata(bucketName, destinationPathString);
		} catch (AmazonS3Exception s3e) {
			if (s3e.getStatusCode() == 403 || s3e.getStatusCode() == 404 ) {
				isValidFile = false;
			} else {
				throw s3e; // rethrow all S3 exceptions other than 404
			}
		} catch (Exception e) {
			return false;
		}

		return isValidFile;
	}

}
