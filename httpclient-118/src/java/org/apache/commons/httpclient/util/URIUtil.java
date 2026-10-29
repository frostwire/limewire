/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/util/URIUtil.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
 *
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

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

/**
 * The URI escape and character encoding and decoding utility.
 * It's compatible with {@link org.apache.commons.httpclient.HttpURL} rather
 * than {@link org.apache.commons.httpclient.URI}.
 *
 * @author <a href="mailto:jericho@apache.org">Sung-Gu</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2002/03/14 15:14:01 
 */

public class URIUtil {

    // ----------------------------------------------------- Instance variables

    protected static final BitSet empty = new BitSet(1);

    // ---------------------------------------------------------- URI utilities

    /**
     * Get the basename of an URI.   It's possibly an empty string.
     *
     * @param uri a string regarded an URI
     * @return the basename string; an empty string if the path ends with slash
     */
    public static String getName(String uri) {
        if (uri == null || uri.length() == 0) { return uri; } 
        String path = URIUtil.getPath(uri);
        int at = path.lastIndexOf("/");
        int to = path.length();
        return (at >= 0) ? path.substring(at + 1, to) : path;
    }


    /**
     * Get the query of an URI.
     *
     * @param uri a string regarded an URI
     * @return the query string; <code>null</code> if empty or undefined
     */
    public static String getQuery(String uri) {
        if (uri == null || uri.length() == 0) { return null; } 
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/", 
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // the authority part of URI ignored
        int to = uri.length();
        // reuse the at and from variables to consider the query
        at = uri.indexOf("?", from);
        if (at >= 0) {
            from = at + 1;
        } else {
            return null;
        }
        // check the fragment
        if (uri.lastIndexOf("#") > from) {
            to = uri.lastIndexOf("#");
        }
        // get the path and query.
        return (from < 0 || from == to) ? null : uri.substring(from, to);
    }


    /**
     * Get the path of an URI.
     *
     * @param uri a string regarded an URI
     * @return the path string
     */
    public static String getPath(String uri) {
        if (uri == null) {
            return null;
        } 
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/", 
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // the authority part of URI ignored 
        int to = uri.length();
        // check the query
        if (uri.indexOf('?', from) != -1) {
            to = uri.indexOf('?', from);
        }
        // check the fragment
        if (uri.lastIndexOf("#") > from && uri.lastIndexOf("#") < to) {
            to = uri.lastIndexOf("#");
        }
        // get only the path.
        return (from < 0) ? (at >= 0 ? "/" : uri) : uri.substring(from, to);
    }


    /**
     * Get the path and query of an URI.
     *
     * @param uri a string regarded an URI
     * @return the path and query string
     */
    public static String getPathQuery(String uri) {
        if (uri == null) {
            return null;
        } 
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/", 
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // the authority part of URI ignored
        int to = uri.length();
        // Ignore the '?' mark so to ignore the query.
        // check the fragment
        if (uri.lastIndexOf("#") > from) {
            to = uri.lastIndexOf("#");
        }
        // get the path and query.
        return (from < 0) ? (at >= 0 ? "/" : uri) : uri.substring(from, to);
    }


    /**
     * Get the path of an URI and its rest part.
     *
     * @param uri a string regarded an URI
     * @return the string from the path part
     */
    public static String getFromPath(String uri) {
        if (uri == null) {
            return null;
        } 
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/", 
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // get the path and its rest.
        return (from < 0) ? (at >= 0 ? "/" : uri) : uri.substring(from);
    }

    // ----------------------------------------------------- Encoding utilities

