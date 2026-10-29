package org.limewire.plugin.daap.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.limewire.plugin.daap.DaapManager;
import org.limewire.plugin.daap.settings.DaapSettings;

import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.SizedTextField;
import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;
import com.limegroup.gnutella.gui.options.panes.AbstractPaneItem;

public final class DaapSupportPaneItem extends AbstractPaneItem {

    
    private final String DAAP_ENABLED_LABEL = 
            "OPTIONS_ITUNES_DAAP_SUPPORT_DAAP_ENABLED_LABEL";
    
    private final String SERVICE_NAME_LABEL = 
            "OPTIONS_ITUNES_DAAP_SUPPORT_SERVICE_NAME_LABEL";

    private final JCheckBox DAAP_ENABLED = new JCheckBox();

    private final JTextField SERVICE_NAME = new SizedTextField(25, SizePolicy.RESTRICT_HEIGHT);

    private final DaapManager manager;
    
    /**
     * The constructor constructs all of the elements of this 
     * <tt>AbstractPaneItem</tt>.
     *
     * @param key the key for this <tt>AbstractPaneItem</tt> that the
     *            superclass uses to generate locale-specific keys
     */
    public DaapSupportPaneItem(DaapManager manager, final String key) {
        super(key);
        this.manager = manager;
        
        LabeledComponent comp = new LabeledComponent(DAAP_ENABLED_LABEL, DAAP_ENABLED,
        		LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
        add(comp.getComponent());

        add(getVerticalSeparator());
        
        comp = new LabeledComponent(SERVICE_NAME_LABEL, SERVICE_NAME);
        
        add(comp.getComponent());
    }
    
    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Sets the options for the fields in this <tt>PaneItem</tt> when the 
     * window is shown.
     */
    public void initOptions() {
        //DAAP_ENABLED.setSelected(DaapSettings.DAAP_ENABLED.getValue() && 
        //            DaapManager.instance().isServerRunning());
        
        SERVICE_NAME.setText(DaapSettings.DAAP_SERVICE_NAME.getValue());
    }
    
    /**
     * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
     *
     * Applies the options currently set in this window, displaying an
     * error message to the user if a setting could not be applied.
     *
     * @throws IOException if the options could not be applied for some reason
     */
    public boolean applyOptions() throws IOException {

        final boolean prevEnabled = DaapSettings.DAAP_ENABLED.getValue();
        final String prevServiceName = DaapSettings.DAAP_SERVICE_NAME.getValue();

        String serviceName = SERVICE_NAME.getText().trim();

        if (serviceName.length()==0 && DAAP_ENABLED.isSelected()) { 
            throw new IOException(); 
        }

        DaapSettings.DAAP_ENABLED.setValue(DAAP_ENABLED.isSelected());
        DaapSettings.DAAP_SERVICE_NAME.setValue(serviceName);
        DaapSettings.DAAP_LIBRARY_NAME.setValue(serviceName);

        try {
            
            if (DAAP_ENABLED.isSelected()) {
                
                if (!prevEnabled) {
                    manager.restart();
                   
                } else if (!serviceName.equals(prevServiceName)) {
                    manager.updateService();
                }
                    
            } else if (prevEnabled) {
                
                manager.stop();
            }

        } catch (IOException err) {

            DaapSettings.DAAP_ENABLED.setValue(prevEnabled);
            DaapSettings.DAAP_SERVICE_NAME.setValue(prevServiceName);
            DaapSettings.DAAP_LIBRARY_NAME.setValue(prevServiceName);

            manager.stop();

            initOptions();

            throw err;
        }

        return false;
    }
    
    public boolean isDirty() {
        return DaapSettings.DAAP_ENABLED.getValue() != DAAP_ENABLED.isSelected() ||
               !DaapSettings.DAAP_SERVICE_NAME.getValue().equals(SERVICE_NAME.getText()) ||
               !DaapSettings.DAAP_LIBRARY_NAME.getValue().equals(SERVICE_NAME.getText());
    }
}
