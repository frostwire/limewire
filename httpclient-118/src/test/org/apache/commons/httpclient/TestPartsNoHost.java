/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestPartsNoHost.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2003/10/30 20:33:00 $
 */
public class TestPartsNoHost extends TestCase {

    static final String PART_DATA = "This is the part data.";
    static final String NAME = "name";

    // ------------------------------------------------------------ Constructor

    public TestPartsNoHost(String testName) {
        super(testName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestPartsNoHost.class);
    }

    // ----------------------------------------------------------------- Tests

    public void testFilePartResendsFileData() throws Exception {
        File file = createTempTestFile();
        FilePart part = new FilePart(NAME, file);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp1 = stream.toString();
        stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp2 = stream.toString();
        file.delete();
        assertEquals(resp1, resp2);
    }

    public void testStringPartResendsData() throws Exception {
        StringPart part = new StringPart(NAME, PART_DATA);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp1 = stream.toString();
        stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp2 = stream.toString();
        assertEquals(resp1, resp2);
    }

    public void testFilePartNullFileResendsData() throws Exception {
        FilePart part = new FilePart(NAME, "emptyfile.ext", null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp1 = stream.toString();
        stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp2 = stream.toString();
        assertEquals(resp1, resp2);
    }


    /** Writes PART_DATA out to a temporary file and returns the file it
     * was written to.
     * @return the File object representing the file the data was
     * written to.
     */
    private File createTempTestFile() throws IOException {
        File file = File.createTempFile("FilePartTest", ".txt");
        PrintWriter out = new PrintWriter(new FileWriter(file));
        out.println(PART_DATA);
        out.flush();
        out.close();
        return file;
    }
}
