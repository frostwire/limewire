/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestStreams.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;


public class TestStreams extends TestCase {

    public TestStreams(String testName) {
        super(testName);
    }

    public void testChunkedInputStream() throws IOException {
        String correctInput = "10;key=\"value\r\nnewline\"\r\n1234567890123456\r\n5\r\n12345\r\n0\r\nFooter1: abcde\r\nFooter2: fghij\r\n";
        String correctResult = "123456789012345612345";
        HttpMethod method = new SimpleHttpMethod();

        //Test for when buffer is larger than chunk size
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(HttpConstants.getBytes(correctInput)), method);
        byte[] buffer = new byte[300];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        String result = HttpConstants.getContentString(out.toByteArray());
        assertEquals(result, correctResult);
        Header footer = method.getResponseFooter("footer1");
        assertEquals(footer.getValue(), "abcde");
        footer = method.getResponseFooter("footer2");
        assertEquals(footer.getValue(), "fghij");

        // recycle the method so that it can be reused below
        method.recycle();

        //Test for when buffer is smaller than chunk size.
        in = new ChunkedInputStream(new ByteArrayInputStream(HttpConstants.getBytes(correctInput)), method);
        buffer = new byte[7];
        out = new ByteArrayOutputStream();
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        result = HttpConstants.getContentString(out.toByteArray());
        assertEquals(result, correctResult);
        footer = method.getResponseFooter("footer1");
        assertEquals(footer.getValue(), "abcde");
        footer = method.getResponseFooter("footer2");
        assertEquals(footer.getValue(), "fghij");
    }

    public void testCorruptChunkedInputStream1() throws IOException {
        //missing \r\n at the end of the first chunk
        String corrupInput = "10;key=\"value\"\r\n123456789012345\r\n5\r\n12345\r\n0\r\nFooter1: abcde\r\nFooter2: fghij\r\n";
        HttpMethod method = new SimpleHttpMethod();

        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(HttpConstants.getBytes(corrupInput)), method);
        byte[] buffer = new byte[300];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        try {
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            fail("Should have thrown exception");
        } catch(IOException e) {
            /* expected exception */
        }
    }

    public void testEmptyChunkedInputStream() throws IOException {
        String input = "0\r\n";
        HttpMethod method = new SimpleHttpMethod();

        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(HttpConstants.getBytes(input)), method);
        byte[] buffer = new byte[300];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        assertEquals(0, out.size());
    }

    public void testContentLengthInputStream() throws IOException {
        String correct = "1234567890123456";
        InputStream in = new ContentLengthInputStream(new ByteArrayInputStream(HttpConstants.getBytes(correct)), 10);
        byte[] buffer = new byte[50];
        int len = in.read(buffer);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(buffer, 0, len);
        String result = HttpConstants.getContentString(out.toByteArray());
        assertEquals(result, "1234567890");
    }

    public void testChunkedConsitance() throws IOException {
        String input = "76126;27823abcd;:q38a-\nkjc\rk%1ad\tkh/asdui\r\njkh+?\\suweb";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream out = new ChunkedOutputStream(buffer);
        out.write(HttpConstants.getBytes(input));
        out.close();
        buffer.close();
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(buffer.toByteArray()), new GetMethod());

        byte[] d = new byte[10];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int len = 0;
        while ((len = in.read(d)) > 0) {
            result.write(d, 0, len);
        }

        String output = HttpConstants.getContentString(result.toByteArray());
        assertEquals(input, output);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestStreams.class);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestStreams.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}

