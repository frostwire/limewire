/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestHttpUrlMethod.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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
 *
 * Unit tests for {@link org.apache.commons.httpclient.HttpMethod}
 * constructors that take URLs. These tests do not require any network
 * connection or web app.
 *
 * @author Marc A. Saegesser
 * @version $Id: TestHttpUrlMethod.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestHttpUrlMethod extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestHttpUrlMethod(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpUrlMethod.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpUrlMethod.class);
    }


    // ----------------------------------------------------------- Test Methods

    // Test constructors

    public void testUrlGetMethodWithPathQuery() {
        GetMethod method = new GetMethod("http://www.fubar.com/path1/path2?query=string");
        try {
            assertEquals(
                "Get URL",
                "http://www.fubar.com/path1/path2?query=string",
                method.getURI().toString()
            );
        } catch ( URIException e ) {
            fail( "trouble getting URI: " + e );
        }
        assertEquals("Get Path", "/path1/path2", method.getPath());
        assertEquals("Get query string", "query=string", method.getQueryString());
     
    }

    public void testUrlGetMethodWithPath() {
        GetMethod method = new GetMethod("http://www.fubar.com/path1/path2");
        try {
            assertEquals(
                "Get URL",
                "http://www.fubar.com/path1/path2",
                method.getURI().toString()
            );
        } catch ( URIException e ) {
            fail( "trouble getting URI: " + e );
        }
        assertEquals("Get Path", "/path1/path2", method.getPath());
        assertEquals("Get query string", null, method.getQueryString());

    }

    public void testUrlGetMethod() {
        GetMethod method = new GetMethod("http://www.fubar.com/");
        try {
            assertEquals(
                "Get URL",
                "http://www.fubar.com/",
                method.getURI().toString()
            );
        } catch ( URIException e ) {
            fail( "trouble getting URI: " + e );
        }
        assertEquals("Get Path", "/", method.getPath());
        assertEquals("Get query string", null, method.getQueryString());

    }
    

    public void testUrlGetMethodWithInvalidProtocol() {
        try
        {
            GetMethod method = new GetMethod("crap://www.fubar.com/");
            fail("The use of invalid protocol must have resulted in an IllegalStateException");
        }
        catch(IllegalStateException e)
        {
            //expected exception
        }
    }
}
