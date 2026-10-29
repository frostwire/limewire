package com.limewire.eclipse.plugin.fisheye.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.limewire.eclipse.plugin.LimeWirePlugin;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = LimeWirePlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(LimeWirePlugin.SERVER_PATH_PREFERENCE_KEY,
				"https://www.limewire.org/fisheye");
		store.setDefault(LimeWirePlugin.REPOSITORY_NAME_PREFERENCE_KEY,
		"limecvs");
	}

}
