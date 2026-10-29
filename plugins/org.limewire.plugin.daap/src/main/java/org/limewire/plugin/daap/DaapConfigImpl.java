package org.limewire.plugin.daap;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.limewire.plugin.daap.settings.DaapSettings;

import com.limegroup.gnutella.util.LimeWireUtils;

import de.kapsi.net.daap.DaapConfig;

/**
 * A LimeWire specific implementation of DaapConfig
 */
class DaapConfigImpl extends DaapConfig {

    private InetAddress addr;

    public DaapConfigImpl(InetAddress addr) {
        this.addr = addr;

        // Reset PORT to default value to prevent increasing
        // it to infinity
        DaapSettings.DAAP_PORT.revertToDefault();
    }

    public String getServerName() {
        return LimeWireUtils.getHttpServer();
    }

    public void nextPort() {
        int port = DaapSettings.DAAP_PORT.getValue();
        DaapSettings.DAAP_PORT.setValue(port + 1);
    }

    public int getBacklog() {
        return 0;
    }

    public InetSocketAddress getInetSocketAddress() {
        int port = DaapSettings.DAAP_PORT.getValue();
        return new InetSocketAddress(addr, port);
    }

    public int getMaxConnections() {
        return DaapSettings.DAAP_MAX_CONNECTIONS.getValue();
    }
}