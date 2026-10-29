/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/HostConfiguration.java,v 1.3 2004/05/19 15:44:08 sberlin Exp $
 * $Revision: 1.3 $
 * $Date: 2004/05/19 15:44:08 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import org.apache.commons.httpclient.protocol.Protocol;

import java.net.InetAddress;

/**
 * Holds all of the variables needed to describe an HTTP connection to a host.  This includes 
 * remote host, port and protocol, proxy host and port, local address, and virtual host.
 * 
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Laura Werner
 * 
 * @since 2.0 
 */
public class HostConfiguration implements Cloneable {

    private static boolean JAVA_118 =
        System.getProperties().getProperty("java.version").startsWith("1.1.8");

    /** The host to use. */
    private String host;

    /** The virtual host to use. */
    private String virtualHost;

    /** The port to use. */
    private int port;

    /** The protocol */
    private Protocol protocol;

    /** True if a host has been set */
    private boolean hostSet;

    /** The host name of the proxy server */
    private String proxyHost;

    /** The port number of the proxy server */
    private int proxyPort;

    /** True if a proxy server has been set */
    private boolean proxySet;
    
    /** The local address to use when creating the socket, or null to use the default */
    private InetAddress localAddress;

    /**
     * The version of SOCKS Proxy we want to support. -1 indicates no support,
     * the only valid values are 4 and 5
     */
    private int socksVersion;
    
    private String userName;
    
    private String userPass;

    /**
     * Constructor for HostConfiguration.
     */
    public HostConfiguration() {
        
        this.host = null;
        this.virtualHost = null;
        this.port = -1;
        this.protocol = null;
        this.hostSet = false;
        
        this.proxyHost = null;
        this.proxyPort = -1;
        this.proxySet = false;
        this.localAddress = null;
        this.socksVersion = -1;
        this.userName = null;
        this.userPass = null;
    }
    
