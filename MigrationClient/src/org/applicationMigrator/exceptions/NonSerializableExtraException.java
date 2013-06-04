package org.applicationMigrator.exceptions;

public class NonSerializableExtraException extends Exception
{
	private static final long serialVersionUID = -5171312486806964975L;

	private static final String MESSAGE = "Cannot Send this Extra via Network";

	private String nonSerializableExtraName;
	
	public NonSerializableExtraException(String nameOfExtra)
	{
		nonSerializableExtraName=nameOfExtra;
	}
	
	@Override
	public String getMessage() {
		return MESSAGE;
	}
	
}
