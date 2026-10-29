/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/auth/HttpAuthRealm.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

package org.apache.commons.httpclient.auth;

/** The key used to look up authentication credentials.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:adrian@intencha.com">Adrian Sutton</a>
 */
public class HttpAuthRealm {
    
    /** The realm the credentials apply to. */
    private String realm = null;
    
    /** The domain the credentials apply to. */
    private String domain = null;
        
    /** Creates a new HttpAuthRealm for the given <tt>domain</tt> and 
     * <tt>realm</tt>.
     * 
     * @param domain the domain the credentials apply to. May be set
     *   to <tt>null</null> if credenticals are applicable to
     *   any domain. 
     * @param realm the realm the credentials apply to. May be set 
     *   to <tt>null</null> if credenticals are applicable to
     *   any realm. 
     *   
     */
    public HttpAuthRealm(final String domain, final String realm) {
        this.domain = domain;
        this.realm = realm;
    }
    
    /** Determines if the given domains match.  Note that <tt>null</tt> acts as a
     * wildcard so if either of the domains are <tt>null</tt>, it is considered a match.
     * 
     * @param d1 the domain
     * @param d2 the other domain
     * @return boolean true if the domains match, otherwise false.
     */
    private static boolean domainAttribMatch(final String d1, final String d2) {
        return d1 == null || d2 == null || d1.equalsIgnoreCase(d2);
    }

    /** Determines if the given realms match.  Note that <tt>null</tt> acts as a
     * wildcard so if either realm is <tt>null</tt>, this function will return <tt>true</tt>.
     * 
     * @param r1 the realm
     * @param r2 the other realm
     * @return boolean true if the realms match, otherwise false.
     */ 
    private static boolean realmAttribMatch(final String r1, final String r2) {
        return r1 == null || r2 == null || r1.equals(r2);
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof HttpAuthRealm)) {
            return super.equals(o);
        }
        HttpAuthRealm that = (HttpAuthRealm) o;
        return 
          domainAttribMatch(this.domain, that.domain) 
          && realmAttribMatch(this.realm, that.realm);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Authentication domain: '");
        buffer.append(this.domain);
        buffer.append("', authentication realm: '");
        buffer.append(this.realm);
        buffer.append("'");
        return buffer.toString();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.domain);
        buffer.append(this.realm);
        return buffer.toString().hashCode();
    }

}
