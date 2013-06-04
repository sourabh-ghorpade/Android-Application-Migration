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
