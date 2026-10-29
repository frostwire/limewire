/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestWebappRedirect.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @version $Id: TestWebappRedirect.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestWebappRedirect extends TestWebappBase {
    
    private static final Log log = LogFactory.getLog(TestWebappRedirect.class);

    private final String redirectUrl = "/" + getWebappContext() + "/redirect";

    private final String paramsUrl =  "/" + getWebappContext() + "/params";

    private final String bodyUrl =  "/" + getWebappContext() + "/body";

    public TestWebappRedirect(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappRedirect.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappRedirect.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public void absoluteRedirectHelper(int code) throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod(redirectUrl);
        method.setQueryString("to=" + paramsUrl + "&code=" + code);
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
    }


    // ------------------------------------------------------------------ Tests

    public void testAbsoluteRedirectCode301() throws Exception {
        absoluteRedirectHelper(301);
    }

    public void testAbsoluteRedirectCode302() throws Exception {
        absoluteRedirectHelper(302);
    }

    public void testAbsoluteRedirectCode303() throws Exception {
        absoluteRedirectHelper(303);
    }

    public void testAbsoluteRedirectCode307() throws Exception {
        absoluteRedirectHelper(307);
    }

    public void testRelativeRedirect() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod(redirectUrl);
        method.setQueryString("to=params");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
    }


    public void testRedirectWithQueryString() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod(redirectUrl);
        method.setQueryString(new NameValuePair[] {
            new NameValuePair("to", paramsUrl + "?foo=bar&bar=foo")
            }
        );
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"foo=bar&bar=foo\"</p>") >= 0);
    }

    public void testRecursiveRedirect() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod(redirectUrl);

        String qs = paramsUrl + "?foo=bar&bar=foo";
        for(int i=0;i<2;i++) {
            qs = redirectUrl + "?to=" + URIUtil.encodeWithinQuery(qs);
        }
        method.setQueryString("to=" + URIUtil.encodeWithinQuery(qs));
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"foo=bar&bar=foo\"</p>") >= 0);
    }

    public void testDetectRedirectLoop() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod(redirectUrl);
        method.setQueryString("loop=true");
        try {
            client.executeMethod(method);
            fail("Expected HTTPException");
        } catch (HttpException t) {
            // expected
        }
        assertEquals(302,method.getStatusCode());
        assertTrue(null != method.getResponseHeader("location"));
        assertTrue(null != (method.getResponseHeader("location")).getValue());
        assertEquals(client.getHostConfiguration().getHostURL() + "/" + getWebappContext() + "/redirect?loop=true",(method.getResponseHeader("location")).getValue());
        log.info("Previous redirect loop warining is okay");
    }

    public void testPostRedirect() throws Exception {
        String bodyStr = "Hello World";
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod(redirectUrl);
        method.setQueryString("to=" + URIUtil.encodeWithinQuery(
                    client.getHostConfiguration().getHostURL() + "/" 
                    + getWebappContext() + "/params?foo=bar&bar=foo"));
        byte[] body = HttpConstants.getContentBytes(bodyStr);
        method.setRequestBody(new ByteArrayInputStream(body));
        method.setRequestContentLength(body.length);  //unbuffered request
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        //unbuffered request can not be redirected
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY,method.getStatusCode());

        method = new PostMethod(redirectUrl);
        method.setQueryString("to=" + URIUtil.encodeWithinQuery(paramsUrl + "?foo=bar&bar=foo"));
        method.setRequestBody(new ByteArrayInputStream(body));
        method.setRequestContentLength(PostMethod.CONTENT_LENGTH_AUTO); //buffered request
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        //buffered request is okay to redirect
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY,method.getStatusCode());
    }

    public void testPutRedirect() throws Exception {
        HttpClient client = createHttpClient();
        PutMethod method = new PutMethod(redirectUrl);
        method.setQueryString("to=" + URIUtil.encodeWithinQuery(bodyUrl + "?foo=bar&bar=foo"));
        method.setRequestBody("This is data to be sent in the body of an HTTP PUT.");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY,method.getStatusCode());
    }
}

