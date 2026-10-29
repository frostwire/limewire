package org.limewire.core.api.updates;

/**
 * contains utility methods for auto update process
 */
public interface AutoUpdateHelper {
    
    /**
     * separator char used while storing autoupdate params 
     */
    public String SEPARATOR = "#";
    
    /**
     * returns true if updates are available for download
     */
    public boolean isUpdateAvailable();
       
    /**
     *  returns true if updates are ready for install
     */
    public boolean isUpdateReadyForInstall();
    
    /**
     * returns executable script file containing command(s) to update limewire.
     */
    public String getAutoUpdateCommand();

}
