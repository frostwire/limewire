/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestWebappBasicAuth.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

/**
 * This suite of tests depends upon the httpclienttest webapp,
 * which is available in the httpclient/src/test-webapp
 * directory in the CVS tree.
 * <p>
 * The webapp should be deployed in the context "httpclienttest"
 * on a servlet engine running on port 8080 on the localhost
 * (IP 127.0.0.1).
 * <p>
 * You can change the assumed port by setting the
 * "httpclient.test.localPort" property.
 * You can change the assumed host by setting the
 * "httpclient.test.localHost" property.
 * You can change the assumed context by setting the
 * "httpclient.test.webappContext" property.
 *
 * @author Rodney Waldhoff
 * @version $Id: TestWebappBasicAuth.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestWebappBasicAuth extends TestWebappBase {

    public TestWebappBasicAuth(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappBasicAuth.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappBasicAuth.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests

    public void testSimpleAuthGet() throws Exception {
        HttpClient client = createHttpClient();
        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));
        GetMethod method = new GetMethod("/" + getWebappContext() + "/auth/basic");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testSimpleAuthPost() throws Exception {
        HttpClient client = createHttpClient();
        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));
        PostMethod method = new PostMethod("/" + getWebappContext() + "/auth/basic");
        method.setRequestBody(new NameValuePair[] { new NameValuePair("testing","one") } );
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: POST</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        method.setRequestBody(new NameValuePair[] { new NameValuePair("testing","one") } );
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: POST</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testSimpleAuthPut() throws Exception {
        HttpClient client = createHttpClient();
        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));
        PutMethod method = new PutMethod("/" + getWebappContext() + "/auth/basic");
        method.setRequestBody("testing one two three");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testNoCredAuthRetry() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/auth/basic");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(401,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>Not authorized.</p>") >= 0);

        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("jakarta","commons"));

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>You have authenticated as \"jakarta:commons\"</p>") >= 0);
    }

    public void testBadCredFails() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/auth/basic");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(HttpStatus.SC_UNAUTHORIZED,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>Not authorized.</p>") >= 0);

        client.getState().setCredentials("BasicAuthServlet",new UsernamePasswordCredentials("bad","creds"));

        method.recycle();
        method.setPath("/" + getWebappContext() + "/auth/basic");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(HttpStatus.SC_UNAUTHORIZED,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>BasicAuth Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>Not authorized. \"Basic YmFkOmNyZWRz\" not recognized.</p>") >= 0);
    }
    
    public void testHeadAuth() throws Exception {
        HttpClient client = new HttpClient();
        HttpState state = client.getState();
        Credentials cred = new UsernamePasswordCredentials("jakarta", "commons");
        state.setCredentials(null, cred);
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(getHost(), getPort(), getProtocol());
        client.setHostConfiguration(hc);
        client.setState(state);
        HeadMethod method = new HeadMethod("/"+ getWebappContext() +"/auth/basic");
        client.executeMethod(method);
        method.releaseConnection();
        assertEquals(200, method.getStatusCode());
    }
    
}




