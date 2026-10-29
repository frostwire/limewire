/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/auth/RFC2617Scheme.java,v 1.3 2004/08/23 17:38:35 sberlin Exp $
 * $Revision: 1.3 $
 * $Date: 2004/08/23 17:38:35 $
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

package org.apache.commons.httpclient.auth;

import java.util.Map;

/**
 * <p>
 * Abstract authentication scheme class that lays foundation for all
 * RFC 2617 compliant authetication schemes and provides capabilities common 
 * to all authentication schemes defined in RFC 2617.
 * </p>
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
*/
public abstract class RFC2617Scheme extends AuthSchemeBase {

    /**
     * Authentication parameter map.
     */
    private Map params = null;

    /**
     * Default constructor for RFC2617 compliant authetication schemes.
     * 
     * @param challenge authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     */
    public RFC2617Scheme(final String challenge) throws MalformedChallengeException {
        super(challenge);
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException(
              "Invalid " + getSchemeName() + " challenge: " + challenge); 
        }
        this.params = AuthChallengeParser.extractParams(challenge);
    }

    /**
     * Returns authentication parameters map. Keys in the map are lower-cased.
     * 
     * @return the map of authentication parameters
     */
    protected Map getParameters() {
        return this.params;
    }

    /**
     * Returns authentication parameter with the given name, if available.
     * 
     * @param name The name of the parameter to be returned
     * 
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null"); 
        }
        return (String) this.params.get(name.toLowerCase());
    }

    /**
     * Returns authentication realm. The realm may not be null.
     * 
     * @return the authentication realm
     */
    public String getRealm() {
        return getParameter("realm");
    }
    
    /**
     * Returns a String identifying the authentication challenge.  This is
     * used, in combination with the host and port to determine if
     * authorization has already been attempted or not.  Schemes which
     * require multiple requests to complete the authentication should
     * return a different value for each stage in the request.
     * 
     * <p>Additionally, the ID should take into account any changes to the
     * authentication challenge and return a different value when appropriate.
     * For example when the realm changes in basic authentication it should be
     * considered a different authentication attempt and a different value should
     * be returned.</p>
     * 
     * <p>This method simply returns the realm for the challenge.</p>
     * 
     * @return String a String identifying the authentication challenge.  The
     * returned value may be null.
     */
    public String getID() {
        return getRealm();
    }
}
