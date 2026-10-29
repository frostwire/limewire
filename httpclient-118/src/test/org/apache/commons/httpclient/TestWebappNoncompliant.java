/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
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
 * Tests cases intended to test if entity enclosing methods
 * can deal with non-compliant HTTP servers or proxies
 * 
 * @author Oleg Kalnichevski
 * @author Jeff Dever
 */

public class TestWebappNoncompliant extends TestWebappBase
{
    public TestWebappNoncompliant(String s)
    {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestWebappNoncompliant.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestWebappNoncompliant.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    /**
     * Tests if client is able able to recover gracefully when 
     * HTTP server or proxy fails to send 100 status code when
     * expected. The client should resume sending the request body 
     * after a defined timeout without having received "continue"
     * code.
     */
    public void testNoncompliantPostMethodString()
    {
        HttpClient client = createHttpClient();
        NoncompliantPostMethod method = new NoncompliantPostMethod("/" + getWebappContext() + "/body");
        method.setUseExpectHeader(true);
        method.setRequestBody("This is data to be sent in the body of an HTTP POST.");
        try {
            client.executeMethod(method);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
        assertEquals(200,method.getStatusCode());
    }

    /**
     */
    public void testNoncompliantStatusLine()
    {
        HttpClient client = createHttpClient();
        GetMethod method = new GetMethod("/" + getWebappContext() + "/statusline");
        method.setRequestHeader("Set-StatusCode", 444+"");
        method.setRequestHeader("Set-StatusMessage", "This status message contains\n"
                + " a newline and a\r"
                + " carrage return but that should be OK.");
        try {
            client.executeMethod(method);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
        assertEquals(444, method.getStatusCode());
    }


    /** 
     * Test if a response to HEAD method from non-compliant server
     * that contains an unexpected body content can be correctly redirected 
     */ 

    public void testNoncompliantHeadWithResponseBody() 
      throws Exception {
          HttpClient client = createHttpClient();
          HeadMethod method = new NoncompliantHeadMethod("/" + getWebappContext() + "/redirect");
          method.setBodyCheckTimeout(50);
          client.executeMethod(method);
          assertEquals(200,method.getStatusCode());
          method.releaseConnection();
    }

    /** 
     * Test if a response to HEAD method from non-compliant server
     * causes an HttpException to be thrown 
     */ 

    public void testNoncompliantHeadStrictMode() 
      throws Exception {
          HttpClient client = createHttpClient();
          client.setStrictMode(true);
          HeadMethod method = new NoncompliantHeadMethod("/" + getWebappContext() + "/body");
          method.setBodyCheckTimeout(50);
          try {
              client.executeMethod(method);
              fail("HttpException should have been thrown"); 
          } catch(HttpException e) {
              // Expected
          }
          method.releaseConnection();
    }

}
