/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/Authenticator.java,v 1.3 2004/08/23 17:38:35 sberlin Exp $
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

package org.apache.commons.httpclient;

import java.util.ArrayList;
import org.apache.commons.httpclient.auth.HttpAuthenticator; 
import org.apache.commons.httpclient.auth.AuthScheme; 
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
 * the protection space specified by the BasicScheme realm value of the current
 * challenge. A client MAY preemptively send the corresponding Authorization
 * header with requests for resources in that space without receipt of another
 * challenge from the server. Similarly, when a client sends a request to a
 * proxy, it may reuse a userid and password in the Proxy-Authorization header
 * field without receiving another challenge from the proxy server.
 * </blockquote>
 * </p>
 * 
 * @deprecated use {@link org.apache.commons.httpclient.auth.HttpAuthenticator}
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
public class Authenticator {

    // -------------------------------------- Static variables and initializers

    /**
     * <tt>org.apache.commons.httpclient.Authenticator</tt> LOG.
     */
    private static final Log LOG = LogFactory.getLog(Authenticator.class);

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


    // ---------------------------------------------------------------- Methods

    /**
     * Add requisite authentication credentials to the given <i>method</i> in
     * the given <i>state</i> if possible.
     * 
     * @param method the HttpMethod which requires authentication
     * @param state the HttpState object providing Credentials
     * @return true if the Authenticate response header was added
     * @throws HttpException when a parsing or other error occurs
     * @throws UnsupportedOperationException when the challenge type is not
     *         supported
     * @see HttpState#setCredentials(String,Credentials)
     * 
     * @deprecated use {@link 
     * HttpAuthenticator#authenticate(AuthScheme, HttpMethod, HttpConnection, HttpState)}
     */
    public static boolean authenticate(HttpMethod method, HttpState state)
        throws HttpException, UnsupportedOperationException {

        LOG.trace("enter Authenticator.authenticate(HttpMethod, HttpState)");

        return authenticate(method, state, false);
    }


    /**
     * Add requisite proxy authentication credentials to the given
     * <i>method</i> in the given <i>state</i> if possible.
     * 
     * @param method the HttpMethod which requires authentication
     * @param state the HttpState object providing Credentials
     * @return true if the Authenticate response header was added
     * @throws HttpException when a parsing or other error occurs
     * @throws UnsupportedOperationException when the given challenge type is
     *         not supported
     * @see HttpState#setProxyCredentials(String,Credentials)
     * 
     * @deprecated use {@link 
     * HttpAuthenticator#authenticateProxy(AuthScheme, HttpMethod, HttpConnection, HttpState)}
     */
    public static boolean authenticateProxy(HttpMethod method, HttpState state)
        throws HttpException, UnsupportedOperationException {

        LOG.trace("enter Authenticator.authenticateProxy(HttpMethod, "
                  + "HttpState)");

        return authenticate(method, state, true);
    }


    /**
     * Add requisite authentication credentials to the given <i>method</i>
     * using the given the <i>challengeHeader</i>. Currently <b>BasicScheme</b> and
     * <b>DigestScheme</b> authentication are supported. If the challengeHeader is
     * null, the default authentication credentials will be sent.
     * 
     * @param method the http method to add the authentication header to
     * @param state the http state object providing {@link Credentials}
     * @param proxy a flag indicating if the authentication is against a proxy
     * 
     * @return true if a response header was added
     * 
     * @throws HttpException when an error occurs parsing the challenge
     * @throws UnsupportedOperationException when the given challenge type is
     *         not supported
     * @see #basic
     * @see #digest
     * @see HttpMethod#addRequestHeader
     */
    private static boolean authenticate(HttpMethod method, HttpState state, 
        boolean proxy)
        throws HttpException, UnsupportedOperationException {

        LOG.trace("enter Authenticator.authenticate(HttpMethod, HttpState, "
                  + "Header, String)");
        return authenticate(method, null, state, proxy);
   }

    private static boolean authenticate(HttpMethod method, HttpConnection conn,
            HttpState state, boolean proxy)
            throws HttpException, UnsupportedOperationException {
        String challengeheader = proxy ? PROXY_AUTH : WWW_AUTH;

        // I REALLY hate doing this, but I need to avoid multiple autorization
        // headers being condenced itno one. Currently HttpMethod interface
        // does not provide this kind of functionality
        Header[] headers = method.getResponseHeaders();
        ArrayList headerlist = new ArrayList();
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            if (header.getName().equalsIgnoreCase(challengeheader)) {
                headerlist.add(header);
            }
        }        
        headers = (Header[]) headerlist.toArray(new Header[headerlist.size()]);        
        headerlist = null;

        //if there is no challenge, attempt to use preemptive authorization
        if (headers.length == 0) {
            if (state.isAuthenticationPreemptive()) {
                LOG.debug("Preemptively sending default basic credentials");
                if (proxy) {
                    return HttpAuthenticator.authenticateProxyDefault(method, conn, state);
                } else {
                    return HttpAuthenticator.authenticateDefault(method, conn, state);
                }
            }
            return false;
        }

        // parse the authenticate headers
        AuthScheme authscheme = HttpAuthenticator.selectAuthScheme(headers);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using " + authscheme.getSchemeName() + " authentication scheme");
        }
        if (proxy) {
            return HttpAuthenticator.authenticateProxy(authscheme, method, conn, state);
        } else {
            return HttpAuthenticator.authenticate(authscheme, method, conn, state);
        }

    }
}
