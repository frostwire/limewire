/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestHttps.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Simple tests for HTTPS support in HttpClient.
 *
 * To run this test you'll need:
 *  + a JSSE implementation installed (see README.txt)
 *  + the java.protocol.handler.pkgs system property set
 *    for your provider.  e.g.:
 *     -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol
 *    (see build.xml)
 *
 * @author Rodney Waldhoff
 * @author Ortwin Glück
 * @version $Id: TestHttps.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestHttps extends TestCase {

    // ---------------------------------------------------------------- Members
    private String _urlWithPort = null;
    private String _urlWithoutPort = null;
    private final String PROXY_HOST = System.getProperty("httpclient.test.proxyHost");
    private final String PROXY_PORT = System.getProperty("httpclient.test.proxyPort");
    private final String PROXY_USER = System.getProperty("httpclient.test.proxyUser");
    private final String PROXY_PASS = System.getProperty("httpclient.test.proxyPass");

    // ------------------------------------------------------------ Constructor
    public TestHttps(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttps.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestHttps.class);
    }

    public void setUp() throws Exception {
        _urlWithPort = "https://www.verisign.com:443/";
        _urlWithoutPort = "https://www.verisign.com/";
    }

    public void testHttpsGet() {
        HttpClient client = new HttpClient();
        if (PROXY_HOST != null) {
            if (PROXY_USER != null) {
                HttpState state = client.getState();
                state.setProxyCredentials(null, new UsernamePasswordCredentials(
                    PROXY_USER, PROXY_PASS));
            }
            client.getHostConfiguration().setProxy(PROXY_HOST, Integer.parseInt(PROXY_PORT));
        }
        GetMethod method = new GetMethod(_urlWithPort);
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown during HTTPS GET: " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            // This enumeration musn't be empty
            assertTrue("No data returned.", (data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown while retrieving data : " + t.toString());
        }
    }

    public void testHttpsGetNoPort() {
        HttpClient client = new HttpClient();
        if (PROXY_HOST != null) {
            if (PROXY_USER != null) {
                HttpState state = client.getState();
                state.setProxyCredentials(null, new UsernamePasswordCredentials(
                    PROXY_USER, PROXY_PASS));
            }
            client.getHostConfiguration().setProxy(PROXY_HOST, Integer.parseInt(PROXY_PORT));
        }
        GetMethod method = new GetMethod(_urlWithoutPort);
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown during HTTPS GET: " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            // This enumeration musn't be empty
            assertTrue("No data returned.", (data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown while retrieving data : " + t.toString());
        }
    }
}
