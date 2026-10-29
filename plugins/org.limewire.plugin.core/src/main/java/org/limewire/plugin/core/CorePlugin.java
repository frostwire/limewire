package org.limewire.plugin.core;

import org.java.plugin.Plugin;

/**
 * Provides services Plugins that depend on the LimeWire CORE module
 */
public class CorePlugin extends Plugin {
    
    /**
     * The CORE Plugin's ID which is defined in plugin.xml.
     * For convenience reasons it's the Package name but it
     * doesn't have to be!  
     */
    public static final String PLUGIN_ID = CorePlugin.class.getPackage().getName();
    
    /**
     * The CorePluginContext instance
     */
    private final CorePluginContext pluginContext = new CorePluginContext();

    @Override
    protected void doStart() throws Exception {
        // DO NOTHING
    }

    @Override
    protected void doStop() throws Exception {
        // DO NOTHING
    }
    
    /**
     * Returns the CorePluginContext instance
     */
    public CorePluginContext getCorePluginContext() {
        return pluginContext;
    }
}
