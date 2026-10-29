/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/Wire.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:59 $
 *
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logs data to the wire LOG.
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0beta1
 */

class Wire {

    /** Log for any wire messages. */
    private static final Log WIRE_LOG = LogFactory.getLog("httpclient.wire");

    private static void wire(String header, InputStream instream)
      throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(instream, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            reader = new InputStreamReader(instream);
        }
        StringBuffer buffer = new StringBuffer();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == 13) {
                buffer.append("[\\r]");
            } else if (ch == 10) {
                    buffer.append("[\\n]\"");
                    buffer.insert(0, "\"");
                    buffer.insert(0, header);
                    WIRE_LOG.debug(buffer.toString());
                    buffer.setLength(0);
            } else if ((ch < 32) || (ch > 127)) {
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
            } else {
                buffer.append((char) ch);
            }
        } 
        if (buffer.length() > 0) {
            buffer.append("\"");
            buffer.insert(0, "\"");
            buffer.insert(0, header);
            WIRE_LOG.debug(buffer.toString());
        }
    }


    public static final boolean enabled() {
        return WIRE_LOG.isDebugEnabled();
    }    
    
    public static final void output(InputStream outstream)
      throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", outstream);
    }

    public static final void input(InputStream instream)
      throws IOException {
        if (instream == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", instream);
    }

    public static final void output(byte[] b, int off, int len)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", new ByteArrayInputStream(b, off, len));
    }

    public static final void input(byte[] b, int off, int len)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", new ByteArrayInputStream(b, off, len));
    }

    public static final void output(byte[] b)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        wire(">> ", new ByteArrayInputStream(b));
    }

    public static final void input(byte[] b)
      throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        wire("<< ", new ByteArrayInputStream(b));
    }

    public static final void output(int b)
      throws IOException {
        output(new byte[] {(byte) b});
    }

    public static final void input(int b)
      throws IOException {
        input(new byte[] {(byte) b});
    }

    public static final void output(final String s)
      throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Output may not be null"); 
        }
        output(s.getBytes());
    }

    public static final void input(final String s)
      throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Input may not be null"); 
        }
        input(s.getBytes());
    }
}
