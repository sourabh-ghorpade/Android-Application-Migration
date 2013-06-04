package org.BEProject.org.BEProject.migrationApplicationServer.emulatorManagement;

import org.applicationMigrator.applicationServer.emulatorManagement.EmulatorPoolManager;
import org.junit.Test;

public class TestEmulatorPoolManager
{

    @Test
    public void testGetInstance()
    {
	@SuppressWarnings("unused")
	EmulatorPoolManager ourPoolManager=EmulatorPoolManager.getInstance();
    }
/*
    @Test
    public void testAllocateEmulator()
    {
	fail("Not yet implemented");
    }

    @Test
    public void testRun()
    {
	fail("Not yet implemented");
    }

    @Test
    public void testStartResourceManager()
    {
	fail("Not yet implemented");
    }*/

}
