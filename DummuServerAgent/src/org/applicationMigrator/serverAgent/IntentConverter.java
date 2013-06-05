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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.applicationMigrator.exceptions.NonSerializableExtraException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

public class IntentConverter
{
    public static Intent convertObjectsToIntent(List<Object> recievedObjects)
    {
	Bundle recievedExtrasBundle = new Bundle();
	for (int counter = 0; counter < recievedObjects.size() - 1; counter += 2)
	{
	    String parameterNameString = (String) recievedObjects.get(counter);
	    try
	    {
		Serializable recievedSerializableObject = (Serializable) recievedObjects
			.get(counter + 1);
		recievedExtrasBundle.putSerializable(parameterNameString,
			recievedSerializableObject);
	    } catch (Exception e)
	    {
		Parcelable recievedParcelableObject = (Parcelable) recievedObjects
			.get(counter + 1);
		recievedExtrasBundle.putParcelable(parameterNameString,
			recievedParcelableObject);
	    }

	}
	return new Intent().putExtras(recievedExtrasBundle);
    }

    public static List<Object> convertIntentToObjects(Intent intent)
	    throws NonSerializableExtraException
    {
	List<Object> intentParameterList = new ArrayList<Object>();
	Bundle extrasBundle = intent.getExtras();
	if (extrasBundle == null)
	    return intentParameterList;
	Set<String> parametersName = extrasBundle.keySet();

	for (String parameterName : parametersName)
	{
	    Object parameterObject = intent.getSerializableExtra(parameterName);
	    if (parameterObject == null)
	    {
		parameterObject = intent.getParcelableExtra(parameterName);
		if (parameterObject == null)
		    throw new NonSerializableExtraException(parameterName);
	    }
	    intentParameterList.add(parameterName);
	    intentParameterList.add(parameterObject);
	}
	return intentParameterList;
    }

}
