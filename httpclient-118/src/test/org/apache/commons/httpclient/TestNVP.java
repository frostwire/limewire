/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestNVP.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

/**
 * Simple tests for {@link NameValuePair}.
 *
 * @author Rodney Waldhoff
 * @version $Id: TestNVP.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestNVP extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestNVP(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestNVP.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestNVP.class);
    }

    // ------------------------------------------------------ Protected Methods

    protected NameValuePair makePair() {
        return new NameValuePair();
    }

    protected NameValuePair makePair(String name, String value) {
        return new NameValuePair(name,value);
    }


    // ----------------------------------------------------------- Test Methods

    public void testGet() {
        NameValuePair pair = makePair("name 1","value 1");
        assertEquals("name 1",pair.getName());
        assertEquals("value 1",pair.getValue());
    }

    public void testSet() {
        NameValuePair pair = makePair();
        assertTrue(null == pair.getName());
        assertTrue(null == pair.getValue());
        pair.setName("name");
        assertEquals("name",pair.getName());
        pair.setValue("value");
        assertEquals("value",pair.getValue());
    }

    public void testEqualsAndHashCode() {
        NameValuePair pair1 = makePair();
        NameValuePair pair2 = makePair();

        assertEquals(pair1,pair1);
        assertEquals(pair1.hashCode(),pair1.hashCode());
        assertEquals(pair2,pair2);
        assertEquals(pair2.hashCode(),pair2.hashCode());
        assertEquals(pair1,pair2);
        assertEquals(pair1.hashCode(),pair2.hashCode());
        assertEquals(pair2,pair1);

        pair1.setName("name");
        pair1.setValue("value");

        assertEquals(pair1,pair1);
        assertEquals(pair1.hashCode(),pair1.hashCode());
        assertTrue(!pair1.equals(pair2));
        assertTrue(!pair2.equals(pair1));

        pair2.setName("name");

        assertEquals(pair1,pair1);
        assertEquals(pair1.hashCode(),pair1.hashCode());
        assertEquals(pair2,pair2);
        assertEquals(pair2.hashCode(),pair2.hashCode());
        assertTrue(!pair1.equals(pair2));
        assertTrue(!pair2.equals(pair1));


        pair2.setValue("value");

        assertEquals(pair1,pair1);
        assertEquals(pair1.hashCode(),pair1.hashCode());
        assertEquals(pair2,pair2);
        assertEquals(pair2.hashCode(),pair2.hashCode());
        assertEquals(pair1,pair2);
        assertEquals(pair1.hashCode(),pair2.hashCode());
        assertEquals(pair2,pair1);
    }
}
