/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestLocalHostBase.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import junit.framework.TestCase;

/**
 * The base class for all tests that need to connection to the localhost
 * web server.
 * 
 * @author Michael Becke
 */
public abstract class TestLocalHostBase extends TestCase {

    private final String protocol = System.getProperty(
        "httpclient.test.localHost.protocol",
        "http"
    );
    private final String host = System.getProperty("httpclient.test.localHost","localhost");
    private final int port;
    private final String proxyHost = System.getProperty("httpclient.test.proxy.host");
    private final int proxyPort;
    
    /**
     * Constructor for TestLocalHostBase.
     * @param testName
     */
    public TestLocalHostBase(String testName) {
        super(testName);
        String portString = System.getProperty("httpclient.test.localPort","8080");
        int tempPort = 8080;
        try {
            tempPort = Integer.parseInt(portString);
        } catch(Exception e) {
            tempPort = 8080;
        }
        port = tempPort;
        String proxyPortString = System.getProperty("httpclient.test.proxy.port","3128");
        int tempProxyPort = 3128;
        try {
            tempProxyPort = Integer.parseInt(proxyPortString);
        } catch(Exception e) {
            tempProxyPort = 3128;
        }
        proxyPort = tempProxyPort;
    }

    /**
     * Gets a new HttpClient instance.  This instance has been configured
     * with all appropriate host/proxy values.
     * 
     * @return a new HttpClient instance
     */
    public HttpClient createHttpClient() {
        return createHttpClient(null);
    }

    /**
     * Gets a new HttpClient instance that uses the given connection manager. 
     * This instance has been configured with all appropriate host/proxy values.
     * 
     * @param connectionManager the connection manager to use or <code>null</code>
     * 
     * @return a new HttpClient instance
     */
    public HttpClient createHttpClient(HttpConnectionManager connectionManager) {
        
        HttpClient client = null;

        if (connectionManager == null) {
            client = new HttpClient();
        } else {
            client = new HttpClient(connectionManager);
        }
        
        client.getHostConfiguration().setHost(host, port, protocol);
        if (proxyHost != null) {
            client.getHostConfiguration().setProxy(proxyHost, proxyPort);
        }

        return client;
    }

    /**
     * @return String
     */
    public String getHost() {
        return host;
    }

    /**
     * @return int
     */
    public int getPort() {
        return port;
    }

    /**
     * @return String
     */
    public String getProtocol() {
        return protocol;
    }

}
