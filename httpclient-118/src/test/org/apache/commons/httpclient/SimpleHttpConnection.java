/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/SimpleHttpConnection.java,v 1.3 2004/08/23 17:38:35 sberlin Exp $
 * $Revision: 1.3 $
 * $Date: 2004/08/23 17:38:35 $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * For test-nohost testing purposes only.
 *
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Michael Becke
 */
class SimpleHttpConnection extends HttpConnection {

    static Log log = LogFactory.getLog("httpclient.test");

    int hits = 0;

    Vector headers = new Vector();
    Vector bodies = new Vector();

    InputStream inputStream;

    ByteArrayOutputStream bodyOutputStream = null;

    public void addResponse(String header) {
        addResponse(header, "");
    }

    public void addResponse(String header, String body) {
        headers.add(header);
        bodies.add(body);
    }

    public SimpleHttpConnection(String header, String body) {
        this();
        headers.add(header);
        bodies.add(body);
    }

    public SimpleHttpConnection() {
        super(null, -1, "localhost", null, 80, Protocol.getProtocol("http"));
    }

    public SimpleHttpConnection(
        String proxyHost,
        int proxyPort,
        String host,
        String virtualHost,
        int port,
        Protocol protocol) {
        super(proxyHost, proxyPort, host, virtualHost, port, protocol);
    }

    public SimpleHttpConnection(String host, int port){
        super(host, port, Protocol.getProtocol("http"));
    }

    public SimpleHttpConnection(String host, int port, String schema){
        super(host, port, Protocol.getProtocol(schema));
    }

    public void assertOpen() throws IllegalStateException {
        if (inputStream == null) {
            throw new IllegalStateException();
        }
    }

    public void assertNotOpen() throws IllegalStateException{
        if (inputStream != null) {
            throw new IllegalStateException();
        }
    }
    
    public boolean isOpen() {
        return inputStream != null;
    }
    
    public void open() throws IOException {
        if (inputStream != null) return;

        try{
            log.debug("hit: " + hits);
            
            // write the header to a byte array
            ByteArrayOutputStream headerOutputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter( headerOutputStream );
            writer.write((String) headers.elementAt(hits));
            // terminate the headers
            writer.write("\r\n");
            writer.close();

            byte[] headerContent = headerOutputStream.toByteArray();
            byte[] bodyContent = HttpConstants.getContentBytes((String)bodies.elementAt(hits));

            // combine the header and body content so they can be read from one steam
            byte[] content = new byte[headerContent.length + bodyContent.length];
            System.arraycopy(headerContent, 0, content, 0, headerContent.length);
            System.arraycopy(bodyContent, 0, content, headerContent.length, bodyContent.length);         

            inputStream = new ByteArrayInputStream( content );
            bodyOutputStream = new ByteArrayOutputStream();
            hits++;
        } catch (ArrayIndexOutOfBoundsException aiofbe) {
            throw new IOException("SimpleHttpConnection has been opened more times " +
                    "than it has responses.  You might need to call addResponse().");
        }
    }

    public void close() {
        if (inputStream != null) {
            try { inputStream.close(); } catch(IOException e) {}
            inputStream = null;
        }
        if (bodyOutputStream != null) {
            try { bodyOutputStream.close(); } catch(IOException e) {}
            bodyOutputStream = null;
        }
    }

    public boolean isResponseAvailable() throws IOException {
        assertOpen();
        return inputStream.available() > 0;
    }

    public boolean isResponseAvailable(int timeout) throws IOException {
        return isResponseAvailable();
    }

    public void write(byte[] data)
    throws IOException, IllegalStateException, HttpRecoverableException {
    }

    public void writeLine()
    throws IOException, IllegalStateException, HttpRecoverableException {
    }

    public String readLine()
    throws IOException, IllegalStateException {
        String str = HttpParser.readLine(inputStream);
        log.debug("read: " + str);
        return str;
    }

    public InputStream getResponseInputStream() {
        return inputStream;
    }

    public OutputStream getRequestOutputStream() {
        return bodyOutputStream;
    }

    public void flushRequestOutputStream() throws IOException {
        assertOpen();
    }
}

