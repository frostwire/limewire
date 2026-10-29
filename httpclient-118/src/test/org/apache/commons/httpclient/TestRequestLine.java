/*
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


import org.apache.commons.httpclient.protocol.Protocol; 
import junit.framework.*;

/**
 * Simple tests for {@link StatusLine}.
 *
 * @author <a href="mailto:oleg@ural.ru">oleg Kalnichevski</a>
 * @version $Id: TestRequestLine.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestRequestLine extends TestCase {

    private StatusLine statusLine = null;

    // ------------------------------------------------------------ Constructor
    public TestRequestLine(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestRequestLine.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestRequestLine.class);
    }

    // ------------------------------------------------------ Protected Methods


    // ----------------------------------------------------------- Test Methods

    public void testRequestLineGeneral() throws Exception {
        SimpleHttpConnection conn = null;
        SimpleHttpMethod method = null;
                
        conn = new SimpleHttpConnection(null, -1, "localhost", null, 80, Protocol.getProtocol("http"));

        method = new SimpleHttpMethod();
        assertEquals("Simple / HTTP/1.1\r\n", method.getTestRequestLine(conn));

        method = new SimpleHttpMethod("stuff");
        assertEquals("Simple stuff HTTP/1.1\r\n", method.getTestRequestLine(conn));

        conn = new SimpleHttpConnection("proxy", 8080, "localhost", null, 80, Protocol.getProtocol("http"));

        method = new SimpleHttpMethod();
        assertEquals("Simple http://localhost/ HTTP/1.1\r\n", method.getTestRequestLine(conn));

        method = new SimpleHttpMethod("stuff");
        assertEquals("Simple http://localhost/stuff HTTP/1.1\r\n", method.getTestRequestLine(conn));

        conn = new SimpleHttpConnection("proxy", 8080, "localhost", null, -1, Protocol.getProtocol("http"));

        method = new SimpleHttpMethod();
        assertEquals("Simple http://localhost/ HTTP/1.1\r\n", method.getTestRequestLine(conn));

        method = new SimpleHttpMethod("stuff");
        assertEquals("Simple http://localhost/stuff HTTP/1.1\r\n", method.getTestRequestLine(conn));

        conn = new SimpleHttpConnection("proxy", 8080, "localhost", null, 666, Protocol.getProtocol("http"));

        method = new SimpleHttpMethod();
        assertEquals("Simple http://localhost:666/ HTTP/1.1\r\n", method.getTestRequestLine(conn));

        method = new SimpleHttpMethod("stuff");
        assertEquals("Simple http://localhost:666/stuff HTTP/1.1\r\n", method.getTestRequestLine(conn));
    }

    public void testRequestLineQuery() throws Exception {
        SimpleHttpConnection conn = null;
        SimpleHttpMethod method = null;
                
        conn = new SimpleHttpConnection(null, -1, "localhost", null, 80, Protocol.getProtocol("http"));

        method = new SimpleHttpMethod();
        method.setQueryString( new NameValuePair[] {
            new NameValuePair("param1", " !#$%&\'()*+,-./:;<=>?@[\\]^_`{|}~"),
            new NameValuePair("param2", "some stuff")
          } );
        assertEquals("Simple /?param1=+%21%23%24%25%26%27%28%29*%2B%2C-.%2F%3A%3B%3C%3D%3E%3F%40%5B%5C%5D%5E_%60%7B%7C%7D%7E&param2=some+stuff HTTP/1.1\r\n", 
          method.getTestRequestLine(conn));
    }

    public void testRequestLinePath() throws Exception {
        SimpleHttpConnection conn = null;
        SimpleHttpMethod method = null;
                
        conn = new SimpleHttpConnection(null, -1, "localhost", null, 80, Protocol.getProtocol("http"));

        method = new SimpleHttpMethod();
        method.setPath("/some%20stuff/");
        assertEquals("Simple /some%20stuff/ HTTP/1.1\r\n", 
          method.getTestRequestLine(conn));

        method = new SimpleHttpMethod("/some%20stuff/");
        assertEquals("Simple /some%20stuff/ HTTP/1.1\r\n", 
          method.getTestRequestLine(conn));
    }
}
