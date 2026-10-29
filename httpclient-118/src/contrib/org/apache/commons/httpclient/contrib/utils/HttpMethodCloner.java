/*
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

package org.apache.commons.httpclient.contrib.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

/**
 * In this class are only methods to copy a HttpMethod: 
 * PUT, GET, POST,DELETE, TRACE, ...
 *
 * @author <a href="mailto:mathis@vtg.at">Thomas Mathis</a>
 * 
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * to be used without additional customization.
 */

public class HttpMethodCloner {

    private static void copyEntityEnclosingMethod(
      EntityEnclosingMethod m, EntityEnclosingMethod copy )
        throws java.io.IOException
     {
         copy.setRequestBody(m.getRequestBodyAsString());
         copy.setUseExpectHeader(m.getUseExpectHeader());
     }
 
    private static void copyHttpMethodBase(
      HttpMethodBase m, HttpMethodBase copy) {
        if (m.getHostConfiguration() != null) {
            copy.setHostConfiguration(
              new HostConfiguration(m.getHostConfiguration()));
        }
        copy.setHttp11(m.isHttp11());
        copy.setStrictMode(m.isStrictMode());
    }

    /**
     * Clones a HttpMethod. <br>
     * <b>Attention:</b> You have to clone a method before it has 
     * been executed, because the URI can change if followRedirects 
     * is set to true.
     *
     * @param m the HttpMethod to clone
     *
     * @return the cloned HttpMethod, null if the HttpMethod could 
     * not be instantiated
     *
     * @throws java.io.IOException if the request body couldn't be read
     */
    public static HttpMethod clone(HttpMethod m) 
      throws java.io.IOException
    {
        HttpMethod copy = null;

        // copy the HttpMethod
        try {
            copy = (HttpMethod) m.getClass().newInstance();
        } catch (InstantiationException iEx) {
        } catch (IllegalAccessException iaEx) {
        }
        if ( copy == null ) {
            return null;
        }
        copy.setDoAuthentication(m.getDoAuthentication());
        copy.setFollowRedirects(m.getFollowRedirects());
        copy.setPath( m.getPath() );
        copy.setQueryString(m.getQueryString());

        // clone the headers
        Header[] h = m.getRequestHeaders();
        int size = (h == null) ? 0 : h.length;

        for (int i = 0; i < size; i++) {
            copy.setRequestHeader(
              new Header(h[i].getName(), h[i].getValue()));
        }
        copy.setStrictMode(m.isStrictMode());
        if (m instanceof HttpMethodBase) {
            copyHttpMethodBase(
              (HttpMethodBase)m, 
              (HttpMethodBase)copy);
        }
        if (m instanceof EntityEnclosingMethod) {
            copyEntityEnclosingMethod(
              (EntityEnclosingMethod)m,
              (EntityEnclosingMethod)copy);
        }
        return copy;
    }
}
