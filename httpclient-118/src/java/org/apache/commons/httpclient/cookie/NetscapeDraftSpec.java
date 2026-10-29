/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/cookie/NetscapeDraftSpec.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

import java.util.StringTokenizer;
import java.util.Date;
import java.util.Locale;   
import java.text.DateFormat; 
import java.text.SimpleDateFormat;  
import java.text.ParseException; 
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.Cookie;

/**
 * <P>Netscape cookie draft specific cookie management functions
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

public class NetscapeDraftSpec extends CookieSpecBase {

    /** Default constructor */
    public NetscapeDraftSpec() {
        super();
    }


    /**
      * Parse the cookie attribute and update the corresponsing {@link Cookie}
      * properties as defined by the Netscape draft specification
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

        if (paramName.equals("expires")) {

            if (paramValue == null) {
                throw new MalformedCookieException(
                    "Missing value for expires attribute");
            }
            try {
                DateFormat expiryFormat = new SimpleDateFormat(
                    "EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
                Date date = expiryFormat.parse(paramValue);
                cookie.setExpiryDate(date);
            } catch (ParseException e) {
                throw new MalformedCookieException("Invalid expires "
                    + "attribute: " + e.getMessage());
            }
        } else {
            super.parseAttribute(attribute, cookie);
        }
    }

    /**
      * Performs Netscape draft compliant {@link Cookie} validation
      *
      * @param host the host from which the {@link Cookie} was received
      * @param port the port from which the {@link Cookie} was received
      * @param path the path from which the {@link Cookie} was received
      * @param secure <tt>true</tt> when the {@link Cookie} was received 
      * using a secure connection
      * @param cookie The cookie to validate.
      * @throws MalformedCookieException if an exception occurs during
      * validation
      */
    public void validate(String host, int port, String path, 
        boolean secure, final Cookie cookie) 
        throws MalformedCookieException {
            
        LOG.trace("enterNetscapeDraftCookieProcessor "
            + "RCF2109CookieProcessor.validate(Cookie)");
        // Perform generic validation
        super.validate(host, port, path, secure, cookie);
        // Perform Netscape Cookie draft specific validation
        if (host.indexOf(".") >= 0) {
            int domainParts = new StringTokenizer(cookie.getDomain(), ".")
                .countTokens();

            if (isSpecialDomain(cookie.getDomain())) {
                if (domainParts < 2) {
                    throw new MalformedCookieException("Domain attribute \""
                        + cookie.getDomain() 
                        + "\" violates the Netscape cookie specification for "
                        + "special domains");
                }
            } else {
                if (domainParts < 3) {
                    throw new MalformedCookieException("Domain attribute \""
                        + cookie.getDomain() 
                        + "\" violates the Netscape cookie specification");
                }            
            }
        }
    }
    
    /**
     * Checks if the given domain is in one of the seven special
     * top level domains defined by the Netscape cookie specification.
     * @param domain The domain.
     * @return True if the specified domain is "special"
     */
    private static boolean isSpecialDomain(final String domain) {
        final String ucDomain = domain.toUpperCase();
        if (ucDomain.endsWith(".COM") 
           || ucDomain.endsWith(".EDU")
           || ucDomain.endsWith(".NET")
           || ucDomain.endsWith(".GOV")
           || ucDomain.endsWith(".MIL")
           || ucDomain.endsWith(".ORG")
           || ucDomain.endsWith(".INT")) {
            return true;
        }
        return false;
    }
}