    /**
     * Get the all escaped and encoded string with the default protocl charset.
     * It's the same function to use <code>encode(String unescaped, Bitset
     * empty, URI.getDefaultProtocolCharset())</code>.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeAll(String unescaped) throws URIException {
        return encodeAll(unescaped, URI.getDefaultProtocolCharset());
    }
 

    /**
     * Get the all escaped and encoded string with a given charset.
     * It's the same function to use <code>encode(String unescaped, Bitset
     * empty, String charset)</code>.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodeAll(String unescaped, String charset)
        throws URIException {

        return encode(unescaped, empty, charset);
    }
  

    /**
     * Escape and encode a string regarded as within the authority component of
     * an URI with the default protocol charset.
     * Within the authority component, the characters ";", ":", "@", "?", and
     * "/" are reserved.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeWithinAuthority(String unescaped)
        throws URIException {

        return encodeWithinAuthority(unescaped, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as within the authority component of
     * an URI with a given charset.
     * Within the authority component, the characters ";", ":", "@", "?", and
     * "/" are reserved.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodeWithinAuthority(String unescaped, String charset)
        throws URIException {

        return encode(unescaped, URI.allowed_within_authority, charset);
    }


    /**
     * Escape and encode a string regarded as the path and query components of
     * an URI with the default protocol charset.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodePathQuery(String unescaped) throws URIException {
        return encodePathQuery(unescaped, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as the path and query components of
     * an URI with a given charset.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodePathQuery(String unescaped, String charset)
        throws URIException {

        int at = unescaped.indexOf('?');
        if (at < 0) {
            return encode(unescaped, URI.allowed_abs_path, charset);
        }
        // else
        return  encode(unescaped.substring(0, at), URI.allowed_abs_path, charset)
            + '?' + encode(unescaped.substring(at + 1), URI.allowed_query, charset);
    }


    /**
     * Escape and encode a string regarded as within the path component of an
     * URI with the default protocol charset.
     * The path may consist of a sequence of path segments separated by a
     * single slash "/" character.  Within a path segment, the characters
     * "/", ";", "=", and "?" are reserved.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeWithinPath(String unescaped)
        throws URIException {

        return encodeWithinPath(unescaped, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as within the path component of an
     * URI with a given charset.
     * The path may consist of a sequence of path segments separated by a
     * single slash "/" character.  Within a path segment, the characters
     * "/", ";", "=", and "?" are reserved.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodeWithinPath(String unescaped, String charset)
        throws URIException {

        return encode(unescaped, URI.allowed_within_path, charset);
    }


    /**
     * Escape and encode a string regarded as the path component of an URI with
     * the default protocol charset.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodePath(String unescaped) throws URIException {
        return encodePath(unescaped, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as the path component of an URI with
     * a given charset.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodePath(String unescaped, String charset)
        throws URIException {

        return encode(unescaped, URI.allowed_abs_path, charset);
    }


    /**
     * Escape and encode a string regarded as within the query component of an
     * URI with the default protocol charset.
     * When a query comprise the name and value pairs, it is used in order
     * to encode each name and value string.  The reserved special characters
     * within a query component are being included in encoding the query.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeWithinQuery(String unescaped)
        throws URIException {

        return encodeWithinQuery(unescaped, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as within the query component of an
     * URI with a given charset.
     * When a query comprise the name and value pairs, it is used in order
     * to encode each name and value string.  The reserved special characters
     * within a query component are being included in encoding the query.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodeWithinQuery(String unescaped, String charset)
        throws URIException {

        return encode(unescaped, URI.allowed_within_query, charset);
    }


    /**
     * Escape and encode a string regarded as the query component of an URI with
     * the default protocol charset.
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, this method
     * is recommended to use in encoding the whole query.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeQuery(String unescaped) throws URIException {
        return encodeQuery(unescaped, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as the query component of an URI with
     * a given charset.
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, this method
     * is recommended to use in encoding the whole query.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see #encode
     */
    public static String encodeQuery(String unescaped, String charset)
        throws URIException {

        return encode(unescaped, URI.allowed_query, charset);
    }


