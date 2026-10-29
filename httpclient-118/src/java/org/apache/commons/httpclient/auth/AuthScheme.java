/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/auth/AuthScheme.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
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

import org.apache.commons.httpclient.Credentials;

/**
 * <p>
 * This interface represents an abstract authentication scheme.
 * </p>
 * <p>
 * An authentication scheme should be able to support the following
 * functions:
 * <ul>
 *   <li>Provide its textual designation
 *   <li>Provide its parameters, if available
 *   <li>Provide the realm this authentication scheme is applicable to,
 *       if available
 *   <li>Generate authorization string for the given set of credentials,
 *       request method and URI as specificed in the HTTP request line
 * </ul>
 * </p>
 * <p>
 * Authentication schemes may ignore method name and URI parameters
 * if they are relevant for the given authentication mechanism
 * </p>
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 *
 * @since 2.0beta1
 */

public interface AuthScheme {
    
    /**
     * Returns textual designation of the given authentication scheme.
     * 
     * @return the name of the given authentication scheme
     */
    String getSchemeName();

    /**
     * Returns authentication parameter with the given name, if available.
     * 
     * @param name The name of the parameter to be returned
     * 
     * @return the parameter with the given name
     */
    String getParameter(final String name);

    /**
     * Returns authentication realm. If the concept of an authentication
     * realm is not applicable to the given authentication scheme, returns
     * <code>null</code>.
     * 
     * @return the authentication realm
     */
    String getRealm();
    
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
    String getID();
    
    /**
     * Produces an authorization string for the given set of {@link Credentials},
     * method name and URI using the given authentication scheme.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method The name of the method that requires authorization. 
     *   This parameter may be ignored, if it is irrelevant 
     *   or not applicable to the given authentication scheme
     * @param uri The URI for which authorization is needed. 
     *   This parameter may be ignored, if it is irrelevant or not 
     *   applicable to the given authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return the authorization string
     * 
     * @see org.apache.commons.httpclient.HttpMethod#getName()
     * @see org.apache.commons.httpclient.HttpMethod#getPath()
     */
    String authenticate(Credentials credentials, String method, String uri) 
      throws AuthenticationException;
       
}
