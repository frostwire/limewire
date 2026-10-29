/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestWebappMethods.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.*;
import org.apache.commons.httpclient.methods.*;

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
 * @author Ortwin Glück
 * @version $Id: TestWebappMethods.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestWebappMethods extends TestWebappBase {

    public TestWebappMethods(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappMethods.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappMethods.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests

    /**
     * Simple test of {@link GetMethod} against /httpclienttest/params.
     */
    public void testGetMethod() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());

        method.recycle();

        method.setPath("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
    }

    /**
     * Simple test of {@link PostMethod} against /httpclienttest/params.
     */
    public void testPostMethod() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/params");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: POST</title>") >= 0);
        assertEquals(200,method.getStatusCode());

        method.recycle();

        method.setPath("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: POST</title>") >= 0);
        assertEquals(200,method.getStatusCode());
    }

    /**
     * Simple test of {@link HeadMethod} against /httpclienttest/params.
     */
    public void testHeadMethod() throws Exception {
        HttpClient client = createHttpClient();
        HeadMethod method = new HeadMethod("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());

        method.recycle();

        method.setPath("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
    }

    /**
     * Simple test of {@link OptionsMethod} against /httpclienttest/params.
     */
    public void testOptionsMethod() throws Exception {
        HttpClient client = createHttpClient();
        OptionsMethod method = new OptionsMethod("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getAllowedMethods().hasMoreElements());

        method.recycle();

        method.setPath("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getAllowedMethods().hasMoreElements());
    }

    /**
     * Simple test of {@link OptionsMethod} against the path "*".
     */
    public void testOptionsStar() throws Exception {
        HttpClient client = createHttpClient();
        OptionsMethod method = new OptionsMethod("*");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getAllowedMethods().hasMoreElements());
    }

    /**
     * Simple test of {@link DeleteMethod} against /httpclienttest/params.
     */
    public void testDeleteMethod() throws Exception {
        HttpClient client = createHttpClient();
        DeleteMethod method = new DeleteMethod("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());

        method.recycle();

        method.setPath("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
    }

    /**
     * Simple test of {@link PutMethod} against /httpclienttest/params.
     */
    public void testPutMethod() throws Exception {
        HttpClient client = createHttpClient();
        PutMethod method = new PutMethod("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString(),method.getResponseBodyAsString().indexOf("<title>Param Servlet: PUT</title>") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString(),method.getResponseBodyAsString().indexOf("<title>Param Servlet: PUT</title>") >= 0);
    }

    public void testPostBodyNVP() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        method.setRequestBody(new NameValuePair[] { 
           new NameValuePair("quote","It was the best of times, it was the worst of times.") } );
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<tt>quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.</tt>") >= 0);
    }

    public void testPostBody() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        method.setRequestBody("quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<tt>quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.</tt>") >= 0);
        assertEquals(200,method.getStatusCode());
    }


    public void testPostBodyCustomLength() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        String bodyStr = "quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.";
		byte[] body = HttpConstants.getContentBytes(bodyStr);

        method.setRequestBody(new ByteArrayInputStream(body));
        method.setRequestContentLength(body.length);
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<tt>quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.</tt>") >= 0);
        assertEquals(200,method.getStatusCode());
    }


    public void testPostBodyAutoLength() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        String body = "quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.";
        method.setRequestBody(body);
        method.setRequestContentLength(PostMethod.CONTENT_LENGTH_AUTO);
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<tt>quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.</tt>") >= 0);
        assertEquals(200,method.getStatusCode());
    }


    public void testPostBodyChunked() {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        String body = "quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.";
        method.setRequestBody(body);
        method.setRequestContentLength(PostMethod.CONTENT_LENGTH_CHUNKED);
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<tt>quote=It+was+the+best+of+times%2C+it+was+the+worst+of+times.</tt>") >= 0);
        assertEquals(200,method.getStatusLine().getStatusCode());
    }


    public void testPutBody() throws Exception {
        HttpClient client = createHttpClient();
        PutMethod method = new PutMethod("/" + getWebappContext() + "/body");
        method.setRequestBody("This is data to be sent in the body of an HTTP PUT.");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString(),method.getResponseBodyAsString().indexOf("<tt>This is data to be sent in the body of an HTTP PUT.</tt>") >= 0);
        assertEquals(200,method.getStatusCode());
    }


    public void testPostMethodRecycle() {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        String bodyStr = "Like, hello, and stuff";
        byte [] body = HttpConstants.getContentBytes(bodyStr);
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestBody(new ByteArrayInputStream(body));
        method.setRequestContentLength(body.length);
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusLine().getStatusCode());
        String response = method.getResponseBodyAsString();

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestBody(new ByteArrayInputStream(body));
        method.setRequestContentLength(body.length);
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
    }

    public void testEmptyPostMethod() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/body");
        
        method.setRequestHeader("Content-Type", "text/plain");
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        String response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestBody((String)null);
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestBody((InputStream)null);
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestBody("");
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        method.setRequestBody((String)null);
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        method.setRequestBody((InputStream)null);
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method.recycle();

        method.setPath("/" + getWebappContext() + "/body");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        method.setRequestBody("");
        client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();

    }

}
