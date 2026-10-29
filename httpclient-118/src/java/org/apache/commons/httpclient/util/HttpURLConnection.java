/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/util/HttpURLConnection.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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

package org.apache.commons.httpclient.util;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Header;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.ProtocolException;
import java.security.Permission;

/**
 * Provides a <code>HttpURLConnection</code> wrapper around HttpClient's
 * <code>HttpMethod</code>. This allows existing code to easily switch to
 * HttpClieht without breaking existing interfaces using the JDK
 * <code>HttpURLConnection<code>.
 *
 * Note 1: The current implementations wraps only a connected
 * <code>HttpMethod</code>, ie a method that has alreayd been used to connect
 * to an HTTP server.
 *
 * Note 2: It is a best try effort as different version of the JDK have
 * different behaviours for <code>HttpURLConnection</code> (And I'm not even
 * including the numerous <code>HttpURLConnection</code> bugs!).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 2.0
 *
 * @version $Id: HttpURLConnection.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class HttpURLConnection extends java.net.HttpURLConnection {

    // -------------------------------------------------------- Class Variables
   
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HttpURLConnection.class);


    // ----------------------------------------------------- Instance Variables

    /**
     * The <code>HttpMethod</code> object that was used to connect to the
     * HTTP server. It contains all the returned data.
     */
    private HttpMethod method;

    /**
     * The URL to which we are connected
     */
    private URL url;



    // ----------------------------------------------------------- Constructors

    /**
     * Creates an <code>HttpURLConnection</code> from a <code>HttpMethod</code>.
     *
     * @param method the theMethod that was used to connect to the HTTP
     *        server and which contains the returned data.
     * @param url the URL to which we are connected (includes query string)
     */
    public HttpURLConnection(HttpMethod method, URL url) {
        super(url);
        this.method = method;
        this.url = url;
    }

    /**
     * Create an instance.
     * @param url The URL.
     * @see java.net.HttpURLConnection#HttpURLConnection(URL)
     */
    protected HttpURLConnection(URL url) {
        super(url);
        throw new RuntimeException("An HTTP URL connection can only be "
            + "constructed from a HttpMethod class");
    }


    // --------------------------------------------------------- Public Methods

    /** 
     * Gets an input stream for the HttpMethod response body.
     * @throws IOException If an IO problem occurs.
     * @return The input stream.
     * @see java.net.HttpURLConnection#getInputStream()
     * @see org.apache.commons.httpclient.HttpMethod#getResponseBodyAsStream()
     */
    public InputStream getInputStream() throws IOException {
        LOG.trace("enter HttpURLConnection.getInputStream()");
        return this.method.getResponseBodyAsStream();
    }

    /**
     * Not yet implemented.
     * Return the error stream.
     * @see java.net.HttpURLConnection#getErrorStream()
     */
    public InputStream getErrorStream() {
        LOG.trace("enter HttpURLConnection.getErrorStream()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#disconnect()
     */
    public void disconnect() {
        LOG.trace("enter HttpURLConnection.disconnect()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @throws IOException If an IO problem occurs.
     * @see java.net.HttpURLConnection#connect()
     */
    public void connect() throws IOException {
        LOG.trace("enter HttpURLConnection.connect()");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @return true if we are using a proxy.
     * @see java.net.HttpURLConnection#usingProxy()
     */
    public boolean usingProxy() {
        LOG.trace("enter HttpURLConnection.usingProxy()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Return the request method.
     * @return The request method.
     * @see java.net.HttpURLConnection#getRequestMethod()
     * @see org.apache.commons.httpclient.HttpMethod#getName()
     */
    public String getRequestMethod() {
        LOG.trace("enter HttpURLConnection.getRequestMethod()");
        return this.method.getName();
    }

    /**
     * Return the response code.
     * @return The response code.
     * @throws IOException If an IO problem occurs.
     * @see java.net.HttpURLConnection#getResponseCode()
     * @see org.apache.commons.httpclient.HttpMethod#getStatusCode()
     */
    public int getResponseCode() throws IOException {
        LOG.trace("enter HttpURLConnection.getResponseCode()");
        return this.method.getStatusCode();
    }

    /**
     * Return the response message
     * @return The response message
     * @throws IOException If an IO problem occurs.
     * @see java.net.HttpURLConnection#getResponseMessage()
     * @see org.apache.commons.httpclient.HttpMethod#getStatusText()
     */
    public String getResponseMessage() throws IOException {
        LOG.trace("enter HttpURLConnection.getResponseMessage()");
        return this.method.getStatusText();
    }

    /**
     * Return the header field
     * @param name the name of the header
     * @return the header field.
     * @see java.net.HttpURLConnection#getHeaderField(String)
     * @see org.apache.commons.httpclient.HttpMethod#getResponseHeaders()
     */
    public String getHeaderField(String name) {
        LOG.trace("enter HttpURLConnection.getHeaderField(String)");
        // Note: Return the last matching header in the Header[] array, as in
        // the JDK implementation.
        Header[] headers = this.method.getResponseHeaders();
        for (int i = headers.length - 1; i >= 0; i--) {
            if (headers[i].getName().equalsIgnoreCase(name)) {
                return headers[i].getValue();
            }
        }

        return null;
    }

    /**
     * Return the header field key
     * @param keyPosition The key position
     * @return The header field key.
     * @see java.net.HttpURLConnection#getHeaderFieldKey(int)
     * @see org.apache.commons.httpclient.HttpMethod#getResponseHeaders()
     */
    public String getHeaderFieldKey(int keyPosition) {
        LOG.trace("enter HttpURLConnection.getHeaderFieldKey(int)");

        // Note: HttpClient does not consider the returned Status Line as
        // a response header. However, getHeaderFieldKey(0) is supposed to 
        // return null. Hence the special case below ...
        
        if (keyPosition == 0) {
            return null;
        }

        // Note: HttpClient does not currently keep headers in the same order
        // that they are read from the HTTP server.

        Header[] headers = this.method.getResponseHeaders();
        if (keyPosition < 0 || keyPosition > headers.length) {
            return null;
        }

        return headers[keyPosition - 1].getName();
    }

    /**
     * Return the header field at the specified position
     * @param position The position
     * @return The header field.
     * @see java.net.HttpURLConnection#getHeaderField(int)
     * @see org.apache.commons.httpclient.HttpMethod#getResponseHeaders()
     */
    public String getHeaderField(int position) {
        LOG.trace("enter HttpURLConnection.getHeaderField(int)");

        // Note: HttpClient does not consider the returned Status Line as
        // a response header. However, getHeaderField(0) is supposed to 
        // return the status line. Hence the special case below ...
        
        if (position == 0) {
            return this.method.getStatusLine().toString();
        }

        // Note: HttpClient does not currently keep headers in the same order
        // that they are read from the HTTP server.

        Header[] headers = this.method.getResponseHeaders();
        if (position < 0 || position > headers.length) {
            return null;
        }

        return headers[position - 1].getValue();
    }

    /**
     * Return the URL
     * @return The URL.
     * @see java.net.HttpURLConnection#getURL()
     */
    public URL getURL() {
        LOG.trace("enter HttpURLConnection.getURL()");
        return this.url;
    }

    // Note: We don't implement the following methods so that they default to
    // the JDK implementation. They will all call
    // <code>getHeaderField(String)</code> which we have overridden.

    // java.net.HttpURLConnection#getHeaderFieldDate(String, long)
    // java.net.HttpURLConnection#getContentLength()
    // java.net.HttpURLConnection#getContentType()
    // java.net.HttpURLConnection#getContentEncoding()
    // java.net.HttpURLConnection#getDate()
    // java.net.HttpURLConnection#getHeaderFieldInt(String, int)
    // java.net.HttpURLConnection#getExpiration()
    // java.net.HttpURLConnection#getLastModified()

    /**
     * Not available: the data must have already been retrieved.
     */
    public void setInstanceFollowRedirects(boolean isFollowingRedirects) {
        LOG.trace("enter HttpURLConnection.setInstanceFollowRedirects(boolean)");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     */
    public boolean getInstanceFollowRedirects() {
        LOG.trace("enter HttpURLConnection.getInstanceFollowRedirects()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setRequestMethod(String)
     */
    public void setRequestMethod(String method) throws ProtocolException {
        LOG.trace("enter HttpURLConnection.setRequestMethod(String)");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getPermission()
     */
    public Permission getPermission() throws IOException {
        LOG.trace("enter HttpURLConnection.getPermission()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getContent()
     */
    public Object getContent() throws IOException {
        LOG.trace("enter HttpURLConnection.getContent()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not yet implemented.
     */
    public Object getContent(Class[] classes) throws IOException {
        LOG.trace("enter HttpURLConnection.getContent(Class[])");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        LOG.trace("enter HttpURLConnection.getOutputStream()");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setDoInput(boolean)
     */
    public void setDoInput(boolean isInput) {
        LOG.trace("enter HttpURLConnection.setDoInput()");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getDoInput()
     */
    public boolean getDoInput() {
        LOG.trace("enter HttpURLConnection.getDoInput()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setDoOutput(boolean)
     */
    public void setDoOutput(boolean isOutput) {
        LOG.trace("enter HttpURLConnection.setDoOutput()");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getDoOutput()
     */
    public boolean getDoOutput() {
        LOG.trace("enter HttpURLConnection.getDoOutput()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setAllowUserInteraction(boolean)
     */
    public void setAllowUserInteraction(boolean isAllowInteraction) {
        LOG.trace("enter HttpURLConnection.setAllowUserInteraction(boolean)");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getAllowUserInteraction()
     */
    public boolean getAllowUserInteraction() {
        LOG.trace("enter HttpURLConnection.getAllowUserInteraction()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setUseCaches(boolean)
     */
    public void setUseCaches(boolean isUsingCaches) {
        LOG.trace("enter HttpURLConnection.setUseCaches(boolean)");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getUseCaches()
     */
    public boolean getUseCaches() {
        LOG.trace("enter HttpURLConnection.getUseCaches()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setIfModifiedSince(long)
     */
    public void setIfModifiedSince(long modificationDate) {
        LOG.trace("enter HttpURLConnection.setIfModifiedSince(long)");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getIfModifiedSince()
     */
    public long getIfModifiedSince() {
        LOG.trace("enter HttpURLConnection.getIfmodifiedSince()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#getDefaultUseCaches()
     */
    public boolean getDefaultUseCaches() {
        LOG.trace("enter HttpURLConnection.getDefaultUseCaches()");
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setDefaultUseCaches(boolean)
     */
    public void setDefaultUseCaches(boolean isUsingCaches) {
        LOG.trace("enter HttpURLConnection.setDefaultUseCaches(boolean)");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not available: the data must have already been retrieved.
     * @see java.net.HttpURLConnection#setRequestProperty(String,String)
     */
    public void setRequestProperty(String key, String value) {
        LOG.trace("enter HttpURLConnection.setRequestProperty()");
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * Not yet implemented.
     * @see java.net.HttpURLConnection#getRequestProperty(String)
     */
    public String getRequestProperty(String key) {
        LOG.trace("enter HttpURLConnection.getRequestProperty()");
        throw new RuntimeException("Not implemented yet");
    }

}

