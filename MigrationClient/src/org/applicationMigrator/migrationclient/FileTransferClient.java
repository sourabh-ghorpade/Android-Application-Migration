package org.applicationMigrator.migrationclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.naming.directory.NoSuchAttributeException;

import android.os.Environment;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class FileTransferClient {
	private static final String BUCKET_NAME = "application.migrater.bucket";
	private String applicationName;
	private User currentUser;

	public FileTransferClient(String applicationName,
			String credentialsFilePath, String ANDROID_ID)
			throws ClassNotFoundException, IOException {
		this.applicationName = applicationName;
		currentUser = new User(ANDROID_ID, credentialsFilePath);
	}

	public void downloadFiles(String outputFilesPaths[]) throws IOException,
			InterruptedException, ClassNotFoundException {
		if (outputFilesPaths == null) {
			return;
		}
		AWSCredentials awsCredentials = currentUser.getAwsCredentials();
		Environment.getExternalStorageDirectory().setReadable(true, false);
		Environment.getExternalStorageDirectory().setWritable(true, false);

		for (String outputFilePath : outputFilesPaths) {
			String fileName = getFileName(outputFilePath);
			String userName = currentUser.getUserNameString();
			String keyNameString = userName + "/" + applicationName + "/"
					+ fileName;
			AmazonS3 s3client = new AmazonS3Client(awsCredentials);
			S3Object s3Object = s3client.getObject(new GetObjectRequest(
					BUCKET_NAME, keyNameString));
			try {
				writeObjectToFile(s3Object, outputFilePath);
				File outputFile = new File(outputFilePath);
				outputFile.setReadable(true, false);
				outputFile.setWritable(true, false);

			} catch (Exception ignored) {
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
		outputFile.setWritable(true, false);
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

	public void uploadFiles(String dataFilesPaths[], boolean forceUpload[])
			throws FileNotFoundException, IOException,
			NoSuchAttributeException, ClassNotFoundException {
		AWSCredentials awsCredentials = currentUser.getAwsCredentials();
		if (dataFilesPaths == null) {
			return;
		}
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

	private String getLocationString() throws NoSuchAttributeException,
			IOException {

		if (applicationName == null)
			throw new NoSuchAttributeException();
		String userName = currentUser.getUserNameString();
		return userName + "/" + applicationName + "/";
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
			throw ase;
		} catch (AmazonClientException ace) {
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
			if (s3e.getStatusCode() == 403) {
				isValidFile = false;
			} else {
				throw s3e; // rethrow all S3 exceptions other than 404
			}
		} catch (Exception e) {
			return false;
		}

		return isValidFile;
	}
	
	public String getUserName()
	{
		return currentUser.getUserNameString();
	}

}
