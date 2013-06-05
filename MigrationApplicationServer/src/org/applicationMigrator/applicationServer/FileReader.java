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
