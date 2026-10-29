/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/auth/BasicScheme.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

package org.apache.commons.httpclient.auth;

import org.apache.commons.httpclient.HttpConstants; 
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.util.Base64; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Basic authentication scheme as defined in RFC 2617.
 * </p>
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Glück
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */

public class BasicScheme extends RFC2617Scheme {
    
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(BasicScheme.class);
    
    /**
     * Constructor for the basic authetication scheme.
     * 
     * @param challenge authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     */
    public BasicScheme(final String challenge) throws MalformedChallengeException {
        super(challenge);
    }


    /**
     * Returns textual designation of the basic authentication scheme.
     * 
     * @return <code>basic</code>
     */
    public String getSchemeName() {
        return "basic";
    }

    /**
     * Produces basic authorization string for the given set of 
     * {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method Method name is ignored by the basic authentication scheme
     * @param uri URI is ignored by the basic authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return a basic authorization string
     */
    public String authenticate(Credentials credentials, String method, String uri)
      throws AuthenticationException {

        LOG.trace("enter BasicScheme.authenticate(Credentials, String, String)");

        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new AuthenticationException(
             "Credentials cannot be used for basic authentication: " 
              + credentials.getClass().getName());
        }
        return BasicScheme.authenticate(usernamepassword);
    }

    /**
     * Return a basic <tt>Authorization</tt> header value for the given 
     * {@link UsernamePasswordCredentials}.
     * 
     * @param credentials The credentials to encode.
     * 
     * @return a basic authorization string
     */
    public static String authenticate(UsernamePasswordCredentials credentials) {

        LOG.trace("enter BasicScheme.authenticate(UsernamePasswordCredentials)");

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null"); 
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(credentials.getUserName());
        buffer.append(":");
        buffer.append(credentials.getPassword());
        
        return "Basic " + HttpConstants.getAsciiString(
            Base64.encode(HttpConstants.getBytes(buffer.toString())));
    }
}
