/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestMethodsExternalHost.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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
import java.util.Enumeration;
import junit.framework.*;
import org.apache.commons.httpclient.methods.*;

/**
 * Simple tests for the HTTP client hitting an external webserver.
 *
 * This test suite assumes you have an internet connection that
 * can communicate with http://java.sun.com/.
 *
 * @author Remy Maucherat
 * @author Rodney Waldhoff
 * @author Ortwin Glück
 * @author Jeff Dever
 * @version $Id: TestMethodsExternalHost.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestMethodsExternalHost extends TestCase {

    private HttpClient client;
    private HttpMethod method;

    // -------------------------------------------------------------- Constants

    private static final String externalHost = "java.sun.com";
    private static final int externalPort = 80;
    private static final String externalPath = "/index.html";
    private static final String externalUri = "http://java.sun.com/index.html";
    private final String PROXY_HOST = System.getProperty("httpclient.test.proxyHost");
    private final String PROXY_PORT = System.getProperty("httpclient.test.proxyPort");
    private final String PROXY_USER = System.getProperty("httpclient.test.proxyUser");
    private final String PROXY_PASS = System.getProperty("httpclient.test.proxyPass");

    // ------------------------------------------------------------ Constructor


    public TestMethodsExternalHost(String testName) {
        super(testName);
    }


    // ------------------------------------------------------- TestCase Methods


    public static Test suite() {
        return new TestSuite(TestMethodsExternalHost.class);
    }

    // ------------------------------------------------------- Helper Methods
    
    public void setUp() {
        client = new HttpClient();

	    client.getHostConfiguration().setHost(externalHost, externalPort, "http");

        if (PROXY_HOST != null) {
            if (PROXY_USER != null) {
                HttpState state = client.getState();
                state.setProxyCredentials(null, new UsernamePasswordCredentials(
                    PROXY_USER, PROXY_PASS));
            }
            client.getHostConfiguration().setProxy(PROXY_HOST, Integer.parseInt(PROXY_PORT));
        }
    }

    public void tearDown() {
        method.releaseConnection();
        method = null;
        client = null;
    }

    public void executeMethod() {
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
    }

    // ----------------------------------------------------------- OPTIONS Test


    public void testMethodsOptionsExternal() {

        method = new OptionsMethod(externalPath);
        executeMethod();

        Enumeration methodsAllowed = ((OptionsMethod)method).getAllowedMethods();
        // This enumeration musn't be empty
        assertTrue("No HTTP method allowed : result of OPTIONS is incorrect.",
               methodsAllowed.hasMoreElements());

    }
    // --------------------------------------------------------------- GET Test


    public void testMethodsGetExternal() {

        method = new GetMethod(externalUri);
        executeMethod();

        try {
            String data = method.getResponseBodyAsString();
            // This enumeration musn't be empty
            assertTrue("No data returned.",
                   (data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }

        method.recycle();
        method.setPath(externalPath);
        executeMethod();

        try {
            String data = method.getResponseBodyAsString();
            // This enumeration musn't be empty
            assertTrue("No data returned.",
                   (data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }

    }


    // -------------------------------------------------------------- HEAD Test

    public void testMethodsHeadExternal() {

        method = new HeadMethod(externalPath);
        executeMethod();

        assertTrue("Method failed : " + method.getStatusCode(),
               (method.getStatusCode() == 200));

    }

    /**
     * This test proves that bad urls throw an IOException,
     * and not some other throwable like a NullPointerException.
     *
     * FIXME: Bad urls don't throw an IOException.
     */
    public void testIOException() {

        method = new GetMethod("http://www.bogusurl.xyz");

        try {
            client.executeMethod(method);
            if ((PROXY_HOST != null) && (method.getStatusCode() >= 400)) return;
        } catch (IOException e) {
            return; // IOException and HttpException are ok
        }

        fail("Should have thrown an exception");

    }


    /**
     * http://nagoya.apache.org/bugzilla/show_bug.cgi?id=16864
     */
    public void testDomino_Go_Webserver404() throws Exception {

        // this file should not exist
        method = new GetMethod("http://www.pc.ibm.com/us/accessories/monitors/p_allmodelos.html");
        int statusCode = client.executeMethod(method);

        assertEquals(404, method.getStatusCode());

    }


}
