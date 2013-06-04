package org.applicationMigrator.applicationServer.emulatorManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Command
{
    private String command;
    private Process executedProcess;

    public Command(String command)
    {
	this.command = command;
	this.executedProcess = null;
    }

    public void execute() throws IOException, InterruptedException
    {
	Runtime runtime = Runtime.getRuntime();
	executedProcess = runtime.exec(command);
	executedProcess.getErrorStream().close();
	/*InputStream stderr = executedProcess.getErrorStream();
	InputStreamReader isr = new InputStreamReader(stderr);
	BufferedReader br = new BufferedReader(isr);
	String line = null;
	if (br.ready())
	{
	    System.out.println("<ERROR>");
	    while ((line = br.readLine()) != null)
		System.out.println(line);
	    System.out.println("</ERROR>");
	}*/
	int exitCode = executedProcess.waitFor();
	if (exitCode != 0)
	{
	    throw new InterruptedException("Failed to Execute");
	}
    }

    public List<String> getProcessResult() throws IOException
    {
	if (executedProcess == null)
	    throw new IOException("No result");
	List<String> result = new ArrayList<String>();
	BufferedReader bufferedReader = new BufferedReader(
		new InputStreamReader(executedProcess.getInputStream()));
	String lineString;
	while ((lineString = bufferedReader.readLine()) != null)
	    result.add(lineString);
	return result;
    }

}
