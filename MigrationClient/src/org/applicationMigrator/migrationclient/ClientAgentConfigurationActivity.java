package org.applicationMigrator.migrationclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class ClientAgentConfigurationActivity extends Activity 
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_agent_home);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_client_agent_home, menu);
		return true;
	}
}
