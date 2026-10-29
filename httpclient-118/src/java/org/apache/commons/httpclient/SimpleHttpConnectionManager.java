/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/SimpleHttpConnectionManager.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:59 $
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

import java.io.IOException;
import java.io.InputStream;

/**
 * A connection manager that provides access to a single HttpConnection.  This
 * manager makes no attempt to provide exclusive access to the contained
 * HttpConnection.
 *
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author Eric Johnson
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Laura Werner
 * 
 * @since 2.0
 */
public class SimpleHttpConnectionManager implements HttpConnectionManager {

    /** The http connection */
    private HttpConnection httpConnection;

    /** The value to set when calling setStaleCheckingEnabled() on connections */
    private boolean connectionStaleCheckingEnabled = true;

    /**
     * Constructor for SimpleHttpConnectionManager.
     */
    public SimpleHttpConnectionManager() {
        super();
    }

    /**
     * @see HttpConnectionManager#getConnection(HostConfiguration)
     */
    public HttpConnection getConnection(HostConfiguration hostConfiguration) {
        return getConnection(hostConfiguration, 0);
    }

    /**
     * Gets the staleCheckingEnabled value to be set on HttpConnections that are created.
     * 
     * @return <code>true</code> if stale checking will be enabled on HttpConections
     * 
     * @see HttpConnection#isStaleCheckingEnabled()
     */
    public boolean isConnectionStaleCheckingEnabled() {
        return connectionStaleCheckingEnabled;
    }

    /**
     * Sets the staleCheckingEnabled value to be set on HttpConnections that are created.
     * 
     * @param connectionStaleCheckingEnabled <code>true</code> if stale checking will be enabled 
     * on HttpConections
     * 
     * @see HttpConnection#setStaleCheckingEnabled(boolean)
     */
    public void setConnectionStaleCheckingEnabled(boolean connectionStaleCheckingEnabled) {
        this.connectionStaleCheckingEnabled = connectionStaleCheckingEnabled;
    }

    /**
     * @see HttpConnectionManager#getConnection(HostConfiguration, long)
     */
    public HttpConnection getConnection(
        HostConfiguration hostConfiguration, long timeout) {

        if (httpConnection == null) {
            httpConnection = new HttpConnection(hostConfiguration);
            httpConnection.setStaleCheckingEnabled(connectionStaleCheckingEnabled);
        } else {

            // make sure the host and proxy are correct for this connection
            // close it and set the values if they are not
            if (!hostConfiguration.hostEquals(httpConnection)
                || !hostConfiguration.proxyEquals(httpConnection)) {
                    
                if (httpConnection.isOpen()) {
                    httpConnection.close();
                }

                httpConnection.setStaleCheckingEnabled(connectionStaleCheckingEnabled);

                httpConnection.setHost(hostConfiguration.getHost());
                httpConnection.setVirtualHost(hostConfiguration.getVirtualHost());
                httpConnection.setPort(hostConfiguration.getPort());
                httpConnection.setProtocol(hostConfiguration.getProtocol());
                httpConnection.setLocalAddress(hostConfiguration.getLocalAddress());

                httpConnection.setProxyHost(hostConfiguration.getProxyHost());
                httpConnection.setProxyPort(hostConfiguration.getProxyPort());
            } else {
                finishLastResponse(httpConnection);
            }
        }

        return httpConnection;
    }

    /**
     * @see HttpConnectionManager#releaseConnection(org.apache.commons.httpclient.HttpConnection)
     */
    public void releaseConnection(HttpConnection conn) {
        if (conn != httpConnection) {
            throw new IllegalStateException("Unexpected close on a different connection.");
        }

        finishLastResponse(httpConnection);
    }

    /**
     * Since the same connection is about to be reused, make sure the
     * previous request was completely processed, and if not
     * consume it now.
     * @param conn The connection
     */
    static void finishLastResponse(HttpConnection conn) {
        InputStream lastResponse = conn.getLastResponseInputStream();
        if (lastResponse != null) {
            conn.setLastResponseInputStream(null);
            try {
                lastResponse.close();
            } catch (IOException ioe) {
                //FIXME: badness - close to force reconnect.
                conn.close();
            }
        }
    }
}
