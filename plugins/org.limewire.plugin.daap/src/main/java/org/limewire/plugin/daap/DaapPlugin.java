package org.limewire.plugin.daap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.java.plugin.Plugin;
import org.limewire.plugin.core.CorePlugin;
import org.limewire.plugin.core.CorePluginContext;
import org.limewire.plugin.daap.options.panes.DaapBufferSizePaneItem;
import org.limewire.plugin.daap.options.panes.DaapPasswordPaneItem;
import org.limewire.plugin.daap.options.panes.DaapSupportPaneItem;
import org.limewire.plugin.daap.settings.DaapSettings;
import org.limewire.plugin.gui.GUIPlugin;
import org.limewire.plugin.gui.GUIPluginContext;

import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.options.OptionsPane;
import com.limegroup.gnutella.gui.options.OptionsPaneImpl;

public class DaapPlugin extends Plugin {
    
    private static final String ITUNES_DAAP_KEY    = "OPTIONS_ITUNES_DAAP_MAIN_TITLE";
    
    private final DaapManager manager = new DaapManager();
    
    private boolean finalizerRegistered = false;
    
    private OptionsPane optionsPane = null;
    
    public DaapManager getDaapManager() {
        return manager;
    }
    
    @Override
    protected synchronized void doStart() throws Exception {
        
        CorePlugin corePlugin = (CorePlugin)getManager().getPlugin(CorePlugin.PLUGIN_ID);
        CorePluginContext coreContext = (CorePluginContext)corePlugin.getCorePluginContext();
        
        FileManager fileManager = coreContext.getFileManager();
        fileManager.addFileEventListener(manager);
        
        GUIPlugin guiPlugin = (GUIPlugin)getManager().getPlugin(GUIPlugin.PLUGIN_ID);
        GUIPluginContext guiContext = guiPlugin.getGUIPluginContext();
        
        addOptionsPane(guiContext);
        
        if (!finalizerRegistered) {
            GUIMediator.addFinalizeListener(manager);
            finalizerRegistered = true;
        }
        
        if (DaapSettings.DAAP_ENABLED.getValue()) {
            try {
                manager.start();
                manager.init();
            } catch (IOException err) {
                GUIMediator.showError("ERROR_DAAP_START_FAILED");
                DaapSettings.DAAP_ENABLED.setValue(false);
            }
        }
    }
    
    @Override
    protected synchronized void doStop() throws Exception {
        CorePlugin corePlugin = (CorePlugin)getManager().getPlugin(CorePlugin.PLUGIN_ID);
        CorePluginContext context = (CorePluginContext)corePlugin.getCorePluginContext();
        
        FileManager fileManager = context.getFileManager();
        fileManager.removeFileEventListener(manager);
        
        GUIPlugin guiPlugin = (GUIPlugin)getManager().getPlugin(GUIPlugin.PLUGIN_ID);
        GUIPluginContext guiContext = guiPlugin.getGUIPluginContext();
        
        removeOptionsPane(guiContext);
        
        manager.stop();
    }
    
    private synchronized void addOptionsPane(GUIPluginContext context) {
        if (optionsPane != null) {
            return;
        }
        
        optionsPane = new OptionsPaneImpl(ITUNES_DAAP_KEY);
        optionsPane.add(new DaapSupportPaneItem(manager, "ITUNES_DAAP_PREFERENCE"));
        optionsPane.add(new DaapPasswordPaneItem(manager, "ITUNES_DAAP_PASSWORD"));
        optionsPane.add(new DaapBufferSizePaneItem("ITUNES_DAAP_BUFFER_SIZE"));
        
        String[] keys = { 
            "ITUNES_DAAP_PREFERENCE", 
            "ITUNES_DAAP_PASSWORD", 
            "ITUNES_DAAP_BUFFER_SIZE"
        };
        
        List<String> keywords = new ArrayList<String>();
        
        String name = GUIMediator.getStringResource(ITUNES_DAAP_KEY);
        
        keywords.add(name);
        for (String key : keys) {
            keywords.add(GUIMediator.getStringResource("OPTIONS_" + key + "_TITLE"));
            keywords.add(GUIMediator.getStringResource("OPTIONS_" + key + "_LABEL"));
        }
        
        context.addOptionsPane(this, optionsPane, name, (String[])keywords.toArray(new String[0]));
    }
    
    private synchronized void removeOptionsPane(GUIPluginContext context) {
        if (optionsPane == null) {
            return;
        }
        
        context.removeOptionsPane(this, optionsPane);
        optionsPane = null;
    }
}
