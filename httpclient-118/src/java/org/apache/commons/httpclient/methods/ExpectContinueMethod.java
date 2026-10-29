/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/methods/ExpectContinueMethod.java,v 1.2 2003/10/31 18:03:21 sberlin Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/31 18:03:21 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

import java.io.IOException;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * This abstract class serves as a foundation for all HTTP methods 
 * that support 'Expect: 100-continue' handshake.
 * </p>
 * 
 * <p>
 * The purpose of the 100 (Continue) status (refer to section 10.1.1 
 * of the RFC 2616 for more details) is to allow a client that is 
 * sending a request message with a request body to determine if the 
 * origin server is willing to accept the request (based on the request 
 * headers) before the client sends the request body. In some cases,
 * it might either be inappropriate or highly inefficient for the 
 * client to send the body if the server will reject the message 
 * without looking at the body.
 * </p>
 * 
 * <p>
 * 'Expect: 100-continue' handshake should be used with caution,
 * as it may cause problems with HTTP servers and proxies that
 * do not support HTTP/1.1 protocol.
 * </p>
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * @since 2.0beta1
 */

public abstract class ExpectContinueMethod extends GetMethod {
    
    /** This flag specifies whether "expect: 100-continue" handshake is
     * to be used prior to sending the request body */
    private boolean useExpectHeader = false;
    
    /** LOG object for this class. */
    private static final Log LOG = LogFactory.getLog(ExpectContinueMethod.class);

    /**
     * No-arg constructor.
     *
     * @since 2.0
     */
    public ExpectContinueMethod() {
        super();
    }

    /**
     * Constructor specifying a URI.
     *
     * @param uri either an absolute or relative URI
     *
     * @since 2.0
     */
    public ExpectContinueMethod(String uri) {
        super(uri);
    }

    /**
     * <p>
     * Returns <tt>true</tt> if the 'Expect: 100-Continue' handshake
     * is activated. The purpose of the 'Expect: 100-Continue' 
     * handshake to allow a client that is sending a request message 
     * with a request body to determine if the origin server is 
     * willing to accept the request (based on the request headers) 
     * before the client sends the request body.
     * </p>
     * 
     * @return <tt>true</tt> if 'Expect: 100-Continue' handshake is to
     * be used, <tt>false</tt> otherwise.
     * 
     * @since 2.0beta1
     */
    public boolean getUseExpectHeader() {
        return this.useExpectHeader;
    }

    /**
     * <p>
     * Activates 'Expect: 100-Continue' handshake. The purpose of 
     * the 'Expect: 100-Continue' handshake to allow a client that is 
     * sending a request message with a request body to determine if 
     * the origin server is willing to accept the request (based on 
     * the request headers) before the client sends the request body.
     * </p>
     * 
     * <p>
     * The use of the 'Expect: 100-continue' handshake can result in 
     * noticable peformance improvement for entity enclosing requests
     * (such as POST and PUT) that require the target server's 
     * authentication.
     * </p>
     * 
     * <p>
     * 'Expect: 100-continue' handshake should be used with 
     * caution, as it may cause problems with HTTP servers and 
     * proxies that do not support HTTP/1.1 protocol.
     * </p>
     * 
     * @param value boolean value
     * 
     * 
     * @since 2.0beta1
     */
    public void setUseExpectHeader(boolean value) {
        this.useExpectHeader = value;
    }

    /**
     * Returns <tt>true</tt> if there is a request body to be sent.
     * 'Expect: 100-continue' handshake may not be used if request
     * body is not present
     * 
     * @return boolean
     * 
     * @since 2.0beta1
     */
    protected abstract boolean hasRequestContent();

    /**
     * Sets the <tt>Expect</tt> header if it has not already been set, 
     * in addition to the "standard" set of headers.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter ExpectContinueMethod.addRequestHeaders(HttpState, HttpConnection)");
        
        super.addRequestHeaders(state, conn);
        // If the request is being retried, the header may already be present
        boolean headerPresent = (getRequestHeader("Expect") != null);
        // See if the expect header should be sent
        // = HTTP/1.1 or higher
        // = request body present

        if (getUseExpectHeader() && isHttp11() && hasRequestContent()) {
            if (!headerPresent) {
                setRequestHeader("Expect", "100-continue");
            }
        } else {
            if (headerPresent) {
                removeRequestHeader("Expect");
            }
        }
    }
}
