/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestHttpConnection.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 *
 * Unit tests for {@link HttpConnection}.
 *
 * @author Sean C. Sullivan
 *
 * @version $Id: TestHttpConnection.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 *
 */
public class TestHttpConnection extends TestLocalHostBase {
    
    // ------------------------------------------------------------ Constructor
    public TestHttpConnection(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpConnection.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpConnection.class);
    }


    // ----------------------------------------------------------- Test Methods

    public void testConstructThenClose() {
        HttpConnection conn = new HttpConnection(getHost(), getPort());
        conn.close();
        assertTrue( ! conn.isOpen() );
    }

    public void testConnTimeout() {

        // create a custom protocol that will delay for 500 milliseconds
        Protocol testProtocol = new Protocol(
            "timeout",
            new DelayedProtocolSocketFactory(
                500, 
                Protocol.getProtocol("http").getSocketFactory()
            ),
            getPort()
        );

        HttpConnection conn = new HttpConnection(getHost(), getPort(), testProtocol);
        // 1 ms is short enough to make this fail
        conn.setConnectionTimeout(1);
        try {
            conn.open();
            fail("Should have timed out");
        } catch(IOException e) {
            assertTrue(e instanceof HttpConnection.ConnectionTimeoutException);
            /* should fail */
        }
    }

    public void testForIllegalStateExceptions() {
        HttpConnection conn = new HttpConnection(getHost(), getPort());

        try {
            OutputStream out = conn.getRequestOutputStream();
            fail("getRequestOutputStream did not throw the expected exception");
        }
        catch (IllegalStateException expected) {
            // this exception is expected
        }
        catch (IOException ex) {
            fail("getRequestOutputStream did not throw the expected exception");
        }

        try {
            OutputStream out = new ChunkedOutputStream(conn.getRequestOutputStream());
            fail("getRequestOutputStream(true) did not throw the expected exception");
        }
        catch (IllegalStateException expected) {
            // this exception is expected
        }
        catch (IOException ex) {
            fail("getRequestOutputStream(true) did not throw the expected exception");
        }

        try {
            InputStream in = conn.getResponseInputStream();
            fail("getResponseInputStream() did not throw the expected exception");
        }
        catch (IllegalStateException expected) {
            // this exception is expected
        }
        catch (IOException ex) {
            fail("getResponseInputStream() did not throw the expected exception");
        }

    }
    
    /**
     * A ProtocolSocketFactory that delays before creating a socket.
     */
    class DelayedProtocolSocketFactory implements ProtocolSocketFactory {
        
        private int delay;
        private ProtocolSocketFactory realFactory;
            
        public DelayedProtocolSocketFactory(int delay, ProtocolSocketFactory realFactory) {
            this.delay = delay;
            this.realFactory = realFactory;            
        }
                
        public Socket createSocket(
            String host,
            int port,
            InetAddress clientHost,
            int clientPort
        ) throws IOException, UnknownHostException {
            
            synchronized (this) {
                try {
                    this.wait(delay);
                } catch (InterruptedException e) {}
            }
            return realFactory.createSocket(host, port, clientHost, clientPort);
        }

        public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
            synchronized (this) {
                try {
                    this.wait(delay);
                } catch (InterruptedException e) {}
            }
            return realFactory.createSocket(host, port);
        }

    }

}

