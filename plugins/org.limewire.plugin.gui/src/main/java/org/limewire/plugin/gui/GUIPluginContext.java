package org.limewire.plugin.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import org.java.plugin.Plugin;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.menu.PluginMenu;
import com.limegroup.gnutella.gui.options.OptionsMediator;
import com.limegroup.gnutella.gui.options.OptionsPane;

/**
 * Provides accessors methods to LimeWire's GUI. 
 */
public class GUIPluginContext {
    
    /**
     * Returns LimeWire's GUIMediator
     */
    public GUIMediator getGUIMediator() {
        return GUIMediator.instance();
    }
    
    /**
     * Returns LimeWire's Plugin Menu
     */
    private JMenu getPluginMenu() {
        return PluginMenu.instance().getMenu();
    }
    
    /**
     * Returns LimeWire's JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        return getGUIMediator().getJTabbedPane();
    }
    
    /**
     * Returns LimeWire's OptionsMediator
     */
    private OptionsMediator getOptionsMediator() {
        return OptionsMediator.instance();
    }
    
    /**
     * Adds the JMenuItem to LimeWire's Plugins Menu
     */
    public void addMenuItem(Plugin plugin, JMenuItem menuItem) {
        getPluginMenu().add(menuItem);
    }
    
    /**
     * Removes the JMenuItem from LimeWire's Plugins Menu
     */
    public void removeMenuItem(Plugin plugin, JMenuItem menuItem) {
        getPluginMenu().remove(menuItem);
    }
    
    /**
     * Adds the Component to LimeWire's TabbedPane
     */
    public void addTab(Plugin plugin, String title, Component tab) {
        getJTabbedPane().addTab(title, tab);
    }
    
    /**
     * Adds the Component to LimeWire's TabbedPane
     */
    public void addTab(Plugin plugin, String title, Icon icon, Component tab) {
        getJTabbedPane().addTab(title, icon, tab);
    }
    
    /**
     * Adds the Component to LimeWire's TabbedPane
     */
    public void addTab(Plugin plugin, String title, Icon icon, Component tab, String tip) {
        getJTabbedPane().addTab(title, icon, tab, tip);
    }
    
    /**
     * Removes the Component from LimeWire's TabbedPane
     */
    public void removeTab(Plugin plugin, Component tab) {
        getJTabbedPane().remove(tab);
    }
    
    /**
     * Adds an OptionsPane to LimeWire's Options/Settings
     * 
     * @param displayName The localized name of the OptionsPane
     */
    public void addOptionsPane(Plugin plugin, OptionsPane pane, String displayName) {
        addOptionsPane(plugin, pane, displayName, new String[0]);
    }
    
    /**
     * Adds an OptionsPane to LimeWire's Options/Settings
     * 
     * @param displayName The localized name of the OptionsPane
     * @param keywords An Array of localized keywords under which the
     *          given OptionsPane should be registered
     */
    public void addOptionsPane(Plugin plugin, OptionsPane pane, String displayName, String... keywords) {
        getOptionsMediator().addPluginOptionsPane(pane.getName(), displayName, pane, keywords);
    }
    
    /**
     * Removes the OptionsPane from LimeWire's Options/Settings
     */
    public void removeOptionsPane(Plugin plugin, OptionsPane pane) {
        getOptionsMediator().removePluginOptionsPane(pane.getName(), pane, new String[0]);
    }
    
    /**
     * Removes the OptionsPane from LimeWire's Options/Settings
     * 
     * @param keywords An Array of localized keywords under which the
     *          given OptionsPane is registered
     */
    public void removeOptionsPane(Plugin plugin, OptionsPane pane, String... keywords) {
        getOptionsMediator().removePluginOptionsPane(pane.getName(), pane, keywords);
    }
}
