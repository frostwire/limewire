package org.limewire.plugin.daap;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.limewire.plugin.daap.settings.DaapSettings;


import de.kapsi.net.daap.DaapUtil;

/**
 * Helps us to publicize and update the DAAP Service via mDNS
 */
class DaapBonjourService {

    private static final String VERSION = "Version";

    private static final String MACHINE_NAME = "Machine Name";

    private static final String PASSWORD = "Password";

    private final JmDNS zeroConf;

    private ServiceInfo serviceInfo;

    public DaapBonjourService(InetAddress addr) throws IOException {
        zeroConf = new JmDNS(addr);
    }

    public boolean isRegistered() {
        return (serviceInfo != null);
    }

    private ServiceInfo createServiceInfo() {

        String type = DaapSettings.DAAP_TYPE_NAME.getValue();
        String name = DaapSettings.DAAP_SERVICE_NAME.getValue();

        int port = DaapSettings.DAAP_PORT.getValue();
        int weight = DaapSettings.DAAP_WEIGHT.getValue();
        int priority = DaapSettings.DAAP_PRIORITY.getValue();

        boolean password = DaapSettings.DAAP_REQUIRES_PASSWORD.getValue();

        Hashtable<String, String> props = new Hashtable<String, String>();

        // Greys the share and the playlist names when iTunes's
        // protocol version is different from this version. It's
        // only a nice visual effect and has no impact to the
        // ability to connect this server! Disabled because
        // iTunes 4.2 is still widespread...
        props.put(VERSION, Integer.toString(DaapUtil.DAAP_VERSION_3));

        // This is the inital share name
        props.put(MACHINE_NAME, name);

        // shows the small lock if Service is protected
        // by a password!
        props.put(PASSWORD, Boolean.toString(password));

        String qualifiedName = null;

        // This isn't really required but as iTunes
        // does it in this way I'm doing it too...
        if (password) {
            qualifiedName = name + "_PW." + type;
        } else {
            qualifiedName = name + "." + type;
        }

        ServiceInfo serviceInfo = new ServiceInfo(type, qualifiedName, port,
                weight, priority, props);

        return serviceInfo;
    }

    public void registerService() throws IOException {
        
        if (isRegistered())
            throw new IOException();

        ServiceInfo serviceInfo = createServiceInfo();
        zeroConf.registerService(serviceInfo);
        this.serviceInfo = serviceInfo;
    }

    public void unregisterService() {
        if (!isRegistered())
            return;

        zeroConf.unregisterService(serviceInfo);
        serviceInfo = null;
    }

    public void updateService() throws IOException {
        if (!isRegistered())
            throw new IOException();

        if (serviceInfo.getPort() != DaapSettings.DAAP_PORT.getValue())
            unregisterService();

        ServiceInfo serviceInfo = createServiceInfo();
        zeroConf.registerService(serviceInfo);

        this.serviceInfo = serviceInfo;
    }

    public void close() {
        unregisterService();
        zeroConf.close();
    }
}