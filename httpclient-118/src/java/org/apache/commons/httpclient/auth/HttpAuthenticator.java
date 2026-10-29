/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/auth/HttpAuthenticator.java,v 1.3 2004/08/23 17:38:35 sberlin Exp $
 * $Revision: 1.3 $
 * $Date: 2004/08/23 17:38:35 $
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

import java.util.Map;
import java.util.HashMap;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for HTTP authorization and authentication.  This class
 * provides utility methods for generating responses to HTTP www and proxy
 * authentication challenges.
 * 
 * <blockquote>
 * A client SHOULD assume that all paths at or deeper than the depth of the
 * last symbolic element in the path field of the Request-URI also are within
 * the protection space specified by the basic realm value of the current
 * challenge. A client MAY preemptively send the corresponding Authorization
 * header with requests for resources in that space without receipt of another
 * challenge from the server. Similarly, when a client sends a request to a
 * proxy, it may reuse a userid and password in the Proxy-Authorization header
 * field without receiving another challenge from the proxy server.
 * </blockquote>
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
public final class HttpAuthenticator {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HttpAuthenticator.class);

    /**
     * The www authenticate challange header.
     */
    public static final String WWW_AUTH = "WWW-Authenticate";


    /**
     * The www authenticate response header.
     */
    public static final String WWW_AUTH_RESP = "Authorization";


    /**
     * The proxy authenticate challange header.
     */
    public static final String PROXY_AUTH = "Proxy-Authenticate";


    /**
     * The proxy authenticate response header.
     */
    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";

    /** Chooses the strongest authentication scheme supported from the
     * array of authentication challenges. Currently only <code>NTLM</code>,
     * <code>Digest</code>, <code>Basic</code> schemes are recognized. 
     * The <code>NTLM</code> scheme is considered the strongest and is 
     * preferred to all others. The <code>Digest</code> scheme is preferred to 
     * the <code>Basic</code> one which provides no encryption for credentials.
     * The <code>Basic</code> scheme is used only if it is the only one 
     * supported.
     * 
     * @param challenges The array of authentication challenges
     * 
     * @return The strongest authentication scheme supported
     * 
     * @throws MalformedChallengeException is thrown if an authentication 
     *  challenge is malformed
     * @throws AuthenticationUnsupportedException when none of challenge types
     *  available is supported.
     */
    public static AuthScheme selectAuthScheme(final Header[] challenges)
      throws MalformedChallengeException, AuthenticationUnsupportedException {
        LOG.trace("enter HttpAuthenticator.selectAuthScheme(Header[])");
        if (challenges == null) {
            throw new IllegalArgumentException("Array of challenges may not be null");
        }
        if (challenges.length == 0) {
            throw new IllegalArgumentException("Array of challenges may not be empty");
        }
        String challenge = null;
        Map challengemap = new HashMap(challenges.length); 
        for (int i = 0; i < challenges.length; i++) {
            challenge = challenges[i].getValue();
            String s = AuthChallengeParser.extractScheme(challenge);
            challengemap.put(s, challenge);
        }
        challenge = (String) challengemap.get("ntlm");
        if (challenge != null) {
            return new NTLMScheme(challenge);
        }
        challenge = (String) challengemap.get("digest");
        if (challenge != null) {
            return new DigestScheme(challenge);
        }
        challenge = (String) challengemap.get("basic");
        if (challenge != null) {
            return new BasicScheme(challenge);
        }
        throw new AuthenticationUnsupportedException(
          "Authentication scheme(s) not supported: " + challengemap.toString()); 
    }
    

    private static boolean doAuthenticateDefault(
        HttpMethod method, 
        HttpConnection conn,
        HttpState state, 
        boolean proxy)
      throws AuthenticationException {
        if (method == null) {
            throw new IllegalArgumentException("HTTP method may not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("HTTP state may not be null");
        }
        String host = null;
        if (conn != null) {
            host = proxy ? conn.getProxyHost() : conn.getHost();
        }
        Credentials credentials = proxy 
            ? state.getProxyCredentials(null, host) : state.getCredentials(null, host);
        if (credentials == null) {
            return false;
        }
        if (!(credentials instanceof UsernamePasswordCredentials)) {
            throw new AuthenticationException(
             "Credentials cannot be used for basic authentication: " 
              + credentials.toString());
        }
        String auth = BasicScheme.authenticate((UsernamePasswordCredentials) credentials);
        if (auth != null) {
            String s = proxy ? PROXY_AUTH_RESP : WWW_AUTH_RESP;
            method.setRequestHeader(s, auth);
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * Attempt to provide default authentication credentials 
     * to the given method in the given context using basic 
     * authentication scheme.
     * 
     * @param method the HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state the HttpState object providing Credentials
     * 
     * @return true if the <tt>Authenticate</tt> response header 
     *   was added
     * 
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,String,Credentials)
     */
    public static boolean authenticateDefault(
        HttpMethod method, 
        HttpConnection conn,
        HttpState state)
      throws AuthenticationException {
        LOG.trace(
            "enter HttpAuthenticator.authenticateDefault(HttpMethod, HttpConnection, HttpState)");
        return doAuthenticateDefault(method, conn, state, false);
    }


    /**
     * Attempt to provide default proxy authentication credentials 
     * to the given method in the given context using basic 
     * authentication scheme.
     * 
     * @param method the HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state the HttpState object providing Credentials
     * 
     * @return true if the <tt>Proxy-Authenticate</tt> response header 
     *   was added
     * 
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,Credentials)
     */
    public static boolean authenticateProxyDefault(
        HttpMethod method, 
        HttpConnection conn,
        HttpState state)
      throws AuthenticationException {
        LOG.trace("enter HttpAuthenticator.authenticateProxyDefault(HttpMethod, HttpState)");
        return doAuthenticateDefault(method, conn, state, true);
    }


    private static boolean doAuthenticate(
        AuthScheme authscheme, 
        HttpMethod method, 
        HttpConnection conn,
        HttpState state, 
        boolean proxy)
       throws AuthenticationException {
        if (authscheme == null) {
            throw new IllegalArgumentException("Authentication scheme may not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("HTTP method may not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("HTTP state may not be null");
        }
        String host = null;
        if (conn != null) {
            if (proxy) {
                host = conn.getProxyHost();
            } else {
                host = conn.getVirtualHost();
                if (host == null) {
                    host = conn.getHost();
                }
            }
        }
        String realm = authscheme.getRealm();
        if (LOG.isDebugEnabled()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Authenticating with the "); 
            if (realm == null) {
                buffer.append("default");
            } else {
                buffer.append('\'');
                buffer.append(realm);
                buffer.append('\'');
            }
            buffer.append(" authentication realm at "); 
            buffer.append(host); 
            LOG.debug(buffer.toString());
        }
        // TODO: To be removed in the future. Required for backward compatibility
        if (realm == null) {
            realm = host;
        }
        Credentials credentials = proxy 
            ? state.getProxyCredentials(realm, host) 
            : state.getCredentials(realm, host);
        if (credentials == null) {
            throw new AuthenticationException(
                "No credentials available for the '" + authscheme.getRealm() 
                + "' authentication realm at " + host);
        }
        String auth = authscheme.authenticate(credentials, method.getName(), method.getPath());
        if (auth != null) {
            String s = proxy ? PROXY_AUTH_RESP : WWW_AUTH_RESP;
            method.setRequestHeader(s, auth);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to provide requisite authentication credentials to the 
     * given method in the given context using the given 
     * authentication scheme.
     * 
     * @param authscheme The authentication scheme to be used
     * @param method The HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state The HttpState object providing Credentials
     * 
     * @return true if the <tt>Authenticate</tt> response header was added
     * 
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,Credentials)
     */
    public static boolean authenticate(
        AuthScheme authscheme, 
        HttpMethod method, 
        HttpConnection conn,
        HttpState state) 
        throws AuthenticationException {
       LOG.trace(
            "enter HttpAuthenticator.authenticate(AuthScheme, HttpMethod, HttpConnection, "
            + "HttpState)");
        return doAuthenticate(authscheme, method, conn, state, false);
    }


    /**
     * Attempt to provide requisite proxy authentication credentials 
     * to the given method in the given context using 
     * the given authentication scheme.
     * 
     * @param authscheme The authentication scheme to be used
     * @param method the HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state the HttpState object providing Credentials
     * 
     * @return true if the <tt>Proxy-Authenticate</tt> response header 
     *  was added
     * 
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,Credentials)
     */
    public static boolean authenticateProxy(
        AuthScheme authscheme, 
        HttpMethod method, 
        HttpConnection conn,
        HttpState state
    ) throws AuthenticationException {
       LOG.trace("enter HttpAuthenticator.authenticateProxy(AuthScheme, HttpMethod, HttpState)");
       return doAuthenticate(authscheme, method, conn, state, true);
    }
}
