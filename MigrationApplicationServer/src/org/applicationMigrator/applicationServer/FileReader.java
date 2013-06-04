package org.applicationMigrator.applicationServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileReader {

	private BufferedReader bufferedReader;

	public FileReader(String filePath) throws FileNotFoundException {
		bufferedReader = new BufferedReader(new java.io.FileReader(filePath));
	}

	public String readWord() throws IOException {
		StringBuilder stringBuilder=new StringBuilder(); 
		char temporaryCharacter;
		while (true) {
			int readValue=bufferedReader.read();
			if(readValue==-1)
				break;
			temporaryCharacter =(char) readValue;
			if (!Character
					.isWhitespace(temporaryCharacter) || temporaryCharacter=='\n') {
				stringBuilder.append(temporaryCharacter);
			}
			else
				break;
		}
		return stringBuilder.toString();
	}
	
	public void close() throws IOException
	{
	    bufferedReader.close();
	    
	}

}
