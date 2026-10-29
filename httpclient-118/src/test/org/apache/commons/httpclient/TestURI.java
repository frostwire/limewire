/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestURI.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
 *
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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Simple tests for the URI class.
 * 
 * @author Michael Becke
 */
public class TestURI extends TestNoHost {

    /**
     * Constructor for TestURI.
     * @param testName
     */
    public TestURI(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(TestURI.class);
    }
    
    public void testIPv4Address() throws URIException {

        URI base = new URI("http://10.0.1.10:8830");
        
        URI uri = base;        
        assertTrue("Should be an IPv4 address", uri.isIPv4address());
           
        uri = new URI(base, "/04-1.html");
        assertTrue("Should be an IPv4 address", uri.isIPv4address());

        uri = new URI("/04-1.html");
        assertFalse("Should NOT be an IPv4 address", uri.isIPv4address());

        uri = new URI(base, "http://10.0.1.10:8830/04-1.html");
        assertTrue("Should be an IPv4 address", uri.isIPv4address());

        uri = new URI("http://10.0.1.10:8830/04-1.html");
        assertTrue("Should be an IPv4 address", uri.isIPv4address());

        uri = new URI(base, "http://host.org/04-1.html");
        assertFalse("Should NOT be an IPv4 address", uri.isIPv4address());

        uri = new URI("http://host.org/04-1.html");
        assertFalse("Should NOT be an IPv4 address", uri.isIPv4address());
        
    }
    
    /**
     * Tests the URI(URI, String) constructor.  This tests URIs ability to
     * resolve relative URIs.
     * 
     * @see URI#URI(URI, String)
     */
    public void testRelativeURIConstructor() {
        
        URI baseURI = null;
        
        try {
            baseURI = new URI( "http://a/b/c/d;p?q" );
        } catch ( URIException e ) {
            fail( "unable to create base URI: " + e );
        }
        
        // the following is an array of arrays in the following order
        // relative URI and resolved( scheme, host, path, query, fragment URI )
        //
        // these examples were taken from rfc 2396
        String[][] testRelativeURIs = {
            { "g:h", "g", null, "h", null, null, "g:h" },
            { "g", "http", "a", "/b/c/g", null, null, "http://a/b/c/g" },
            { "./g", "http", "a", "/b/c/g", null, null, "http://a/b/c/g" },
            { "g/", "http", "a", "/b/c/g/", null, null, "http://a/b/c/g/" },
            { "/g", "http", "a", "/g", null, null, "http://a/g" },
            { "//g", "http", "g", null, null, null, "http://g" },
            { "?y", "http", "a", "/b/c/", "y", null, "http://a/b/c/?y" },
            { "g?y", "http", "a", "/b/c/g", "y", null, "http://a/b/c/g?y" },
            { "#s", "http", "a", "/b/c/d;p", "q", "s", "http://a/b/c/d;p?q#s" },
            { "#", "http", "a", "/b/c/d;p", "q", "", "http://a/b/c/d;p?q#" },
            { "", "http", "a", "/b/c/d;p", "q", null, "http://a/b/c/d;p?q" },
            { "g#s", "http", "a", "/b/c/g", null, "s", "http://a/b/c/g#s" },
            { "g?y#s","http", "a", "/b/c/g", "y", "s", "http://a/b/c/g?y#s" },
            { ";x", "http", "a", "/b/c/;x", null, null, "http://a/b/c/;x" },
            { "g;x", "http", "a", "/b/c/g;x", null, null, "http://a/b/c/g;x" },
            { "g;x?y#s", "http", "a", "/b/c/g;x", "y", "s", "http://a/b/c/g;x?y#s" },
            { ".", "http", "a", "/b/c/", null, null, "http://a/b/c/" },
            { "./", "http", "a", "/b/c/", null, null, "http://a/b/c/" },
            { "..", "http", "a", "/b/", null, null, "http://a/b/" },
            { "../", "http", "a", "/b/", null, null, "http://a/b/" },
            { "../g", "http", "a", "/b/g", null, null, "http://a/b/g" },
            { "../..", "http", "a", "/", null, null, "http://a/" },
            { "../../", "http", "a", "/", null, null, "http://a/" },
            { "../../g", "http", "a", "/g", null, null, "http://a/g" },
            { "../../../g", "http", "a", "/g", null, null, "http://a/g" },
            { "../../../../g", "http", "a", "/g", null, null, "http://a/g" },
            { "/./g", "http", "a", "/g", null, null, "http://a/g" },
            { "/../g", "http", "a", "/g", null, null, "http://a/g" },
            { "g.", "http", "a", "/b/c/g.", null, null, "http://a/b/c/g." },
            { ".g", "http", "a", "/b/c/.g", null, null, "http://a/b/c/.g" },
            { "g..", "http", "a", "/b/c/g..", null, null, "http://a/b/c/g.." },
            { "..g", "http", "a", "/b/c/..g", null, null, "http://a/b/c/..g" },
            { "./../g", "http", "a", "/b/g", null, null, "http://a/b/g" },
            { "./g/.", "http", "a", "/b/c/g/", null, null, "http://a/b/c/g/" },
            { "g/./h", "http", "a", "/b/c/g/h", null, null, "http://a/b/c/g/h" },
            { "g/../h", "http", "a", "/b/c/h", null, null, "http://a/b/c/h" },
            { "g;x=1/./y", "http", "a", "/b/c/g;x=1/y", null, null, "http://a/b/c/g;x=1/y" },
            { "g;x=1/../y", "http", "a", "/b/c/y", null, null, "http://a/b/c/y" },
            { "g?y/./x", "http", "a", "/b/c/g", "y/./x", null, "http://a/b/c/g?y/./x" },
            { "g?y/../x", "http", "a", "/b/c/g", "y/../x", null, "http://a/b/c/g?y/../x" },
            { "g#s/./x", "http", "a", "/b/c/g", null, "s/./x", "http://a/b/c/g#s/./x" },
            { "g#s/../x", "http", "a", "/b/c/g", null, "s/../x", "http://a/b/c/g#s/../x" }
        };
        for (int i = 0; i < testRelativeURIs.length; i++) {
            URI testURI = null;
            
            try {
                testURI = new URI( baseURI, testRelativeURIs[i][0] );
            } catch ( URIException e ) {
                e.printStackTrace();
                fail( 
                    "unable to create URI with relative value(" 
                    + testRelativeURIs[i][0] + "): " + e 
                );   
            }
            
            try {
                assertEquals( testURI.getScheme(), testRelativeURIs[i][1] );
                assertEquals( testURI.getAuthority(), testRelativeURIs[i][2] );
                assertEquals( testURI.getPath(), testRelativeURIs[i][3] );
                assertEquals( testURI.getQuery(), testRelativeURIs[i][4] );
                assertEquals( testURI.getFragment(), testRelativeURIs[i][5] );
                assertEquals( testURI.getURIReference(), testRelativeURIs[i][6] );
            } catch ( URIException e ) {
                fail( "error getting URI property: " + e );
            }            
        }
        
    }

}
