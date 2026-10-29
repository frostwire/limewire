package org.limewire.plugin.core;

import com.limegroup.bittorrent.TorrentManager;
import com.limegroup.gnutella.Acceptor;
import com.limegroup.gnutella.ConnectionManager;
import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.UploadManager;
import com.limegroup.gnutella.dht.DHTManager;

/**
 * Provides accessors methods to LimeWire's CORE. 
 */
public class CorePluginContext {
    
    /**
     * Returns LimeWire's Acceptor
     */
    public Acceptor getAcceptor() {
        return RouterService.getAcceptor();
    }
    
    /**
     * Returns LimeWire's ConnectionManager
     */
    public ConnectionManager getConnectionManager() {
        return RouterService.getConnectionManager();
    }
    
    /**
     * Returns LimeWire's DownloadManager
     */
    public DownloadManager getDownloadManager() {
        return RouterService.getDownloadManager();
    }
    
    /**
     * Returns LimeWire's UploadManager
     */
    public UploadManager getUploadManager() {
        return RouterService.getUploadManager();
    }
    
    /**
     * Returns LimeWire's DHTManager
     */
    public DHTManager getDHTManager() {
        return RouterService.getDHTManager();
    }
    
    /**
     * Returns LimeWire's TorrentManager
     */
    public TorrentManager getTorrentManager() {
        return RouterService.getTorrentManager();
    }
    
    /**
     * Returns LimeWire's FileManager
     */
    public FileManager getFileManager() {
        return RouterService.getFileManager();
    }
}
