/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestHttpConnectionManager.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Unit tests for {@link HttpConnectionManager}.
 *
 * @author Marc A. Saegesser
 * @version $Id: TestHttpConnectionManager.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestHttpConnectionManager extends TestLocalHostBase {

    // ------------------------------------------------------------ Constructor
    public TestHttpConnectionManager(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpConnectionManager.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpConnectionManager.class);
    }


    // ----------------------------------------------------------- Test Methods

    // Test the accessor methods
    public void testMaxConnectionsAccessors() {
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();

        // First test the default value
        assertEquals("Default MaxConnections", 2, mgr.getMaxConnectionsPerHost());

        mgr.setMaxConnectionsPerHost(10);
        assertEquals("MaxConnections", 10, mgr.getMaxConnectionsPerHost());
    }

    /**
     * Test that the ConnectMethod correctly releases connections when
     * CONNECT fails.
     */
    public void testConnectMethodFailureRelease() {
        
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
        mgr.setMaxTotalConnections(1);
        
        // we're going to execute a connect method against the localhost, assuming
        // that CONNECT is not supported.  This should test the fakeResponse()
        // code on HttpMethodBase.
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(getHost(), getPort(), getProtocol());
        
        GetMethod get = new GetMethod("/");
        try {
            HttpConnection connection = mgr.getConnection(hostConfiguration);
            ConnectMethod connect = new ConnectMethod(get);
            assertTrue(connect.execute(new HttpState(), connection) != 200);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error executing connect: " + e);
        }

        // this should calling releaseConnection() releases the connection
        try {
            get.releaseConnection();
            mgr.getConnection(hostConfiguration, 1).releaseConnection();
        } catch (HttpException e1) {
            fail("Connection should have been available.");
        }
        
        get = new GetMethod("/");
        
        try {
            HttpConnection connection = mgr.getConnection(hostConfiguration);
            ConnectMethod connect = new ConnectMethod(get);                        
            assertTrue(connect.execute(new HttpState(), connection) != 200);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error executing connect: " + e);
        }

        // make sure reading the response fully releases the connection        
        try {
            get.getResponseBodyAsString();
            mgr.getConnection(hostConfiguration, 1).releaseConnection();
        } catch (HttpException e1) {
            fail("Connection should have been available.");
        }     
        
        get = new GetMethod("/");
        
        try {
            HttpConnection connection = mgr.getConnection(hostConfiguration);
            ConnectMethod connect = new ConnectMethod(get);                        
            assertTrue(connect.execute(new HttpState(), connection) != 200);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error executing connect: " + e);
        }

        // make sure closing the output stream releases the connection        
        try {
            get.getResponseBodyAsStream().close();
            mgr.getConnection(hostConfiguration, 1).releaseConnection();
        } catch (HttpException e) {
            fail("Connection should have been available.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Close connection failed: " + e);   
        }
    }

    public void testGetConnection() {
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("www.nosuchserver.com", 80, "http");

        // Create a new connection
        HttpConnection conn = mgr.getConnection(hostConfiguration);
        // Validate the connection properties
        assertEquals("Host", "www.nosuchserver.com", conn.getHost());
        assertEquals("Port", 80, conn.getPort());
        // Release the connection
        mgr.releaseConnection(conn);

        // Create a new connection
        hostConfiguration.setHost("www.nosuchserver.com", -1, "https");
        conn = mgr.getConnection(hostConfiguration);
        // Validate the connection properties
        assertEquals("Host", "www.nosuchserver.com", conn.getHost());
        assertEquals("Port", 443, conn.getPort());
        // Release the connection
        mgr.releaseConnection(conn);

        // Create a new connection
        hostConfiguration.setHost("www.nowhere.org", 8080, "http");
        conn = mgr.getConnection(hostConfiguration);
        // Validate the connection properties
        assertEquals("Host", "www.nowhere.org", conn.getHost());
        assertEquals("Port", 8080, conn.getPort());
        // Release the connection
        mgr.releaseConnection(conn);

    }

    public void testReleaseConnection() {

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);

        HttpClient client = createHttpClient(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.setHttpConnectionFactoryTimeout( 1 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }

        try {
            // this should fail quickly since the connection has not been released
            client.executeMethod(getMethod);
            fail("a httpConnection should not be available");
        } catch (HttpException e) {            
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

        // this should release the connection
        getMethod.releaseConnection();

        getMethod = new GetMethod("/");

        try {
            // this should fail quickly if the connection has not been released
            client.executeMethod(getMethod);
        } catch (HttpException e) {
            fail("httpConnection does not appear to have been released: " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

    }

    /**
     * Makes sure that a connection gets released after the content of the body
     * is read.
     */
    public void testResponseAutoRelease() {

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);

        HttpClient client = createHttpClient(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.setHttpConnectionFactoryTimeout( 1 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }
        
        // this should release the connection
        getMethod.getResponseBody();

        getMethod = new GetMethod("/");

        try {
            // this should fail quickly if the connection has not been released
            client.executeMethod(getMethod);
        } catch (HttpException e) {
            fail("httpConnection does not appear to have been released: " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

    }
    
    /**
     * Tests the MultiThreadedHttpConnectionManager's ability to reclaim unused 
     * connections.
     */
    public void testConnectionReclaiming() {
        
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);
        connectionManager.setMaxTotalConnections(1);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HttpConnection connection = connectionManager.getConnection(host1);
        // now release this connection
        connection.releaseConnection();
        connection = null;
        
        try {
            // the connection from host1 should be reclaimed
            connection = connectionManager.getConnection(host2, 100);
        } catch (HttpException e) {
            e.printStackTrace();
            fail("a httpConnection should have been available: " + e);
        }        
    }
    
    /**
     * Tests the MultiThreadedHttpConnectionManager's ability to restrict the maximum number 
     * of connections.
     */    
    public void testMaxConnections() {
        
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);
        connectionManager.setMaxTotalConnections(2);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HttpConnection connection1 = connectionManager.getConnection(host1);
        HttpConnection connection2 = connectionManager.getConnection(host2);
    
        try {
            // this should fail quickly since the connection has not been released
            connectionManager.getConnection(host2, 100);
            fail("a httpConnection should not be available");
        } catch (HttpException e) {
            // this should throw an exception
        }
        
        // release one of the connections
        connection2.releaseConnection();
        connection2 = null;
        
        try {
            // there should be a connection available now
            connection2 = connectionManager.getConnection(host2, 100);
        } catch (HttpException e) {
            e.printStackTrace();
            fail("a httpConnection should have been available: " + e);
        }
    }    

    public void testHostReusePreference() {
        
        final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);
        connectionManager.setMaxTotalConnections(1);

        final HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        final HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HttpConnection connection = connectionManager.getConnection(host1);

        GetConnectionThread getHost1 = new GetConnectionThread(host1, connectionManager, 200);
        GetConnectionThread getHost2 = new GetConnectionThread(host2, connectionManager, 200);
        
        getHost2.start();
        getHost1.start();
        
        // give the threads some time to startup
        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
            
        // after the connection to host1 is released it should be given to getHost1
        connection.releaseConnection();
        connection = null;

        try {
            getHost1.join();
            getHost2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotSame(
            "Connection should have been given to someone", 
            getHost1.getConnection(),
            getHost2.getConnection()
        );        
        assertNotNull("Connection should have been given to host1", getHost1.getConnection());
        assertNull("Connection should NOT have been given to host2", getHost2.getConnection());
        
    } 
    
    public void testMaxConnectionsPerServer() {
     
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);

        HttpClient client = createHttpClient(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.setHttpConnectionFactoryTimeout( 1 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }

        GetMethod getMethod2 = new GetMethod("/");

        try {
            // this should fail quickly since the connection has not been released
            client.executeMethod(getMethod2);
            fail("a httpConnection should not be available");
        } catch (HttpException e) {
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }
                
    }
    
    public void testReclaimUnusedConnection() {

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(1);

        HttpClient client = createHttpClient(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.setHttpConnectionFactoryTimeout( 30000 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }

        getMethod = new GetMethod("/");
        
        Runtime.getRuntime().gc();

        try {
            // we didn't explicitly release the connection, but it should be 
            // reclaimed by the garbage collector, we hope:)
            client.executeMethod(getMethod);
        } catch (HttpException e) {
            fail("httpConnection does not appear to have been reclaimed by the GC: " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

    }
    
    public void testGetFromMultipleThreads() {
        
        HttpClient client = createHttpClient(new MultiThreadedHttpConnectionManager());
        ExecuteMethodThread[] threads = new ExecuteMethodThread[10];
        
        for (int i = 0; i < threads.length; i++) {
            GetMethod method = new GetMethod("/");
            method.setFollowRedirects(true);
            
            threads[i] = new ExecuteMethodThread(method, client);
            threads[i].start();
        }
        
        for (int i = 0; i < threads.length; i++) {
            try {
                // wait until this thread finishes. we'll give it 10 seconds,
                // but it shouldn't take that long
                threads[i].join(10000);
            } catch (InterruptedException e) {
            }
            // make sure an exception did not occur
            Exception e = threads[i].getException();
            if (e != null) {
                fail("An error occured in the get: " + e);
            }
            // we should have a 200 status
            assertEquals(threads[i].getMethod().getStatusCode(), HttpStatus.SC_OK);
        }
    }

    public void testTimeout() {
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
        mgr.setMaxConnectionsPerHost(2);
        
        try{
            HostConfiguration hostConfig = new HostConfiguration();
            hostConfig.setHost("www.nosuchserver.com", 80, "http");
            
            HttpConnection conn1 = mgr.getConnection(hostConfig);
            HttpConnection conn2 = mgr.getConnection(hostConfig);
            
            HttpConnection conn3 = mgr.getConnection(hostConfig, 1000);
            fail("Expected an HttpException.");
            
        }catch(HttpException e){
            //Expected result
        }
    }
    
    static class GetConnectionThread extends Thread {
        
        private HostConfiguration hostConfiguration;
        private MultiThreadedHttpConnectionManager connectionManager;
        private HttpConnection connection;
        private long timeout;
        
        public GetConnectionThread(
            HostConfiguration hostConfiguration, 
            MultiThreadedHttpConnectionManager connectionManager,
            long timeout
        ) {
            this.hostConfiguration = hostConfiguration;
            this.connectionManager = connectionManager; 
            this.timeout = timeout;
        }
        
        public void run() {
            try {
                connection = connectionManager.getConnection(hostConfiguration, timeout);
            } catch (HttpException e) {
            }            
        }
        
        public HttpConnection getConnection() {
            return connection;
        }

    }
    
}

