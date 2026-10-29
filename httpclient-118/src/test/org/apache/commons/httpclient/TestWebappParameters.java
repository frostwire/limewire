/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestWebappParameters.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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
 * @version $Id: TestWebappParameters.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestWebappParameters extends TestWebappBase {

    public TestWebappParameters(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappParameters.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappParameters.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests

    /**
     * Test that {@link GetMethod#setQueryString(java.lang.String)}
     * can include a leading question mark.
     */
    public void testGetMethodQueryString() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString("?hadQuestionMark=true");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"hadQuestionMark=true\"</p>") >= 0);
    }

    /**
     * Test that {@link GetMethod#setQueryString(java.lang.String)}
     * doesn't have to include a leading question mark.
     */
    public void testGetMethodQueryString2() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString("hadQuestionMark=false");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"hadQuestionMark=false\"</p>") >= 0);
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * values get added to the query string.
     */
    public void testGetMethodParameters() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString(new NameValuePair[] { new NameValuePair("param-one","param-value") });
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"param-one=param-value\"</p>") >= 0);
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * works with multiple parameters.
     */
    public void testGetMethodMultiParameters() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString(new NameValuePair[] {
                                new NameValuePair("param-one","param-value"),
                                new NameValuePair("param-two","param-value2"),
                                new NameValuePair("special-chars",":/?~.")
                              });
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"special-chars\";value=\":/?~.\"") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"param-one\";value=\"param-value\"") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"param-two\";value=\"param-value2\"") >= 0);
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * works with a parameter name but no value.
     */
    public void testGetMethodParameterWithoutValue() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString(new NameValuePair[] { new NameValuePair("param-without-value",null) });
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"param-without-value=\"</p>") >= 0);
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * works with a parameter name that occurs more than once.
     */
    public void testGetMethodParameterAppearsTwice() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString(new NameValuePair[] {
                                  new NameValuePair("foo","one"),
                                  new NameValuePair("foo","two")
                             });
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"foo\";value=\"one\"") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"foo\";value=\"two\"") >= 0);
    }

    public void testGetMethodOverwriteQueryString() throws Exception {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/params");
        method.setQueryString("query=string");
        method.setQueryString(new NameValuePair[] {
                                  new NameValuePair("param","eter"),
                                  new NameValuePair("para","meter")
                             });
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: GET</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"query\";value=\"string\"") == -1);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"param\";value=\"eter\"") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"para\";value=\"meter\"") >= 0);
    }

    /**
     * Test that {@link PostMethod#addParameter(java.lang.String,java.lang.String)}
     * and {@link PostMethod#setQueryString(java.lang.String)} combine
     * properly.
     */
    public void testPostMethodParameterAndQueryString() throws Exception {
        HttpClient client = createHttpClient();
        PostMethod method = new PostMethod("/" + getWebappContext() + "/params");
        method.setQueryString("query=string");
        method.setRequestBody(new NameValuePair[] { 
           new NameValuePair("param","eter"),
           new NameValuePair("para","meter") } );
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertTrue(method.getResponseBodyAsString().indexOf("<title>Param Servlet: POST</title>") >= 0);
        assertEquals(200,method.getStatusCode());
        assertTrue(method.getResponseBodyAsString().indexOf("<p>QueryString=\"query=string\"</p>") >= 0);
        assertTrue(method.getResponseBodyAsString(),method.getResponseBodyAsString().indexOf("name=\"param\";value=\"eter\"") >= 0);
        assertTrue(method.getResponseBodyAsString().indexOf("name=\"para\";value=\"meter\"") >= 0);
    }
}

