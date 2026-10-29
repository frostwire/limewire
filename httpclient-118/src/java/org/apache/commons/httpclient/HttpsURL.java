/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/HttpsURL.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:59 $
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

package org.apache.commons.httpclient;

/**
 * The HTTPS URL.
 *
 * @author <a href="mailto:jericho at apache.org">Sung-Gu</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public class HttpsURL extends HttpURL {

    // ----------------------------------------------------------- Constructors

    /**
     * Create an instance as an internal use.
     */
    protected HttpsURL() {
    }


    /**
     * Construct a HTTPS URL as an escaped form of a character array with the
     * given charset to do escape encoding.
     *
     * @param escaped the HTTPS URL character sequence
     * @param charset the charset to do escape encoding
     * @throws URIException If {@link #checkValid()} fails
     * @throws NullPointerException if <code>escaped</code> is <code>null</code>
     * @see #getProtocolCharset
     */
    public HttpsURL(char[] escaped, String charset)
        throws URIException, NullPointerException {
        protocolCharset = charset;
        parseUriReference(new String(escaped), true);
        checkValid();
    }


    /**
     * Construct a HTTPS URL as an escaped form of a character array.
     *
     * @param escaped the HTTPS URL character sequence
     * @throws URIException If {@link #checkValid()} fails
     * @throws NullPointerException if <code>escaped</code> is <code>null</code>
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(char[] escaped) throws URIException, NullPointerException {
        parseUriReference(new String(escaped), true);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from a given string with the given charset to do
     * escape encoding.
     *
     * @param original the HTTPS URL string
     * @param charset the charset to do escape encoding
     * @throws URIException If {@link #checkValid()} fails
     * @see #getProtocolCharset
     */
    public HttpsURL(String original, String charset) throws URIException {
        protocolCharset = charset;
        parseUriReference(original, false);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from a given string.
     *
     * @param original the HTTPS URL string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String original) throws URIException {
        parseUriReference(original, false);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String host, int port, String path) throws URIException {
        this(null, host, port, path, null, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @param query the query string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String host, int port, String path, String query)
        throws URIException {

        this(null, host, port, path, query, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param user the user name
     * @param password his or her password
     * @param host the host string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String user, String password, String host)
        throws URIException {

        this((user == null) ? null : user 
            + ((password == null) ? "" : ':' +  password),
                host, -1, null, null, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param user the user name
     * @param password his or her password
     * @param host the host string
     * @param port the port number
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String user, String password, String host, int port)
        throws URIException {

        this((user == null) ? null : user 
            + ((password == null) ? "" : ':' +  password),
                host, port, null, null, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param user the user name
     * @param password his or her password
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String user, String password, String host, int port,
            String path) throws URIException {

        this((user == null) ? null : user 
            + ((password == null) ? "" : ':' +  password),
                host, port, path, null, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param user the user name
     * @param password his or her password
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @param query The query string.
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String user, String password, String host, int port,
            String path, String query) throws URIException {

        this((user == null) ? null : user 
            + ((password == null) ? "" : ':' + password),
                host, port, path, query, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param host the host string
     * @param path the path string
     * @param query the query string
     * @param fragment the fragment string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String host, String path, String query, String fragment)
        throws URIException {

        this(null, host, -1, path, query, fragment);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param userinfo the userinfo string
     * @param host the host string
     * @param path the path string
     * @param query the query string
     * @param fragment the fragment string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String userinfo, String host, String path, String query,
            String fragment) throws URIException {

        this(userinfo, host, -1, path, query, fragment);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String userinfo, String host, int port, String path)
        throws URIException {

        this(userinfo, host, port, path, null, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @param query the query string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String userinfo, String host, int port, String path,
            String query) throws URIException {

        this(userinfo, host, port, path, query, null);
        checkValid();
    }


    /**
     * Construct a HTTPS URL from given components.
     *
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @param query the query string
     * @param fragment the fragment string
     * @throws URIException If {@link #checkValid()} fails
     * @see #getDefaultProtocolCharset
     */
    public HttpsURL(String userinfo, String host, int port, String path,
            String query, String fragment) throws URIException {

        // validate and contruct the URI character sequence
        StringBuffer buff = new StringBuffer();
        if (userinfo != null || host != null || port != -1) {
            _scheme = DEFAULT_SCHEME; // in order to verify the own protocol
            buff.append(_default_scheme);
            buff.append("://");
            if (userinfo != null) {
                buff.append(userinfo);
                buff.append('@');
            }
            if (host != null) {
                buff.append(host);
                if (port != -1 || port != DEFAULT_PORT) {
                    buff.append(':');
                    buff.append(port);
                }
            }
        }
        if (path != null) {  // accept empty path
            if (scheme != null && !path.startsWith("/")) {
                throw new URIException(URIException.PARSING,
                        "abs_path requested");
            }
            buff.append(path);
        }
        if (query != null) {
            buff.append('?');
            buff.append(query);
        }
        if (fragment != null) {
            buff.append('#');
            buff.append(fragment);
        }
        parseUriReference(buff.toString(), false);
        checkValid();
    }


    /**
     * Construct a HTTPS URL with a given relative HTTPS URL string.
     *
     * @param base the base HttpsURL
     * @param relative the relative HTTPS URL string
     * @throws URIException If {@link #checkValid()} fails
     */
    public HttpsURL(HttpsURL base, String relative) throws URIException {
        this(base, new HttpsURL(relative));
    }


    /**
     * Construct a HTTPS URL with a given relative URL.
     *
     * @param base the base HttpsURL
     * @param relative the relative HttpsURL
     * @throws URIException If {@link #checkValid()} fails
     */
    public HttpsURL(HttpsURL base, HttpsURL relative) throws URIException {
        super(base, relative);
        checkValid();
    }

    // -------------------------------------------------------------- Constants

    /**
     * Default scheme for HTTPS URL.
     */
    public static final char[] DEFAULT_SCHEME = { 'h', 't', 't', 'p', 's' };
    
    /**
     * Default scheme for HTTPS URL.
     * @deprecated Use {@link #DEFAULT_SCHEME} instead.  This one doesn't
     * conform to the project naming conventions.
     */
    public static final char[] _default_scheme = DEFAULT_SCHEME;


    /**
     * Default port for HTTPS URL.
     */
    public static final int DEFAULT_PORT = 443;

    /**
     * Default port for HTTPS URL.
     * @deprecated Use {@link #DEFAULT_PORT} instead.  This one doesn't conform
     * to the project naming conventions.
     */
    public static final int _default_port = DEFAULT_PORT;


    /**
     * The serialVersionUID.
     */
    static final long serialVersionUID = 887844277028676648L;

    // ---------------------------------------------------------------- Utility

    /**
     * Verify the valid class use for construction.
     *
     * @throws URIException the wrong scheme use
     */
    protected void checkValid() throws URIException {
        // could be explicit protocol or undefined.
        if (!(equals(_scheme, DEFAULT_SCHEME) || _scheme == null)) {
            throw new URIException(URIException.PARSING, "wrong class use");
        }
    }

}

