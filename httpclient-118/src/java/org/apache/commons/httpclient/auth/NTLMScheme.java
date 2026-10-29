/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/auth/NTLMScheme.java,v 1.2 2003/10/31 18:03:20 sberlin Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/31 18:03:20 $
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

import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.NTLM;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** An implementation of the Microsoft proprietary NTLM authentication scheme.  For a detailed
 * explanation of the NTLM scheme please see <a href="http://davenport.sourceforge.net/ntlm.html">
 * http://davenport.sourceforge.net/ntlm.html</a>.
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
public class NTLMScheme extends AuthSchemeBase {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(NTLMScheme.class);

    /** NTLM challenge string. */
    private String ntlmchallenge = null;

    /**
     * Constructor for the NTLM authentication scheme.
     * 
     * @param challenge The authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     */
    public NTLMScheme(final String challenge) throws MalformedChallengeException {
        super(challenge);
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException("Invalid NTLM challenge: " + challenge);
        }
        int i = challenge.indexOf(' ');
        if (i != -1) {
            s = challenge.substring(i, challenge.length());
            this.ntlmchallenge = s.trim();
        } else {
            this.ntlmchallenge = "";
        }
    }

    /**
     * Returns textual designation of the NTLM authentication scheme.
     * 
     * @return <code>ntlm</code>
     */
    public String getSchemeName() {
        return "ntlm";
    }

    /**
     * The concept of an authentication realm is not supported by the NTLM 
     * authentication scheme. Always returns <code>null</code>.
     * 
     * @return <code>null</code>
     */
    public String getRealm() {
        return null;
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
     * @return String a String identifying the authentication challenge.  The
     * returned value may be null.
     */
    public String getID() {
        return ntlmchallenge;
    }
    

    /**
     * Returns the authentication parameter with the given name, if available.
     * 
     * <p>There are no valid parameters for NTLM authentication so this method always returns
     * <tt>null</tt>.</p>
     * 
     * @param name The name of the parameter to be returned
     * 
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null"); 
        }
        return null;
    }

    /**
     * Create a NTLM authorization string for the given
     * challenge and NT credentials.
     *
     * @param challenge The challenge.
     * @param credentials {@link NTCredentials}
     *
     * @return a ntlm authorization string
     * @throws AuthenticationException is thrown if authentication fails
     */
    public static String authenticate(
     final NTCredentials credentials, final String challenge) 
      throws AuthenticationException {

        LOG.trace("enter NTLMScheme.authenticate(NTCredentials, String)");

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        }
        
        throw new AuthenticationException("NTLM not available.");
        /*
        NTLM ntlm = new NTLM();
        String s = null;
        try {
            s = ntlm.getResponseFor(challenge,
              credentials.getUserName(), credentials.getPassword(),
              credentials.getHost(), credentials.getDomain());
        } catch (HttpException e) {
            throw new AuthenticationException(e.getMessage());
        }
        return "NTLM " + s; */
    }
    
    /**
     * Produces NTLM authorization string for the given set of 
     * {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method Method name is ignored by the NTLM authentication scheme
     * @param uri URI is ignored by the NTLM authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return an NTLM authorization string
     */
    public String authenticate(Credentials credentials, String method, String uri) 
      throws AuthenticationException {
        LOG.trace("enter NTLMScheme.authenticate(Credentials, String, String)");

        NTCredentials ntcredentials = null;
        try {
            ntcredentials = (NTCredentials) credentials;
        } catch (ClassCastException e) {
            throw new AuthenticationException(
             "Credentials cannot be used for NTLM authentication: " 
              + credentials.getClass().getName());
        }
        return NTLMScheme.authenticate(ntcredentials, this.ntlmchallenge);
    }    
}
