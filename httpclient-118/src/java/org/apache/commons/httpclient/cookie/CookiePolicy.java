/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/cookie/CookiePolicy.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cookie management policy class. The cookie policy provides corresponding
 * cookie management interfrace for a given type or version of cookie. 
 * <p>RFC 2109 specification is used per default. Other supported specification
 * can be  chosen when appropriate or set default when desired
 * <p>The following specifications are provided:
 *  <ul>
 *   <li><tt>COMPATIBILITY</tt>: compatible with the common cookie management
 *   practices *  (even if they are not 100% standards compliant)
 *   <li><tt>NETSCAPE_DRAFT</tt>: Netscape cookie draft compliant
 *   <li><tt>RFC2109</tt>: RFC2109 compliant (default)
 *  </ul>
 * <p>Default policy can be set on JVM start-up through the system property 
 *  <tt>"apache.commons.httpclient.cookiespec"</tt>. Recognized values: 
 *  <tt>COMPATIBILITY</tt>, <tt>NETSCAPE_DRAFT</tt>, <tt>RFC2109</tt>.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 2.0
 */
public abstract class CookiePolicy {

    /** cookiespec system property. */
    private static final String SYSTEM_PROPERTY =
        "apache.commons.httpclient.cookiespec";

    /**
     * The <tt>COMPATIBILITY</tt> policy provides high compatibilty 
     * with common cookie management of popular HTTP agents.
     */
    public static final int COMPATIBILITY = 0;

    /** The <tt>NETSCAPE_DRAFT</tt> Netscape draft compliant policy. */
    public static final int NETSCAPE_DRAFT = 1;

    /** The <tt>RFC2109</tt> RFC 2109 compliant policy. */
    public static final int RFC2109 = 2;

    /** The default cookie policy. */
    private static int defaultPolicy = RFC2109;

    /** Log object. */
    protected static final Log LOG = LogFactory.getLog(CookiePolicy.class);

    static {
        String s = null;
        try {
            s = System.getProperty(SYSTEM_PROPERTY);
        } catch (SecurityException e) {
        }
        if ("COMPATIBILITY".equalsIgnoreCase(s)) {
            setDefaultPolicy(COMPATIBILITY);
        } else if ("NETSCAPE_DRAFT".equalsIgnoreCase(s)) {
            setDefaultPolicy(NETSCAPE_DRAFT);
        } else if ("RFC2109".equalsIgnoreCase(s)) {
            setDefaultPolicy(RFC2109);
        } else {
            if (s != null) {
                LOG.warn("Unrecognized cookiespec property '" + s
                    + "' - using default");
            }
            setDefaultPolicy(defaultPolicy);
        }
    }

    /**
     * @return default cookie policy
     *  <tt>(COMPATIBILITY | NETSCAPE_DRAFT | RFC2109)</tt>
     */
    public static int getDefaultPolicy() {
        return defaultPolicy;
    }
    

    /**
     * @param policy new default cookie policy
     *  <tt>(COMPATIBILITY | NETSCAPE_DRAFT | RFC2109)</tt>
     */
    public static void setDefaultPolicy(int policy) {
        defaultPolicy = policy;
    }
    

    /**
     * @param policy cookie policy to get the CookieSpec for
     * @return cookie specification interface for the given policy
     *  <tt>(COMPATIBILITY | NETSCAPE_DRAFT | RFC2109)</tt>
     */
    public static CookieSpec getSpecByPolicy(int policy) {
        switch(policy) {
            case COMPATIBILITY: 
                return new CookieSpecBase(); 
            case NETSCAPE_DRAFT: 
                return new NetscapeDraftSpec(); 
            case RFC2109:
                return new RFC2109Spec();
            default:
                return getSpecByPolicy(defaultPolicy); 
        }
    }


    /**
     * @return default cookie specification interface
     */
    public static CookieSpec getDefaultSpec() {
        return getSpecByPolicy(defaultPolicy);
    }
    

    /**
     * Gets the CookieSpec for a particular cookie version.
     * 
     * <p>Supported versions:
     * <ul>
     *  <li><tt>version 0</tt> corresponds to the NETSCAPE_DRAFT
     *  <li><tt>version 1</tt> corresponds to the RFC2109
     *  <li>Any other cookie value coresponds to the default spec
     * <ul>
     *
     * @param ver the cookie version to get the spec for
     * @return cookie specification interface intended for processing 
     *  cookies with the given version 
     */
    public static CookieSpec getSpecByVersion(int ver) {
        switch(ver) {
            case 0: 
                return new NetscapeDraftSpec(); 
            case 1:
                return new RFC2109Spec();
            default:
                return getDefaultSpec(); 
        }
    }

    /**
     * @return cookie specification interface that provides high compatibilty 
     * with common cookie management of popular HTTP agents
     */
    public static CookieSpec getCompatibilitySpec() {
        return getSpecByPolicy(COMPATIBILITY);
    }
}
