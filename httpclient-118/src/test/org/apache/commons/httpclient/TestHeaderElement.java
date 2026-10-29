/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestHeaderElement.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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
 * Simple tests for {@link HeaderElement}.
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @version $Id: TestHeaderElement.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestHeaderElement extends TestNVP {

    // ------------------------------------------------------------ Constructor
    public TestHeaderElement(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHeaderElement.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHeaderElement.class);
    }

    // ------------------------------------------------------ Protected Methods

    protected NameValuePair makePair() {
        return new HeaderElement();
    }

    protected NameValuePair makePair(String name, String value) {
        return new HeaderElement(name,value);
    }


    // ----------------------------------------------------------- Test Methods

    public void testOldMain() throws Exception {
        // this is derived from the old main method in HeaderElement
        String headerValue = "name1 = value1; name2; name3=\"value3\" , name4=value4; " +
            "name5=value5, name6= ; name7 = value7; name8 = \" value8\"";
        HeaderElement[] elements = HeaderElement.parse(headerValue);
        // there are 3 elements
        assertEquals(3,elements.length);
        // 1st element
        assertEquals("name1",elements[0].getName());
        assertEquals("value1",elements[0].getValue());
        // 1st element has 2 getParameters()
        assertEquals(2,elements[0].getParameters().length);
        assertEquals("name2",elements[0].getParameters()[0].getName());
        assertTrue(null == elements[0].getParameters()[0].getValue());
        assertEquals("name3",elements[0].getParameters()[1].getName());
        assertEquals("value3",elements[0].getParameters()[1].getValue());
        // 2nd element
        assertEquals("name4",elements[1].getName());
        assertEquals("value4",elements[1].getValue());
        // 2nd element has 1 parameter
        assertEquals(1,elements[1].getParameters().length);
        assertEquals("name5",elements[1].getParameters()[0].getName());
        assertEquals("value5",elements[1].getParameters()[0].getValue());
        // 3rd element
        assertEquals("name6",elements[2].getName());
        assertTrue(null == elements[2].getValue());
        // 3rd element has 2 getParameters()
        assertEquals(2,elements[2].getParameters().length);
        assertEquals("name7",elements[2].getParameters()[0].getName());
        assertEquals("value7",elements[2].getParameters()[0].getValue());
        assertEquals("name8",elements[2].getParameters()[1].getName());
        assertEquals(" value8",elements[2].getParameters()[1].getValue());
    }
}