    /**
     * Escape and encode a given string with allowed characters not to be
     * escaped and the default protocol charset.
     *
     * @param unescaped a string
     * @param allowed allowed characters not to be escaped
     * @return the escaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see Coder#encode
     */
    public static String encode(String unescaped, BitSet allowed)
        throws URIException {

        return encode(unescaped, allowed, URI.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a given string with allowed characters not to be
     * escaped and a given charset.
     *
     * @param unescaped a string
     * @param allowed allowed characters not to be escaped
     * @param charset the charset
     * @return the escaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see Coder#encode
     */
    public static String encode(String unescaped, BitSet allowed,
            String charset) throws URIException {

        return new String(Coder.encode(unescaped, allowed, charset));
    }


    /**
     * Unescape and decode a given string regarded as an escaped string with the
     * default protocol charset.
     *
     * @param escaped a string
     * @return the unescaped string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * @see Coder#decode
     */
    public static String decode(String escaped) throws URIException {
        return Coder.decode(escaped.toCharArray(), URI.getDefaultProtocolCharset());
    }


    /**
     * Unescape and decode a given string regarded as an escaped string.
     *
     * @param escaped a string
     * @param charset the charset
     * @return the unescaped string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @see Coder#decode
     */
    public static String decode(String escaped, String charset)
        throws URIException {

        return Coder.decode(escaped.toCharArray(), charset);
    }

    // --------------------------------- transforming a string between charsets

    /**
     * Convert a target string to the specified character encoded string with
     * the default protocol charset.
     *
     * @param target a target string
     * @return the protocol character encoded string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultProtocolCharset
     * 
     * @deprecated Do not use. To be removed
     */
    public static String toProtocolCharset(String target) throws URIException {
        return toUsingCharset(
            target, 
            URI.getDefaultDocumentCharset(), 
            URI.getDefaultProtocolCharset());
    }


    /**
     * Convert a target string to the specified character encoded string with
     * a given protocol charset.
     *
     * @param target a target string
     * @param charset the transformed protocol charset
     * @return the protocol character encoded string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @deprecated Do not use. To be removed
     */
    public static String toProtocolCharset(String target, String charset)
        throws URIException {

        return toUsingCharset(target, URI.getDefaultDocumentCharset(), charset);
    }


    /**
     * Convert a target string to the specified character encoded string with
     * the default document charset.
     *
     * @param target a target string
     * @return the document character encoded string
     * 
     * @throws URIException if the default protocol charset is not supported
     * 
     * @see URI#getDefaultDocumentCharset
     * 
     * @deprecated Do not use. To be removed
     */
    public static String toDocumentCharset(String target) throws URIException {
        return toUsingCharset(target, URI.getDefaultProtocolCharset(),
                URI.getDefaultDocumentCharset());
    }


    /**
     * Convert a target string to the specified character encoded string with
     * a given document charset.
     *
     * @param target a target string
     * @param charset the transformed document charset
     * @return the document character encoded string
     * 
     * @throws URIException if the charset is not supported
     * 
     * @deprecated Do not use. To be removed
     */
    public static String toDocumentCharset(String target, String charset)
        throws URIException {

        return toUsingCharset(target, URI.getDefaultProtocolCharset(), charset);
    }


    /**
     * Convert a target string from the <code>fromCharset</code> charset to
     * the <code>toCharset</code> charset.
     * <p>
     * What if the document charset is ISO-8859-1 and the protocol charset is
     * UTF-8, when it's read from the document part and is used in the protocol
     * part, the use of the method will be <code>toUsingCharset(the string,
     * "ISO-8859-1", "UTF-8")</code>.
     *
     * @param target a target string
     * @param fromCharset the previous charset
     * @param toCharset the changing charset
     * @return the document character encoded string
     * 
     * @throws URIException if either of the charsets are not supported
     * 
     * @deprecated Do not use. To be removed
     */

    public static String toUsingCharset(String target, String fromCharset,
            String toCharset) throws URIException {

        try {
            return new String(target.getBytes(fromCharset), toCharset);
        } catch (UnsupportedEncodingException error) {
            throw new URIException(URIException.UNSUPPORTED_ENCODING,
                    error.getMessage());
        }
    }

    // ---------------------------------------------------------- Inner classes

    /**
     * The basic and internal utility for URI escape and character encoding and
     * decoding.
     */
    protected static class Coder extends URI {

        /**
         * Escape and encode a given string with allowed characters not to be
         * escaped.
         *
         * @param unescapedComponent an unescaped component
         * @param allowed allowed characters not to be escaped
         * @param charset the charset to encode
         * @return the escaped and encoded string
         * 
         * @throws URIException if the charset is not supported
         */
        public static char[] encode(String unescapedComponent, BitSet allowed, String charset) 
            throws URIException {

            return URI.encode(unescapedComponent, allowed, charset);
        }


        /**
         * Unescape and decode a given string.
         *
         * @param escapedComponent an being-unescaped component
         * @param charset the charset to decode
         * @return the escaped and encoded string
         * 
         * @throws URIException if the charset is not supported
         */
        public static String decode(char[] escapedComponent, String charset)
            throws URIException {

            return URI.decode(escapedComponent, charset);
        }


        /**
         * Verify whether a given string is escaped or not
         *
         * @param original given characters
         * @return true if the given character array is 7 bit ASCII-compatible.
         */
        public static boolean verifyEscaped(char[] original) {
            for (int i = 0; i < original.length; i++) {
                int c = original[i];
                if (c > 128) {
                    return false;
                } else if (c == '%') {
                    if (Character.digit(original[++i], 16) == -1 
                        || Character.digit(original[++i], 16) == -1) {
                        return false;
                    }
                }
            }
            return true;
        }


        /**
         * Replace from a given character to given character in an array order
         * for a given string.
         *
         * @param original a given string
         * @param from a replacing character array
         * @param to a replaced character array
         * @return the replaced string
         */
        public static String replace(String original, char[] from, char[] to) {
            for (int i = from.length; i > 0; --i) {
                original = replace(original, from[i], to[i]);
            }
            return original.toString();
        }


        /**
         * Replace from a given character to given character for a given string.
         *
         * @param original a given string
         * @param from a replacing character array
         * @param to a replaced character array
         * @return the replaced string
         */
        public static String replace(String original, char from, char to) {
            StringBuffer result = new StringBuffer(original.length());
            int at, saved = 0;
            do {
                at = original.indexOf(from);
                if (at >= 0) {
                    result.append(original.substring(0, at));
                    result.append(to);
                } else {
                    result.append(original.substring(saved));
                }
                saved = at;
            } while (at >= 0);
            return result.toString();
        }
    }

}

