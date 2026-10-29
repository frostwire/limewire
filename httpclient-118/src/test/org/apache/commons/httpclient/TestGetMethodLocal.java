/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestGetMethodLocal.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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
 * Simple tests of {@link GetMethod}.
 *
 * @author Rodney Waldhoff
 * @version $Id: TestGetMethodLocal.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestGetMethodLocal extends TestLocalHostBase {

    // ------------------------------------------------------------ Constructor

    public TestGetMethodLocal(String testName) {
        super(testName);
    }


    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestGetMethodLocal.class);
    }

    // ------------------------------------------------------------------ Tests

    public void testGetSlash() {
        HttpClient client = createHttpClient();

        GetMethod method = new GetMethod("/");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            assertTrue("No data returned.",(data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());
    }

    public void testExecuteMultipleMethods() throws Exception {

        HttpClient client = createHttpClient();

        GetMethod getSlash = new GetMethod("/");
        for(int i=0;i<10;i++) {
            assertEquals(200, client.executeMethod(getSlash));
            String data = getSlash.getResponseBodyAsString();
            assertTrue(null != data);
            assertTrue(data.length() > 0);
            getSlash.recycle();
            getSlash.setPath("/");
        }
    }

    public void testRecycle() {
        HttpClient client = createHttpClient(null);

        GetMethod method = new GetMethod("/");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            assertTrue("No data returned.",(data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());

        method.recycle();
        method.setPath("/");

        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            assertTrue("No data returned.",(data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(200,method.getStatusCode());

    }

    public void test404() {
        HttpClient client = createHttpClient(null);

        GetMethod method = new GetMethod("/i/am/assuming/this/path/and/file/doesnt/exist/on/the/web/server.xyzzy");
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Unable to execute method : " + t.toString());
        }
        assertEquals(404,method.getStatusCode());

    }

    /**
     * The intent of this test is to allow for the incomplete parsing of a GET
     * response, and to make it particularly tricky, the GET response issues
     * a Connection: close".
     *
     * <p>This wants to insure that a recoverable exception is not unexpectedly
     * triggered.</p>
     */
    public void testGetResponseNotReadAutoRecover() {
        HttpClient client = createHttpClient(null);

        try {
            // issue a GET with a connection: close, and don't parse the body.
            String path = "/";
            GetMethod method1 = new GetMethod(path);
            method1.addRequestHeader("Connection", "close");
            client.executeMethod(method1);
            assertEquals(0, method1.getRecoverableExceptionCount() );

            // issue another GET.
            GetMethod method2 = new GetMethod(path);
            client.executeMethod(method2);
            assertEquals(0, method2.getRecoverableExceptionCount() );
        }
        catch (IOException ioe) {

            fail("Problem executing method : " + ioe.toString() );
        }
    }

}
