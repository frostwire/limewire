/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/cookie/CookieSpec.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

package org.apache.commons.httpclient.cookie;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.Cookie;

/**
 * Defines the cookie management specification.
 * <p>Cookie management specification must define
 * <ul>
 *   <li> rules of parsing "Set-Cookie" header
 *   <li> rules of validation of parsed cookies
 *   <li>  formatting of "Cookie" header 
 * </ul>
 * for a given host, port and path of origin
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 *
 * @since 2.0
 */
public interface CookieSpec {    

    /** Path delimiter */
    String PATH_DELIM = "/";

    /** Path delimiting charachter */
    char PATH_DELIM_CHAR = PATH_DELIM.charAt(0);

    /**
      * Parse the <tt>"Set-Cookie"</tt> header value into Cookie array.
      *
      * @param host the host which sent the <tt>Set-Cookie</tt> header
      * @param port the port which sent the <tt>Set-Cookie</tt> header
      * @param path the path which sent the <tt>Set-Cookie</tt> header
      * @param secure <tt>true</tt> when the <tt>Set-Cookie</tt> header 
      *  was received over secure conection
      * @param header the <tt>Set-Cookie</tt> received from the server
      * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie value
      * @throws MalformedCookieException if an exception occurs during parsing
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    Cookie[] parse(String host, int port, String path, boolean secure,
      final String header)
      throws MalformedCookieException, IllegalArgumentException;

    /**
      * Parse the <tt>"Set-Cookie"</tt> Header into an array of Cookies.
      *
      * @param host the host which sent the <tt>Set-Cookie</tt> header
      * @param port the port which sent the <tt>Set-Cookie</tt> header
      * @param path the path which sent the <tt>Set-Cookie</tt> header
      * @param secure <tt>true</tt> when the <tt>Set-Cookie</tt> header 
      *  was received over secure conection
      * @param header the <tt>Set-Cookie</tt> received from the server
      * @return an array of <tt>Cookie</tt>s parsed from the header
      * @throws MalformedCookieException if an exception occurs during parsing
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    Cookie[] parse(String host, int port, String path, boolean secure, 
      final Header header)
      throws MalformedCookieException, IllegalArgumentException;

    /**
      * Parse the cookie attribute and update the corresponsing Cookie 
      *  properties.
      *
      * @param attribute cookie attribute from the <tt>Set-Cookie</tt>
      * @param cookie the to be updated
      * @throws MalformedCookieException if an exception occurs during parsing
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    void parseAttribute(final NameValuePair attribute, final Cookie cookie)
      throws MalformedCookieException, IllegalArgumentException;

    /**
      * Validate the cookie according to validation rules defined by the 
      *  cookie specification.
      *
      * @param host the host from which the {@link Cookie} was received
      * @param port the port from which the {@link Cookie} was received
      * @param path the path from which the {@link Cookie} was received
      * @param secure <tt>true</tt> when the {@link Cookie} was received 
      *  using a secure connection
      * @param cookie the Cookie to validate
      * @throws MalformedCookieException if the cookie is invalid
      * @throws IllegalArgumentException if an input parameter is illegal
      */
    void validate(String host, int port, String path, boolean secure, 
      final Cookie cookie) 
      throws MalformedCookieException, IllegalArgumentException;
    
    /**
     * Determines if a Cookie matches a location.
     *
     * @param host the host to which the request is being submitted
     * @param port the port to which the request is being submitted
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using a secure connection
     * @param cookie the Cookie to be matched
     *
     * @return <tt>true</tt> if the cookie should be submitted with a request 
     *  with given attributes, <tt>false</tt> otherwise.
     */
    boolean match(String host, int port, String path, boolean secure,
        final Cookie cookie);

    /**
     * Determines which of an array of Cookies matches a location.
     *
     * @param host the host to which the request is being submitted
     * @param port the port to which the request is being submitted 
     *  (currenlty ignored)
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using a secure protocol
     * @param cookies an array of <tt>Cookie</tt>s to be matched
     *
     * @return <tt>true</tt> if the cookie should be submitted with a request 
     *  with given attributes, <tt>false</tt> otherwise.
     */
    Cookie[] match(String host, int port, String path, boolean secure, 
        final Cookie cookies[]);

    /**
     * Create a <tt>"Cookie"</tt> header value for an array of cookies.
     *
     * @param cookie the cookie to be formatted as string
     * @return a string suitable for sending in a <tt>"Cookie"</tt> header.
     */
    String formatCookie(Cookie cookie);

    /**
     * Create a <tt>"Cookie"</tt> header value for an array of cookies.
     *
     * @param cookies the Cookies to be formatted
     * @return a string suitable for sending in a Cookie header.
     * @throws IllegalArgumentException if an input parameter is illegal
     */
    String formatCookies(Cookie[] cookies) throws IllegalArgumentException;
    
    /**
     * Create a <tt>"Cookie"</tt> Header for an array of Cookies.
     *
     * @param cookies the Cookies format into a Cookie header
     * @return a Header for the given Cookies.
     * @throws IllegalArgumentException if an input parameter is illegal
     */
    Header formatCookieHeader(Cookie[] cookies) throws IllegalArgumentException;

    /**
     * Create a <tt>"Cookie"</tt> Header for single Cookie.
     *
     * @param cookie the Cookie format as a <tt>Cookie</tt> header
     * @return a Cookie header.
     * @throws IllegalArgumentException if an input parameter is illegal
     */
    Header formatCookieHeader(Cookie cookie) throws IllegalArgumentException;

}
