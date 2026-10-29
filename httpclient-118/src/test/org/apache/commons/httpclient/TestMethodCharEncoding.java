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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:ajmas@bigfoot.com">Andre John Mas</a>
 * @author <a href="mailto:laura@lwerner.org">Laura Werner</a>
 */

public class TestMethodCharEncoding extends TestCase {

    static final String CHARSET_DEFAULT = HttpConstants.DEFAULT_CONTENT_CHARSET;
    static final String CHARSET_ASCII = "US-ASCII";
    static final String CHARSET_UTF8 = "UTF-8";
    static final String CHARSET_KOI8_R = "KOI8_R";
    static final String CHARSET_WIN1251 = "Cp1251";

    static final int SWISS_GERMAN_STUFF_UNICODE [] = {
        0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4
    };
    
    static final int SWISS_GERMAN_STUFF_ISO8859_1 [] = {
        0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4
    };
    
    static final int SWISS_GERMAN_STUFF_UTF8 [] = {
        0x47, 0x72, 0xC3, 0xBC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xC3, 0xA4,
        0x6D, 0xC3, 0xA4
    };

    static final int RUSSIAN_STUFF_UNICODE [] = {
        0x412, 0x441, 0x435, 0x43C, 0x5F, 0x43F, 0x440, 0x438, 
        0x432, 0x435, 0x442 
    }; 

    static final int RUSSIAN_STUFF_UTF8 [] = {
        0xD0, 0x92, 0xD1, 0x81, 0xD0, 0xB5, 0xD0, 0xBC, 0x5F, 
        0xD0, 0xBF, 0xD1, 0x80, 0xD0, 0xB8, 0xD0, 0xB2, 0xD0, 
        0xB5, 0xD1, 0x82
    };

    static final int RUSSIAN_STUFF_KOI8R [] = {
        0xF7, 0xD3, 0xC5, 0xCD, 0x5F, 0xD0, 0xD2, 0xC9, 0xD7, 
        0xC5, 0xD4
    };

    static final int RUSSIAN_STUFF_WIN1251 [] = {
        0xC2, 0xF1, 0xE5, 0xEC, 0x5F, 0xEF, 0xF0, 0xE8, 0xE2, 
        0xE5, 0xF2
    };

    // ------------------------------------------------------------ Constructor

    public TestMethodCharEncoding(String testName) {
        super(testName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestMethodCharEncoding.class);
    }

    // ----------------------------------------------------------------- Tests


    public void testRequestCharEncoding() throws IOException {
        
        GetMethod httpget = new GetMethod("/");
        assertEquals(CHARSET_DEFAULT, httpget.getRequestCharSet());
        httpget.setRequestHeader("Content-Type", "text/plain; charset=" + CHARSET_ASCII); 
        assertEquals(CHARSET_ASCII, httpget.getRequestCharSet());
        httpget.setRequestHeader("Content-Type", "text/plain; charset=" + CHARSET_UTF8); 
        assertEquals(CHARSET_UTF8, httpget.getRequestCharSet());
        
    }

    public void testResponseCharEncoding() throws IOException {
        
        SimpleHttpConnection conn = new SimpleHttpConnection();
        String body = "stuff";
        String headers1 = "HTTP/1.1 200 OK\r\n"
                       +"Content-Length: 4\r\n";
        conn.addResponse(headers1, body);
        conn.open();
        GetMethod httpget = new GetMethod("/");
        httpget.execute(new HttpState(), conn);
        assertEquals(CHARSET_DEFAULT, httpget.getResponseCharSet());
        conn.close();
        httpget.recycle();
        
        String headers2 = "HTTP/1.1 200 OK\r\n"
                       +"Content-Type: text/plain\r\n"
                       +"Content-Length: 4\r\n";
        conn.addResponse(headers2, body);
        conn.open();
        httpget.setPath("/");
        httpget.execute(new HttpState(), conn);
        assertEquals(CHARSET_DEFAULT, httpget.getResponseCharSet());
        conn.close();
        httpget.recycle();

        String headers3 = "HTTP/1.1 200 OK\r\n"
                       +"Content-Type: text/plain; charset=" + CHARSET_UTF8 + "\r\n"
                       +"Content-Length: 4\r\n";
        conn.addResponse(headers3, body);
        conn.open();
        httpget.setPath("/");
        httpget.execute(new HttpState(), conn);
        assertEquals(CHARSET_UTF8, httpget.getResponseCharSet());
        conn.close();
        httpget.recycle();

    }