    /**
     * Copy constructor for HostConfiguration
     * 
     * @param hostConfiguration the hostConfiguration to copy
     */
    public HostConfiguration (HostConfiguration hostConfiguration) {
        // wrap all of the assignments in a synchronized block to avoid
        // having to negotiate the monitor for each method call
        if(JAVA_118) {
            this.host = hostConfiguration.getHost();
            this.virtualHost = hostConfiguration.getVirtualHost();
            this.port = hostConfiguration.getPort();
            this.protocol = hostConfiguration.getProtocol();
            this.hostSet = hostConfiguration.isHostSet();
            this.proxyHost = hostConfiguration.getProxyHost();
            this.proxyPort = hostConfiguration.getProxyPort();
            this.proxySet = hostConfiguration.isProxySet();
            this.localAddress = hostConfiguration.getLocalAddress();
            this.socksVersion = hostConfiguration.getSOCKSVersion();
            this.userName = hostConfiguration.getSOCKSUsername();
            this.userPass = hostConfiguration.getSOCKSPassword();
        } else {
        synchronized (hostConfiguration) {            
            this.host = hostConfiguration.getHost();
            this.virtualHost = hostConfiguration.getVirtualHost();
            this.port = hostConfiguration.getPort();
            this.protocol = hostConfiguration.getProtocol();
            this.hostSet = hostConfiguration.isHostSet();
            this.proxyHost = hostConfiguration.getProxyHost();
            this.proxyPort = hostConfiguration.getProxyPort();
            this.proxySet = hostConfiguration.isProxySet();
            this.localAddress = hostConfiguration.getLocalAddress();
            this.socksVersion = hostConfiguration.getSOCKSVersion();
            this.userName = hostConfiguration.getSOCKSUsername();
            this.userPass = hostConfiguration.getSOCKSPassword();
        }
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new HostConfiguration(this);
    }    
    
    /**
     * @see java.lang.Object#toString()
     */
    public synchronized String toString() {
        
        boolean appendComma = false;
        
        StringBuffer b = new StringBuffer(50);        
        b.append("HostConfiguration[");
        
        if (isHostSet()) {
            appendComma = true;
            b.append("host=").append(host);
            b.append(", protocol=").append(protocol);
            b.append(", port=").append(port);
            if (virtualHost != null) {
                b.append(", virtualHost=").append(virtualHost);
            }
        }
        if (isProxySet()) {
            if (appendComma) {
                b.append(", ");
            } else {
                appendComma = true;
            }
            b.append("proxyHost=").append(proxyHost);
            b.append(", proxyPort=").append(proxyPort);
        }
        if (localAddress != null) {
            if (appendComma) {
                b.append(", ");
            } else {
                appendComma = true;
            }
            b.append("localAddress=").append(localAddress);
        }
        
        b.append("]");
        return b.toString();
    }    
    
    /**
     * Tests if the host configuration equals the configuration set on the
     * connection. True only if the host, port, protocol, local address and virtual address
     * are equal.  If no host configuration has been set false will be returned.
     * 
     * @param connection the connection to test against
     * @return <code>true</code> if the connection's host information equals that of this
     * configuration
     * 
     * @see #proxyEquals(HttpConnection)
     * @see #isHostSet()
     */
    public synchronized boolean hostEquals(HttpConnection connection) {
        
        if (hostSet) {
            if (!this.host.equalsIgnoreCase(connection.getHost())) {
                return false;
            }
            if (this.virtualHost != null) {
                if (!this.virtualHost.equalsIgnoreCase(connection.getVirtualHost())) {
                    return false;
                }
            } else {
                if (connection.getVirtualHost() != null) {
                    return false; 
                }
            }
            if (this.port != connection.getPort()) {
                return false;
            }
            if (!this.protocol.equals(connection.getProtocol())) {
                return false;
            }
            if (this.localAddress != null) {
                if (!this.localAddress.equals(connection.getLocalAddress())) {
                    return false;
                }
            } else {
                if (connection.getLocalAddress() != null) {
                    return false; 
                }
            }
            return true;
        } else {
            return false;   
        }
        
    }

    /**
     * Tests if the proxy configuration equals the configuration set on the
     * connection. True only if the proxyHost and proxyPort are equal.
     *
     * @param connection the connection to test against
     * @return <code>true</code> if the connection's proxy information equals that of this
     * configuration
     *
     * @see #hostEquals(HttpConnection)
     */
    public synchronized boolean proxyEquals(HttpConnection connection) {
        
        if (proxyHost == null) {
            return connection.getProxyHost() == null;   
        } else {
            return (
                proxyHost.equalsIgnoreCase(connection.getProxyHost())
                && proxyPort == connection.getProxyPort()
            );
        }
    }    
    
    /**
     * Returns true if the host is set.
     * @return <code>true</code> if the host is set.
     */
    public synchronized boolean isHostSet() {
        return hostSet;
    }

    /**
     * Sets the given host, port and protocol
     * 
     * @param host the host(IP or DNS name)
     * @param port The port
     * @param protocol The protocol.
     */
    public synchronized void setHost(String host, int port, String protocol) {
        setHost(host, null, port, Protocol.getProtocol(protocol));
    }
    
    /**
     * Sets the given host, virtual host, port and protocol.
     * 
     * @param host the host(IP or DNS name)
     * @param virtualHost the virtual host name or <code>null</code>
     * @param port the host port or -1 to use protocol default
     * @param protocol the protocol
     */
    public synchronized void setHost(String host, String virtualHost, int port, 
                                     Protocol protocol) {
            
        if (host == null) {
            throw new IllegalArgumentException("host must not be null");   
        }
        if (protocol == null) {
            throw new IllegalArgumentException("protocol must not be null");   
        }
        
        this.host = host;
        this.virtualHost = virtualHost;
        this.port = port == -1 ? protocol.getDefaultPort() : port;
        this.protocol = protocol;
        
        this.hostSet = true;
    }

    /**
     * Sets the given host, port and protocol.
     *   
     * @param host the host(IP or DNS name)
     * @param port The port
     * @param protocol the protocol
     */
    public synchronized void setHost(String host, int port, Protocol protocol) {
        setHost(host, null, port, protocol);
    }

    /**
     * Sets the given host and port.  Uses the default protocol "http".
     * 
     * @param host the host(IP or DNS name)
     * @param port The port
     */
    public synchronized void setHost(String host, int port) {
        setHost(host, null, port, Protocol.getProtocol("http"));
    }
    
    /**
     * Set the given host. Uses the default protocol("http") and its port.
     * 
     * @param host The host(IP or DNS name).
     */
    public synchronized void setHost(String host) {
        Protocol defaultProtocol = Protocol.getProtocol("http"); 
        setHost(host, null, defaultProtocol.getDefaultPort(), defaultProtocol);
    }
    
    /**
     * Sets the protocol, host and port from the given URI.
     * @param uri the URI.
     */
    public synchronized void setHost(URI uri) {
        try {
            setHost(uri.getHost(), uri.getPort(), uri.getScheme());
        } catch (URIException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * Return the host url.
     * 
     * @return The host url.
     */
    public synchronized String getHostURL() {
        
        if (!hostSet) {
            throw new IllegalStateException("a default host must be set to "
                + "create a host URL" 
            );   
        }
        
        String url = protocol.getScheme() + "://" + host;
        
        if (port != -1 && port != protocol.getDefaultPort()) {
            url += ":" + port;
        }
        
        return url;
    }

    /**
     * Returns the host.
     * 
     * @return the host(IP or DNS name), or <code>null</code> if not set
     * 
     * @see #isHostSet()
     */
    public synchronized String getHost() {
        return host;
    }

    /**
     * Returns the virtual host.
     * 
     * @return the virtual host name, or <code>null</code> if not set
     */
    public synchronized String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Returns the port.
     * 
     * @return the host port, or <code>-1</code> if not set
     * 
     * @see #isHostSet()
     */
    public synchronized int getPort() {
        return port;
    }

    /**
     * Returns the protocol.
     * @return The protocol.
     */
    public synchronized Protocol getProtocol() {
        return protocol;
    }

    /**
     * Tests if the proxy host/port have been set.
     * 
     * @return <code>true</code> if a proxy server has been set.
     * 
     * @see #setProxy(String, int)
     */    
    public synchronized boolean isProxySet() {
        return proxySet;   
    }

    /**
     * Set the proxy settings.
     * @param proxyHost The proxy host
     * @param proxyPort The proxy port
     */
    public synchronized void setProxy(String proxyHost, int proxyPort) {
        
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        
        this.proxySet = true;
    }

    /**
     * To ensure proper working of the proxy code, this method must not be
     * called unless setProxy has been called 
     * <p>
     * @param user null if the proxy server does not need authentication
     * @param pass null if the proxy server does not need authentication
     */
    public synchronized void setSOCKSProxySettings(int version, 
                   String proxyHost, int proxyPort, String user, String pass) {
        if(version  < 4 || version > 5)
            throw new IllegalArgumentException("unsupported version of SOCKS");
        if(proxyHost == null || "".equals(proxyHost))
            throw new IllegalArgumentException("Proxy host is null");
		if((proxyPort & 0xFFFF0000) != 0 || (port ==0) )
            throw new IllegalArgumentException("Invalid Proxy Port");
 
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;        
        this.socksVersion = version;
        this.userName = user;
        this.userPass = pass;
    }

    /**
     * Returns the proxyHost.
     * 
     * @return the proxy host, or <code>null</code> if not set
     * 
     * @see #isProxySet()
     */
    public synchronized String getProxyHost() {
        return proxyHost;
    }

    /**
     * Returns the proxyPort.
     * 
     * @return the proxy port, or <code>-1</code> if not set
     * 
     * @see #isProxySet()
     */
    public synchronized int getProxyPort() {
        return proxyPort;
    }

    public synchronized int getSOCKSVersion() {
        return socksVersion;
    }
    
    public synchronized String getSOCKSUsername() {
        return userName;
    }
    
    public synchronized String getSOCKSPassword() {
        return userPass;
    }
    
    public synchronized boolean isSOCKSSet() {
        return (socksVersion==4 || socksVersion == 5);
    }
    

    /**
     * Set the local address to be used when creating connections.
     * If this is unset, the default address will be used.
     * This is useful for specifying the interface to use on multi-homed or clustered systems.
     * 
     * @param localAddress the local address to use
     */
    public synchronized void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Return the local address to be used when creating connections.
     * If this is unset, the default address should be used.
     * 
     * @return the local address to be used when creating Sockets, or <code>null</code>
     */
    public synchronized InetAddress getLocalAddress() {
        return this.localAddress;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public synchronized boolean equals(Object o) {
        
        if (o instanceof HostConfiguration) {
            
            // shortcut if we're comparing with ourselves
            if (o == this) { 
                return true;
            }
            
            HostConfiguration config = (HostConfiguration) o;
            
            if (hostSet) {
                if (!host.equalsIgnoreCase(config.getHost())) {
                    return false;
                }
                if (virtualHost != null) {
                    if (!virtualHost.equalsIgnoreCase(config.getVirtualHost())) {
                        return false;
                    }
                } else {
                    if (config.getVirtualHost() != null) {
                        return false;
                    }
                }
                if (port != config.getPort()) {
                    return false;
                }
                if (!protocol.equals(config.getProtocol())) {
                    return false;
                }
            } else if (config.isHostSet()) {
                return false;
            }
            if (proxyHost != null) {
                if (!proxyHost.equalsIgnoreCase (config.getProxyHost())
                    || proxyPort != config.getProxyPort()) {
                    // either proxyHost or proxyPort don't match
                    return false;
                }
            } else if (config.getProxyHost() != null) {
                return false;
            }            
            if (localAddress != null) {
                if (!localAddress.equals(config.getLocalAddress())) {
                    return false;
                }
            } else {
                if (config.getLocalAddress() != null) {
                    return false; 
                }
            }

            // everything matches
            return true;

        } else {
            return false;
        }
        
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        
        if (host != null) {
            return host.hashCode();
        } else if (proxyHost != null) {
            return proxyHost.hashCode();   
        } else {
            return super.hashCode();
        }
    }
}
