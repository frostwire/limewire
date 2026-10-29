/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/methods/OptionsMethod.java,v 1.3 2004/08/23 17:38:35 sberlin Exp $
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

package org.apache.commons.httpclient.methods;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * Implements the HTTP OPTIONS method.
 * <p>
 * The HTTP OPTIONS method is defined in section 9.2 of 
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * <blockquote>
 *  The OPTIONS method represents a request for information about the
 *  communication options available on the request/response chain
 *  identified by the Request-URI. This method allows the client to
 *  determine the options and/or requirements associated with a resource,
 *  or the capabilities of a server, without implying a resource action
 *  or initiating a resource retrieval.
 * </blockquote>
 * </p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 *
 * @version $Revision: 1.3 $
 * @since 1.0
 */
public class OptionsMethod
    extends HttpMethodBase {


    // --------------------------------------------------------- Class Variables

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(OptionsMethod.class);

    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     *
     * @since 1.0
     */
    public OptionsMethod() {
    }


    /**
     * Constructor specifying a URI.
     *
     * @param uri either an absolute or relative URI
     *
     * @since 1.0
     */
    public OptionsMethod(String uri) {
        super(uri);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Methods allowed.
     */
    private Vector methodsAllowed = new Vector();


    // --------------------------------------------------------- Public Methods

    /**
     * Get the name.
     * @return "OPTIONS"
     * @since 2.0
     */
    public String getName() {
        return "OPTIONS";
    }


    /**
     * Is the specified method allowed ?
     * 
     * @param method The method to check.
     * @return true if the specified method is allowed.
     * @since 1.0
     */
    public boolean isAllowed(String method) {
        checkUsed();
        return methodsAllowed.contains(method);
    }


    /**
     * Get a list of allowed methods.
     * @return An enumeration of all the allowed methods.
     *
     * @since 1.0
     */
    public Enumeration getAllowedMethods() {
        checkUsed();
        return methodsAllowed.elements();
    }


    // ----------------------------------------------------- HttpMethod Methods

    /**
     * <p>
     * This implementation will parse the <tt>Allow</tt> header to obtain 
     * the set of methods supported by the resource identified by the Request-URI.
     * </p>
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @see #readResponse
     * @see #readResponseHeaders
     * @since 2.0
     */
    protected void processResponseHeaders(HttpState state, HttpConnection conn) {
        LOG.trace("enter OptionsMethod.processResponseHeaders(HttpState, HttpConnection)");

        Header allowHeader = getResponseHeader("allow");
        if (allowHeader != null) {
            String allowHeaderValue = allowHeader.getValue();
            StringTokenizer tokenizer =
                new StringTokenizer(allowHeaderValue, ",");
            while (tokenizer.hasMoreElements()) {
                String methodAllowed =
                    tokenizer.nextToken().trim().toUpperCase();
                methodsAllowed.addElement(methodAllowed);
            }
        }
    }

    /**
     * Return true if the method needs a content-length header in the request.
     *
     * @return true if a content-length header will be expected by the server
     *
     * @since 1.0
     */
    public boolean needContentLength() {
        return false;
    }


}