    private String constructString(int [] unicodeChars) {
        StringBuffer buffer = new StringBuffer();
        if (unicodeChars != null) {
            for (int i = 0; i < unicodeChars.length; i++) {
                buffer.append((char)unicodeChars[i]); 
            }
        }
        return buffer.toString();
    }


    private void verifyEncoding(final InputStream instream, final int[] sample)
     throws IOException  {
        assertNotNull("Request body", instream);
        
        for (int i = 0; i < sample.length; i++) {
            int b = instream.read();
            assertTrue("Unexpected end of stream", b != -1);
            if (sample[i] != b) {
                fail("Invalid request body encoding");
            }
        }
        assertTrue("End of stream expected", instream.read() == -1);
    }
    
    
    public void testLatinAccentInRequestBody() throws IOException {

        PostMethod httppost = new PostMethod("/");
        httppost.setRequestBody(constructString(SWISS_GERMAN_STUFF_UNICODE));
        // Test default encoding ISO-8859-1
        verifyEncoding(httppost.getRequestBody(), SWISS_GERMAN_STUFF_ISO8859_1);
        // Test UTF-8 encoding
        httppost.setRequestHeader("Content-Type", "text/plain; charset=" + CHARSET_UTF8);
        verifyEncoding(httppost.getRequestBody(), SWISS_GERMAN_STUFF_UTF8);

    }
    
    public void testRussianInRequestBody() throws IOException {

        PostMethod httppost = new PostMethod("/");
        httppost.setRequestBody(constructString(RUSSIAN_STUFF_UNICODE));

        // Test UTF-8 encoding
        httppost.setRequestHeader("Content-Type", "text/plain; charset=" + CHARSET_UTF8);
        verifyEncoding(httppost.getRequestBody(), RUSSIAN_STUFF_UTF8);
        // Test KOI8-R
        httppost.setRequestHeader("Content-Type", "text/plain; charset=" + CHARSET_KOI8_R);
        verifyEncoding(httppost.getRequestBody(), RUSSIAN_STUFF_KOI8R);
        // Test WIN1251
        httppost.setRequestHeader("Content-Type", "text/plain; charset=" + CHARSET_WIN1251);
        verifyEncoding(httppost.getRequestBody(), RUSSIAN_STUFF_WIN1251);

    }

    public void testQueryParams() throws IOException {

        GetMethod get = new GetMethod("/");

        String ru_msg = constructString(RUSSIAN_STUFF_UNICODE); 
        String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE); 

        get.setQueryString(new NameValuePair[] {
            new NameValuePair("ru", ru_msg),
            new NameValuePair("ch", ch_msg) 
        });            

        Map params = new HashMap();
        StringTokenizer tokenizer = new StringTokenizer(
            get.getQueryString(), "&");
        while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            int i = s.indexOf('=');
            assertTrue("Invalid url-encoded parameters", i != -1);
            String name = s.substring(0, i).trim(); 
            String value = s.substring(i + 1, s.length()).trim(); 
            value = URIUtil.decode(value, CHARSET_UTF8);
            params.put(name, value);
        }
        assertEquals(ru_msg, params.get("ru"));
        assertEquals(ch_msg, params.get("ch"));
    }

    public void testUrlEncodedRequestBody() throws IOException {

        PostMethod httppost = new PostMethod("/");

        String ru_msg = constructString(RUSSIAN_STUFF_UNICODE); 
        String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE); 

        httppost.setRequestBody(new NameValuePair[] {
            new NameValuePair("ru", ru_msg),
            new NameValuePair("ch", ch_msg) 
        });            

        httppost.setRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE 
            + "; charset=" + CHARSET_UTF8);

        Map params = new HashMap();
        StringTokenizer tokenizer = new StringTokenizer(
        httppost.getRequestBodyAsString(), "&");
        while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            int i = s.indexOf('=');
            assertTrue("Invalid url-encoded parameters", i != -1);
            String name = s.substring(0, i).trim(); 
            String value = s.substring(i + 1, s.length()).trim(); 
            value = URIUtil.decode(value, CHARSET_UTF8);
            params.put(name, value);
        }
        assertEquals(ru_msg, params.get("ru"));
        assertEquals(ch_msg, params.get("ch"));
    }
    
}
