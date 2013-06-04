package org.BEProject.org.BEProject.migrationApplicationServer.emulatorManagement;

import java.io.IOException;

import org.applicationMigrator.applicationServer.FileReader;

public class TestFileReader
{

    public static void shouldReadFile() throws IOException
    {
	String indexFilePath = "C:\\AndroidMigration\\AppRepo\\installedAppsList.txt";
	FileReader fileReader=new FileReader(indexFilePath);
	System.out.println(fileReader.readWord());
	System.out.println(fileReader.readWord());
	System.out.println(fileReader.readWord());
    }
    
    public static void main(String ared[]) throws IOException
    {
	shouldReadFile();
    }
    
    
}
