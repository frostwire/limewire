/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestMethodsRedirectNoHost.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.*;

/**
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Revision: 1.1.1.1 $
 */
public class TestMethodsRedirectNoHost extends TestCase {

    
    SimpleHttpConnection conn;
 

    // ------------------------------------------------------------ Constructor

    public TestMethodsRedirectNoHost(String testName) {
        super(testName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestMethodsRedirectNoHost.class);
    }

    public void setUp() throws Exception{
        conn = new SimpleHttpConnection();
    }


    private void addRedirectResponse(String location) {
        String headers = "HTTP/1.1 302 Redirect\r\n"
                       +"Date: Wed, 28 Mar 2002 05:05:04 GMT\r\n"
                       +"Location: " + location + "\r\n"
                       +"Connection: close\r\n";
        conn.addResponse(headers, "");
    }

    private void addOkResponse() {
        String headers = "HTTP/1.1 200 OK\r\n"
                       +"Date: Wed, 28 Mar 2001 05:05:04 GMT\r\n"
                       +"Connection: close\r\n";
        conn.addResponse(headers, "");
    }


    // ----------------------------------------------------------------- Tests

    public void testRedirect() throws Exception {
        addRedirectResponse("http://localhost/newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(200, method.getStatusCode());
        assertEquals("/newfile", method.getPath());
        
    }


    public void testRedirectIgnoreCase() throws Exception {
        addRedirectResponse("HtTP://localhost/newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(200, method.getStatusCode());
        assertEquals("/newfile", method.getPath());
        
    }


    public void testPostRedirect() throws Exception {
        addRedirectResponse("http://localhost/newfile");
        addOkResponse();
        conn.open();

        PostMethod method = new PostMethod("/oldfile");
        method.setRequestBody(new NameValuePair[] { new NameValuePair("name", "value") } );
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
        
    }


    public void testNoRedirect() throws Exception {

        addRedirectResponse("http://localhost/newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(false);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
        
    }
 

    public void testRedirectBadLocation() throws Exception {
        addRedirectResponse("newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.setStrictMode(false);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(200, method.getStatusCode());
        assertEquals("/newfile", method.getPath());
    }

   
    public void testRedirectBadLocationStrict() throws Exception {
        addRedirectResponse("newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.setStrictMode(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
    }   

    public void testRedirectBogusLocationStrict() throws Exception {
        addRedirectResponse("xxx://bogus");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.setStrictMode(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
    }

    public void testRedirectDifferentHost() throws Exception {
        conn = new SimpleHttpConnection("oldhost", 80);
        addRedirectResponse("http://newhost/newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
    }

    public void testRedirectDifferentPort() throws Exception {
        conn = new SimpleHttpConnection("oldhost", 80);
        addRedirectResponse("http://oldhost:8080/newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
    }


    public void testRedirectDifferentProtocol() throws Exception {
        conn = new SimpleHttpConnection("oldhost", 80);
        addRedirectResponse("https://oldhost:80/newfile");
        addOkResponse();
        conn.open();

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.execute(new HttpState(), conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(302, method.getStatusCode());
        assertEquals("/oldfile", method.getPath());
    }


    public void testRedirectWithCookie() throws Exception {
        addRedirectResponse("http://localhost/newfile");
        addOkResponse();
        conn.open();

        HttpState state = new HttpState();
        state.addCookie(
            new Cookie("localhost", "name", "value", "/", -1, false)); 

        HttpMethod method = new SimpleHttpMethod("/oldfile");
        method.setFollowRedirects(true);
        method.execute(state, conn);
        Header locationHeader = method.getResponseHeader("Location");
        assertEquals(200, method.getStatusCode());

        Header[] headers = method.getRequestHeaders();
        int cookiecount = 0;
        for (int i = 0; i < headers.length; i++) {
            if ("cookie".equalsIgnoreCase(headers[i].getName())) {
                ++cookiecount;
            }
        }
        assertTrue("There can only be one (cookie)", cookiecount == 1);            
    }

}
