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
