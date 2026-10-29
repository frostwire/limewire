/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestWebappCookie.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import junit.framework.*;

import org.apache.commons.httpclient.cookie.CookiePolicy;
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
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Id: TestWebappCookie.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestWebappCookie extends TestWebappBase {

    public TestWebappCookie(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappCookie.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappCookie.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }


    // ------------------------------------------------------------------ Tests

    public void testSetCookieGet() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertEquals(1,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
    }

    public void testSetCookiePost() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        PostMethod method = new PostMethod("/" + getWebappContext() + "/cookie/write");
        method.setRequestBody(new NameValuePair[] { new NameValuePair("simple","set") } );
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: POST</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertEquals(1,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
    }

    public void testSetCookiePut() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        PutMethod method = new PutMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set");
        method.setRequestBody("data to be sent via http post");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertEquals(1,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
    }

    public void testSetExpiredCookieGet() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=unset");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Deleted simplecookie.<br>") >= 0);
        assertEquals(0,client.getState().getCookies().length);
    }

    public void testSetExpiredCookiePut() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        PutMethod method = new PutMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=unset");
        method.setRequestBody("data to be sent via http post");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Deleted simplecookie.<br>") >= 0);
        assertEquals(0,client.getState().getCookies().length);
    }

    public void testSetUnsetCookieGet() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertEquals(1,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());

        method.recycle();
        
        method.setPath("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=unset");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Deleted simplecookie.<br>") >= 0);
        assertEquals(0,client.getState().getCookies().length);
    }

    public void testSetMultiCookieGetStrict() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set&domain=set");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote domaincookie.<br>") >= 0);
        assertEquals(2,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
        assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[1])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[1])).getValue());
    }


    public void testMultiSendCookieGetNonstrict() throws Exception {
        HttpClient client = createHttpClient();

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set&domain=set");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote domaincookie.<br>") >= 0);
        assertEquals(2,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
        assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[1])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[1])).getValue());

        GetMethod method2 = new GetMethod("/" + getWebappContext() + "/cookie/read");
        try {
            client.executeMethod(method2);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method2.getStatusCode());
        String s = method2.getResponseBodyAsString();
        assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
        assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; simplecookie=\"value\"</tt></p>") >= 0);
        assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; domaincookie=\"value\"; $Domain=\"" + getHost() + "\"</tt></p>") >= 0);
        assertTrue(s, s.indexOf("<tt>simplecookie=\"value\"</tt><br>") >= 0);
        assertTrue(s, s.indexOf("<tt>domaincookie=\"value\"</tt><br>") >= 0);
    }


    public void testSetMultiCookiePut() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        PutMethod method = new PutMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set&domain=set");
        method.setRequestBody("data to be sent via http post");
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: PUT</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote domaincookie.<br>") >= 0);
        assertEquals(2,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
        assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[1])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[1])).getValue());
    }

    public void testSendCookieGet() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertEquals(1,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());

        GetMethod method2 = new GetMethod("/" + getWebappContext() + "/cookie/read");
        
        try {
            client.executeMethod(method2);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method2.getStatusCode());
        String s = method2.getResponseBodyAsString();
        assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
        assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; simplecookie=\"value\"</tt></p>") >= 0);
        assertTrue(s, s.indexOf("<tt>simplecookie=\"value\"</tt><br>") >= 0);
    }

    public void testMultiSendCookieGet() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);

        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
        method.setQueryString("simple=set&domain=set");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("Wrote domaincookie.<br>") >= 0);
        assertEquals(2,client.getState().getCookies().length);
        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
        assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[1])).getName());
        assertEquals("value",((Cookie)(client.getState().getCookies()[1])).getValue());

        GetMethod method2 = new GetMethod("/" + getWebappContext() + "/cookie/read");
        
        try {
            client.executeMethod(method2);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method2.getStatusCode());
        String s = method2.getResponseBodyAsString();
        assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
        assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; simplecookie=\"value\"; domaincookie=\"value\"; $Domain=\"" + getHost() + "\"</tt></p>") >= 0);
        assertTrue(s, s.indexOf("<tt>simplecookie=\"value\"</tt><br>") >= 0);
        assertTrue(s, s.indexOf("<tt>domaincookie=\"value\"</tt><br>") >= 0);
    }

    public void testDeleteCookieGet() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);


        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
            method.setQueryString("simple=set&domain=set");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote domaincookie.<br>") >= 0);
            assertEquals(2,client.getState().getCookies().length);
            assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
            assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
            assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[1])).getName());
            assertEquals("value",((Cookie)(client.getState().getCookies()[1])).getValue());
        }

        {
            GetMethod method2 = new GetMethod("/" + getWebappContext() + "/cookie/read");
            
            try {
                client.executeMethod(method2);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method2.getStatusCode());
            String s = method2.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
            assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; simplecookie=\"value\"; domaincookie=\"value\"; $Domain=\"" + getHost() + "\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>simplecookie=\"value\"</tt><br>") >= 0);
            assertTrue(s, s.indexOf("<tt>domaincookie=\"value\"</tt><br>") >= 0);
        }

        {
            GetMethod method3 = new GetMethod("/" + getWebappContext() + "/cookie/write");

            method3.setQueryString("simple=unset");
            try {
                client.executeMethod(method3);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method3.getStatusCode());
            assertTrue(method3.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
            assertTrue(method3.getResponseBodyAsString().indexOf("Deleted simplecookie.<br>") >= 0);
            assertEquals(1,client.getState().getCookies().length);
            assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[0])).getName());
            assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
        }

        {
            GetMethod method4 = new GetMethod("/" + getWebappContext() + "/cookie/read");

            try {
                client.executeMethod(method4);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method4.getStatusCode());
            String s = method4.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
            assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; domaincookie=\"value\"; $Domain=\"" + getHost() + "\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>domaincookie=\"value\"</tt><br>") >= 0);
        }
    }

    public void testDeleteCookiePut() throws Exception {
        HttpClient client = createHttpClient();
        client.setStrictMode(true);


        {
            PutMethod method = new PutMethod("/" + getWebappContext() + "/cookie/write");
            method.setQueryString("simple=set&domain=set");
            method.setRequestBody("data to be sent via http post");
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: PUT</title>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote domaincookie.<br>") >= 0);
            assertEquals(2,client.getState().getCookies().length);
            assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
            assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
            assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[1])).getName());
            assertEquals("value",((Cookie)(client.getState().getCookies()[1])).getValue());
        }

        {
            PutMethod method2 = new PutMethod("/" + getWebappContext() + "/cookie/read");
            method2.setRequestBody("data to be sent via http post");
            try {
                client.executeMethod(method2);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method2.getStatusCode());
            String s = method2.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: PUT</title>") >= 0);
            assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; simplecookie=\"value\"; domaincookie=\"value\"; $Domain=\"" + getHost() + "\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>simplecookie=\"value\"</tt><br>") >= 0);
            assertTrue(s, s.indexOf("<tt>domaincookie=\"value\"</tt><br>") >= 0);
        }

        {
            PutMethod method3 = new PutMethod("/" + getWebappContext() + "/cookie/write");
            method3.setRequestBody("data to be sent via http post");
            method3.setQueryString("simple=unset");
            try {
                client.executeMethod(method3);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method3.getStatusCode());
            assertTrue(method3.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: PUT</title>") >= 0);
            assertTrue(method3.getResponseBodyAsString().indexOf("Deleted simplecookie.<br>") >= 0);
            assertEquals(1,client.getState().getCookies().length);
            assertEquals("domaincookie", ((Cookie)(client.getState().getCookies()[0])).getName());
            assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
        }

        {
            PutMethod method4 = new PutMethod("/" + getWebappContext() + "/cookie/read");
            method4.setRequestBody("data to be sent via http post");
            try {
                client.executeMethod(method4);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method4.getStatusCode());
            String s = method4.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: PUT</title>") >= 0);
            assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; domaincookie=\"value\"; $Domain=\"" + getHost() + "\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>domaincookie=\"value\"</tt><br>") >= 0);
        }
    }

    public void testPathCookie1() throws Exception {
        HttpClient client = createHttpClient();


        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
            method.setQueryString("path=/");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote pathcookie.<br>") >= 0);
            assertEquals(1,client.getState().getCookies().length);
            assertEquals("/",((Cookie)(client.getState().getCookies()[0])).getPath());
        }

        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/read");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            String s = method.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
            assertTrue(s ,s.indexOf("<p><tt>Cookie: $Version=\"1\"; pathcookie=\"value\"; $Path=\"/\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>pathcookie=\"value\"</tt><br>") >= 0);
        }
    }

    public void testPathCookie2() throws Exception {
        HttpClient client = createHttpClient();


        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
            method.setQueryString("path=/" + getWebappContext());
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote pathcookie.<br>") >= 0);
            assertEquals(1,client.getState().getCookies().length);
            assertEquals("/" + getWebappContext(),((Cookie)(client.getState().getCookies()[0])).getPath());
        }

        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/read");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            String s = method.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
            assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; pathcookie=\"value\"; $Path=\"/" + getWebappContext() +"\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>pathcookie=\"value\"</tt><br>") >= 0);
        }
    }

    public void testPathCookie3() throws Exception {
        HttpClient client = createHttpClient();

        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
            method.setQueryString("path=/" + getWebappContext() + "/cookie");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote pathcookie.<br>") >= 0);
            assertEquals(1,client.getState().getCookies().length);
            assertEquals("/" + getWebappContext() + "/cookie",((Cookie)(client.getState().getCookies()[0])).getPath());
        }

        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/read");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            String s = method.getResponseBodyAsString();
            assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
            assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; pathcookie=\"value\"; $Path=\"/" + getWebappContext() + "/cookie\"</tt></p>") >= 0);
            assertTrue(s, s.indexOf("<tt>pathcookie=\"value\"</tt><br>") >= 0);
        }
    }

    public void testPathCookie4() throws Exception {
        HttpClient client = createHttpClient();


        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
            method.setQueryString("path=/" + getWebappContext() + "/cookie/write");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
            assertTrue(method.getResponseBodyAsString().indexOf("Wrote pathcookie.<br>") >= 0);
            assertEquals(1,client.getState().getCookies().length);
            assertEquals("/" + getWebappContext() + "/cookie/write",((Cookie)(client.getState().getCookies()[0])).getPath());
        }

        {
            GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/read");
            
            try {
                client.executeMethod(method);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Unable to execute method : " + t.toString());
            }
            assertEquals(200,method.getStatusCode());
            assertTrue(method.getResponseBodyAsString().indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
            assertTrue(method.getResponseBodyAsString(),method.getResponseBodyAsString().indexOf("<p><tt>Cookie: ") == -1);
            assertTrue(method.getResponseBodyAsString().indexOf("<tt>pathcookie=value</tt><br>") == -1);
        }
    }
    
    
    public void testCookiePolicies() {
        HttpClient client = createHttpClient();


        {
        	client.getState().setCookiePolicy(CookiePolicy.RFC2109);
	        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
	        method.setQueryString("simple=set");
	        
	        try {
	            client.executeMethod(method);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            fail("Unable to execute method : " + t.toString());
	        }
	        assertEquals(200,method.getStatusCode());
	        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
	        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
	        assertEquals(1,client.getState().getCookies().length);
	        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
	        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
	
	        GetMethod method2 = new GetMethod("/" + getWebappContext() + "/cookie/read");
	        
	        try {
	            client.executeMethod(method2);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            fail("Unable to execute method : " + t.toString());
	        }
	        assertEquals(200,method2.getStatusCode());
	        String s = method2.getResponseBodyAsString();
	        assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
	        assertTrue(s, s.indexOf("<p><tt>Cookie: $Version=\"1\"; simplecookie=\"value\"</tt></p>") >= 0);
	        assertTrue(s, s.indexOf("<tt>simplecookie=\"value\"</tt><br>") >= 0);
        }

        {
        	client.getState().setCookiePolicy(CookiePolicy.COMPATIBILITY);
	        GetMethod method = new GetMethod("/" + getWebappContext() + "/cookie/write");
	        method.setQueryString("simple=set");
	        
	        try {
	            client.executeMethod(method);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            fail("Unable to execute method : " + t.toString());
	        }
	        assertEquals(200,method.getStatusCode());
	        assertTrue(method.getResponseBodyAsString().indexOf("<title>WriteCookieServlet: GET</title>") >= 0);
	        assertTrue(method.getResponseBodyAsString().indexOf("Wrote simplecookie.<br>") >= 0);
	        assertEquals(1,client.getState().getCookies().length);
	        assertEquals("simplecookie", ((Cookie)(client.getState().getCookies()[0])).getName());
	        assertEquals("value",((Cookie)(client.getState().getCookies()[0])).getValue());
	
	        GetMethod method2 = new GetMethod("/" + getWebappContext() + "/cookie/read");
	        
	        try {
	            client.executeMethod(method2);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            fail("Unable to execute method : " + t.toString());
	        }
	        assertEquals(200,method2.getStatusCode());
	        String s = method2.getResponseBodyAsString();
	        assertTrue(s, s.indexOf("<title>ReadCookieServlet: GET</title>") >= 0);
	        assertTrue(s, s.indexOf("<p><tt>Cookie: simplecookie=value</tt></p>") >= 0);
	        assertTrue(s, s.indexOf("<tt>simplecookie=value</tt><br>") >= 0);
        }
    }
    
}

