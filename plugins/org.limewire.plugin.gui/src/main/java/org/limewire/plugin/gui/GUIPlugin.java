package org.limewire.plugin.gui;

import org.java.plugin.Plugin;

/**
 * Provides services for Plugins that depend on the LimeWire GUI module
 */
public class GUIPlugin extends Plugin {

    /**
     * The GUI Plugin's ID which is defined in plugin.xml.
     * For convenience reasons it's the Package name but it
     * doesn't have to be!  
     */
    public static final String PLUGIN_ID = GUIPlugin.class.getPackage().getName();
    
    /**
     * The GUIPluginContext instance
     */
    private final GUIPluginContext pluginContext = new GUIPluginContext();
    
    @Override
    protected void doStart() throws Exception {
        // DO NOTHING
    }

    @Override
    protected void doStop() throws Exception {
        // DO NOTHING
    }

    /**
     * Returns the GUIPluginContext instance
     */
    public GUIPluginContext getGUIPluginContext() {
        return pluginContext;
    }
}
