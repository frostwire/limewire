/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/cookie/RFC2109Spec.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.Cookie;

/**
 * <p>RFC 2109 specific cookie management functions
 *
 * @author  B.C. Holmes
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:dsale@us.britannica.com">Doug Sale</a>
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author Sean C. Sullivan
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @since 2.0 
 */

public class RFC2109Spec extends CookieSpecBase {

    /** Default constructor */    
    public RFC2109Spec() {
        super();
    }


    /**
      * Parse RFC 2109 specific cookie attribute and update the corresponsing
      * {@link Cookie} properties.
      *
      * @param attribute {@link NameValuePair} cookie attribute from the
      * <tt>Set- Cookie</tt>
      * @param cookie {@link Cookie} to be updated
      * @throws MalformedCookieException if an exception occurs during parsing
      */
    public void parseAttribute(
        final NameValuePair attribute, final Cookie cookie)
        throws MalformedCookieException {
          
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute may not be null.");
        }
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null.");
        }
        final String paramName = attribute.getName().toLowerCase();
        final String paramValue = attribute.getValue();

        if (paramName.equals("path")) {
            if (paramValue == null) {
                throw new MalformedCookieException(
                    "Missing value for path attribute");
            }
            if (paramValue.trim().equals("")) {
                throw new MalformedCookieException(
                    "Blank value for path attribute");
            }
            cookie.setPath(paramValue);
            cookie.setPathAttributeSpecified(true);
        } else if (paramName.equals("version")) {

            if (paramValue == null) {
                throw new MalformedCookieException(
                    "Missing value for version attribute");
            }
            try {
               cookie.setVersion(Integer.parseInt(paramValue));
            } catch (NumberFormatException e) {
                throw new MalformedCookieException("Invalid version: " 
                    + e.getMessage());
            }

        } else {
            super.parseAttribute(attribute, cookie);
        }
    }

    /**
      * Performs RFC 2109 compliant {@link Cookie} validation
      *
      * @param host the host from which the {@link Cookie} was received
      * @param port the port from which the {@link Cookie} was received
      * @param path the path from which the {@link Cookie} was received
      * @param secure <tt>true</tt> when the {@link Cookie} was received using a
      * secure connection
      * @param cookie The cookie to validate
      * @throws MalformedCookieException if an exception occurs during
      * validation
      */
    public void validate(String host, int port, String path, 
        boolean secure, final Cookie cookie) throws MalformedCookieException {
            
        LOG.trace("enter RFC2109Spec.validate(String, int, String, "
            + "boolean, Cookie)");
            
        // Perform generic validation
        super.validate(host, port, path, secure, cookie);
        // Perform RFC 2109 specific validation
        if (cookie.isDomainAttributeSpecified() 
            && (!cookie.getDomain().equals(host))) {
                
            // domain must start with dot
            if (!cookie.getDomain().startsWith(".")) {
                throw new MalformedCookieException("Domain attribute \"" 
                    + cookie.getDomain() 
                    + "\" violates RFC 2109: domain must start with a dot");
            }
            // domain must have at least one embedded dot
            int dotIndex = cookie.getDomain().indexOf('.', 1);
            if (dotIndex < 0 || dotIndex == cookie.getDomain().length() - 1) {
                throw new MalformedCookieException("Domain attribute \"" 
                    + cookie.getDomain() 
                    + "\" violates RFC 2109: domain must contain an embedded dot");
            }
            host = host.toLowerCase();
            if (host.indexOf('.') >= 0) {
                if (!host.endsWith(cookie.getDomain())) {
                    throw new MalformedCookieException(
                        "Illegal domain attribute \"" + cookie.getDomain() 
                        + "\". Domain of origin: \"" + host + "\"");
                }
                // host minus domain may not contain any dots
                String hostWithoutDomain = host.substring(0, host.length() 
                    - cookie.getDomain().length());
                if (hostWithoutDomain.indexOf('.') != -1) {
                    throw new MalformedCookieException("Domain attribute \"" 
                        + cookie.getDomain() 
                        + "\" violates RFC 2109: host minus domain may not contain any dots");
                }
            }
        }
    }


    /**
     * Return a name/value string suitable for sending in a <tt>"Cookie"</tt>
     * header as defined in RFC 2109 for backward compatibility with cookie
     * version 0
     * @param name The name.
     * @param value The value
     * @param version The cookie version 
     * @return a string suitable for sending in a <tt>"Cookie"</tt> header.
     */

    private String formatNameValuePair(
        final String name, final String value, int version) {
            
        final StringBuffer buffer = new StringBuffer();
        if (version < 1) {
            buffer.append(name);
            buffer.append("=");
            if (value != null) {
                buffer.append(value);   
            }
        } else {
            buffer.append(name);
            buffer.append("=\"");
            if (value != null) {
                buffer.append(value);
            }
            buffer.append("\"");
        }
        return buffer.toString(); 
    }

    /**
     * Return a string suitable for sending in a <tt>"Cookie"</tt> header 
     * as defined in RFC 2109 for backward compatibility with cookie version 0
     * @param cookie a {@link Cookie} to be formatted as string
     * @param version The version to use.
     * @return a string suitable for sending in a <tt>"Cookie"</tt> header.
     */
    private String formatCookieAsVer(Cookie cookie, int version) {
        LOG.trace("enter RFC2109Spec.formatCookieAsVer(Cookie)");
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        StringBuffer buf = new StringBuffer();
        buf.append(formatNameValuePair(cookie.getName(), 
            cookie.getValue(), version));
        if (cookie.getDomain() != null 
            && cookie.isDomainAttributeSpecified()) {
                
            buf.append("; ");
            buf.append(formatNameValuePair("$Domain", 
                cookie.getDomain(), version));
        }
        if (cookie.getPath() != null && cookie.isPathAttributeSpecified()) {
            buf.append("; ");
            buf.append(formatNameValuePair("$Path", cookie.getPath(), version));
        }
        return buf.toString();
    }


    /**
     * Return a string suitable for sending in a <tt>"Cookie"</tt> header as
     * defined in RFC 2109
     * @param cookie a {@link Cookie} to be formatted as string
     * @return a string suitable for sending in a <tt>"Cookie"</tt> header.
     */
    public String formatCookie(Cookie cookie) {
        LOG.trace("enter RFC2109Spec.formatCookie(Cookie)");
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        int ver = cookie.getVersion();
        StringBuffer buffer = new StringBuffer();
        buffer.append(formatNameValuePair("$Version", 
          Integer.toString(ver), ver));
        buffer.append("; ");
        buffer.append(formatCookieAsVer(cookie, ver));
        return buffer.toString();
    }

    /**
     * Create a RFC 2109 compliant <tt>"Cookie"</tt> header value containing all
     * {@link Cookie}s in <i>cookies</i> suitable for sending in a <tt>"Cookie"
     * </tt> header
     * @param cookies an array of {@link Cookie}s to be formatted
     * @return a string suitable for sending in a Cookie header.
     */
    public String formatCookies(Cookie[] cookies) {
        LOG.trace("enter RFC2109Spec.formatCookieHeader(Cookie[])");
        int version = Integer.MAX_VALUE;
        // Pick the lowerest common denominator
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        final StringBuffer buffer = new StringBuffer();
        buffer.append(formatNameValuePair("$Version", 
            Integer.toString(version), version));
        for (int i = 0; i < cookies.length; i++) {
            buffer.append("; ");
            buffer.append(formatCookieAsVer(cookies[i], version));
        }
        return buffer.toString();
    }
}
