package com.limewire.eclipse.plugin;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	public void earlyStartup() {
		LimeWirePlugin.log("Early startup installing configuration listener");
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchConfigurationListener(new LimeWireConfigurationListener());
	}

}
