package org.applicationMigrator.serverAgent;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.applicationMigrator.serverAgent.enums.FileManagementTasks;

public class FileManager implements Runnable
{

    private FileManagementTasks task;
    private ServerAgentFileTransferClient fileTransferClient;
    private String[] dataFilesPaths;
    private boolean[] forceUploadValues;
    private String adminCredentialsPath;

    public FileManager(FileManagementTasks task, String applicationName,
	    String adminCredentialsPath, String[] dataFilesPaths,
	    boolean[] forceUploadValues, String userName)
    {
	this.task = task;
	this.dataFilesPaths = dataFilesPaths;
	this.forceUploadValues = forceUploadValues;
	this.adminCredentialsPath = adminCredentialsPath;
	fileTransferClient = new ServerAgentFileTransferClient(applicationName,
		userName);
    }

    @Override
    public void run()
    {
	if (task == FileManagementTasks.DOWNLOAD)
	{

	} else if (task == FileManagementTasks.UPLOAD)
	{
	    try
	    {
		fileTransferClient.uploadFiles(dataFilesPaths,
			forceUploadValues, adminCredentialsPath);
	    } catch (FileNotFoundException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

}
