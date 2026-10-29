/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/util/EncodingUtil.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
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
package org.apache.commons.httpclient.util;

import java.util.BitSet;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The home for utility methods that handle various encoding tasks.
 * 
 * @author Michael Becke
 * 
 * @since 2.0 final
 */
public class EncodingUtil {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(EncodingUtil.class);

    /**
     * BitSet of www-form-url safe characters.
     */
    private static final BitSet WWW_FORM_URL = new BitSet(256);

    // Static initializer for www_form_url
    static {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            WWW_FORM_URL.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            WWW_FORM_URL.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            WWW_FORM_URL.set(i);
        }
        // blank to be replaced with +
        WWW_FORM_URL.set(' ');
        WWW_FORM_URL.set('-');
        WWW_FORM_URL.set('_');
        WWW_FORM_URL.set('.');
        WWW_FORM_URL.set('*');
    }
    
    /**
     * Form-urlencoding routine.
     *
     * The default encoding for all forms is `application/x-www-form-urlencoded'. 
     * A form data set is represented in this media type as follows:
     *
     * The form field names and values are escaped: space characters are replaced 
     * by `+', and then reserved characters are escaped as per [URL]; that is, 
     * non-alphanumeric characters are replaced by `%HH', a percent sign and two 
     * hexadecimal digits representing the ASCII code of the character. Line breaks, 
     * as in multi-line text field values, are represented as CR LF pairs, i.e. `%0D%0A'.
     * 
     * @param pairs the values to be encoded
     * @param charset the character set of pairs to be encoded
     * 
     * @return the urlencoded pairs
     * 
     * @since 2.0 final
     */
    public static String formUrlEncode(NameValuePair[] pairs, String charset) {
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i].getName() != null) {
                if (i > 0) {
                    buf.append("&");
                }
                String queryName = pairs[i].getName();
                try {
                    queryName = URIUtil.encode(queryName, WWW_FORM_URL, charset).replace(' ', '+');
                } catch (URIException urie) {
                    LOG.error("Error encoding pair name: " + queryName, urie);
                }
                buf.append(queryName);
                buf.append("=");
                if (pairs[i].getValue() != null) {
                    String queryValue = pairs[i].getValue();
                    try {
                        queryValue = URIUtil.encode(
                            queryValue, 
                            WWW_FORM_URL, 
                            charset
                        ).replace(' ', '+');
                    } catch (URIException urie) {
                        LOG.error("Error encoding pair value: " + queryValue, urie);
                    }
                    buf.append(queryValue);
                }
            }
        }
        return buf.toString();
    }
    
    /**
     * This class should not be instantiated.
     */
    private EncodingUtil() {
    }

}
