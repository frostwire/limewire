/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/methods/EntityEnclosingMethod.java,v 1.2 2003/10/31 18:03:21 sberlin Exp $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.ContentLengthInputStream;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This abstract class serves as a foundation for all HTTP methods 
 * that can enclose an entity within requests 
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 *
 * @since 2.0beta1
 * @version $Revision: 1.2 $
 */
public abstract class EntityEnclosingMethod extends ExpectContinueMethod {

    // ----------------------------------------- Static variables/initializers

    /**
     * The content length will be calculated automatically. This implies
     * buffering of the content.
     */
    public static final int CONTENT_LENGTH_AUTO = -2;

    /**
     * The request will use chunked transfer encoding. Content length is not
     * calculated and the content is not buffered.<br>
     */
    public static final int CONTENT_LENGTH_CHUNKED = -1;

    /** LOG object for this class. */
    private static final Log LOG = LogFactory.getLog(EntityEnclosingMethod.class);

    /** The buffered request body, if any. */
    private byte[] buffer = null;

    /** The unbuffered request body, if any. */
    private InputStream requestStream = null;

    /** The request body as string, if any. */
    private String requestString = null;

    /** for optimization purpose, the generated request body may be
     * cached when the method is being executed.
     */
    private byte[] contentCache = null; 
    
    /** Counts how often the request was sent to the server. */
    private int repeatCount = 0;

    /** The content length of the <code>requestBodyStream</code> or one of
     *  <code>CONTENT_LENGTH_AUTO</code> and <code>CONTENT_LENGTH_CHUNKED</code>.
     */
    private int requestContentLength = CONTENT_LENGTH_AUTO;

    // ----------------------------------------------------------- Constructors

    /**
     * No-arg constructor.
     *
     * @since 2.0
     */
    public EntityEnclosingMethod() {
        super();
        setFollowRedirects(false);
    }

    /**
     * Constructor specifying a URI.
     *
     * @param uri either an absolute or relative URI
     *
     * @since 2.0
     */
    public EntityEnclosingMethod(String uri) {
        super(uri);
        setFollowRedirects(false);
    }

    /**
     * Returns <tt>true</tt> if there is a request body to be sent.
     * 
     * <P>This method must be overridden by sub-classes that implement
     * alternative request content input methods
     * </p>
     * 
     * @return boolean
     * 
     * @since 2.0beta1
     */
    protected boolean hasRequestContent() {
        LOG.trace("enter EntityEnclosingMethod.hasRequestContent()");
        return (this.buffer != null) 
            || (this.requestStream != null) 
            || (this.requestString != null);
    }

    /**
     * Clears request body.
     * 
     * <p>This method must be overridden by sub-classes that implement
     * alternative request content input methods.</p>
     * 
     * @since 2.0beta1
     */
    protected void clearRequestBody() {
        LOG.trace("enter EntityEnclosingMethod.clearRequestBody()");
        this.requestStream = null;
        this.requestString = null;
        this.buffer = null;
        this.contentCache = null;
    }

    /**
     * Generates request body.   
     * 
     * <p>This method must be overridden by sub-classes that implement
     * alternative request content input methods.</p>
     * 
     * @return request body as an array of bytes. If the request content 
     *          has not been set, returns <tt>null</null>.
     * 
     * @since 2.0beta1
     */
    protected byte[] generateRequestBody() {
        LOG.trace("enter EntityEnclosingMethod.renerateRequestBody()");
        if (this.requestStream != null) {
            bufferContent();
        }
         
        if (this.buffer != null) { 
            return this.buffer;
        } else if (this.requestString != null) {
            return HttpConstants.getContentBytes(this.requestString, getRequestCharSet());
        } else {
            return null;
        }
    }

    /**
     * Entity enclosing requests cannot be redirected without user intervention
     * according to RFC 2616.
     *
     * @return <code>false</code>.
     *
     * @since 2.0
     */
    public boolean getFollowRedirects() {
        return false;
    }


    /**
     * Entity enclosing requests cannot be redirected without user intervention 
     * according to RFC 2616.
     *
     * @param followRedirects must always be <code>false</code>
     */
    public void setFollowRedirects(boolean followRedirects) {
        if (followRedirects == true) {
            // TODO: EntityEnclosingMethod should inherit from HttpMethodBase rather than GetMethod
            // Enable exception once the inheritence is fixed
            //throw new IllegalArgumentException(
            //  "Entity enclosing requests cannot be redirected without user intervention");
        }
        super.setFollowRedirects(false);
    }

    /**
     * Sets length information about the request body.
     *
     * <p>
     * Note: If you specify a content length the request is unbuffered. This
     * prevents redirection and automatic retry if a request fails the first
     * time. This means that the HttpClient can not perform authorization
     * automatically but will throw an Exception. You will have to set the
     * necessary 'Authorization' or 'Proxy-Authorization' headers manually.
     * </p>
     *
     * @param length size in bytes or any of CONTENT_LENGTH_AUTO,
     *        CONTENT_LENGTH_CHUNKED. If number of bytes or CONTENT_LENGTH_CHUNKED
     *        is specified the content will not be buffered internally and the
     *        Content-Length header of the request will be used. In this case
     *        the user is responsible to supply the correct content length.
     *        If CONTENT_LENGTH_AUTO is specified the request will be buffered
     *        before it is sent over the network.
     *
     */
    public void setRequestContentLength(int length) {
        LOG.trace("enter EntityEnclosingMethod.setRequestContentLength(int)");
        this.requestContentLength = length;
    }

    /**
     * Overrides method of {@link org.apache.commons.httpclient.HttpMethodBase}
     * to return the length of the request body.
     *
     * @return number of bytes in the request body
     */
    protected int getRequestContentLength() {
        LOG.trace("enter EntityEnclosingMethod.getRequestContentLength()");

        if (!hasRequestContent()) {
            return 0;
        }
        if (this.requestContentLength != CONTENT_LENGTH_AUTO) {
            return this.requestContentLength;
        }
        if (this.contentCache == null) {
            this.contentCache = generateRequestBody(); 
        }
        return (this.contentCache == null) ? 0 : this.contentCache.length;
    }

    /**
     * Generates <tt>Content-Length</tt> or <tt>Transfer-Encoding: Chunked</tt>
     * request header, as long as no <tt>Content-Length</tt> request header
     * already exists.
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
    protected void addContentLengthRequestHeader(HttpState state,
                                                 HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.addContentLengthRequestHeader("
                  + "HttpState, HttpConnection)");

        if ((getRequestHeader("content-length") == null)
            && (getRequestHeader("Transfer-Encoding") == null)) {
            int len = getRequestContentLength();
            if (len >= 0) {
                addRequestHeader("Content-Length", String.valueOf(len));
            } else if ((len == CONTENT_LENGTH_CHUNKED) && (isHttp11())) {
                addRequestHeader("Transfer-Encoding", "chunked");
            }
        }
    }

    /**
     * Sets the request body to be the specified inputstream.
     *
     * @param body Request body content as {@link java.io.InputStream}
     */
    public void setRequestBody(InputStream body) {
        LOG.trace("enter EntityEnclosingMethod.setRequestBody(InputStream)");
        clearRequestBody();
        this.requestStream = body;
    }

    /**
     * Returns the request body as a {@link java.io.InputStream}.
     * Calling this method will cause the content to be buffered.
     *
     * @return The request body as a {@link java.io.InputStream} if it has been set.
     */
    public InputStream getRequestBody() {
        LOG.trace("enter EntityEnclosingMethod.getRequestBody()");
        byte [] content = generateRequestBody();
        if (content != null) {
            return new ByteArrayInputStream(content);
        } else {
            return new ByteArrayInputStream(new byte[] {});
        }
    }

    /**
     * Sets the request body to be the specified string.
     * The string will be submitted, using the encoding
     * specified in the Content-Type request header.<br>
     * Example: <code>setRequestHeader("Content-type", "text/xml; charset=UTF-8");</code><br>
     * Would use the UTF-8 encoding.
     * If no charset is specified, the 
     * {@link org.apache.commons.httpclient.HttpConstants#DEFAULT_CONTENT_CHARSET default}
     * content encoding is used (ISO-8859-1).
     *
     * @param body Request body content as a string
     */
    public void setRequestBody(String body) {
        LOG.trace("enter EntityEnclosingMethod.setRequestBody(String)");
        clearRequestBody();
        this.requestString = body;
    }

    /**
     * Returns the request body as a {@link java.lang.String}.
     * Calling this method will cause the content to be buffered.
     *
     * @return the request body as a {@link java.lang.String}
     * 
     * @throws IOException when i/o error occurs while reading the request
     */
    public String getRequestBodyAsString() throws IOException {
        LOG.trace("enter EntityEnclosingMethod.getRequestBodyAsString()");
        byte [] content = generateRequestBody();
        if (content != null) {
            return HttpConstants.getContentString(content, getRequestCharSet());
        } else {
            return null;
        }
    }


    /**
     * Writes the request body to the given {@link HttpConnection connection}.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@link HttpConnection connection} used to execute
     *        this HTTP method
     *
     * @return <tt>true</tt>
     *
     * @throws IOException if an I/O (transport) error occurs
     * @throws HttpException  if a protocol exception occurs.
     * @throws HttpRecoverableException if a recoverable transport error occurs. 
     *                    Usually this kind of exceptions can be recovered from by
     *                    retrying the HTTP method 
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace(
            "enter EntityEnclosingMethod.writeRequestBody(HttpState, HttpConnection)");
        
        if (!hasRequestContent()) {
            LOG.debug("Request body has not been specified");
            return true;
        }

        int contentLength = getRequestContentLength();

        if ((contentLength == CONTENT_LENGTH_CHUNKED) && !isHttp11()) {
            throw new HttpException(
                "Chunked transfer encoding not allowed for HTTP/1.0");
        }
        
        InputStream instream = null;
        if (this.requestStream != null) {
            LOG.debug("Using unbuffered request body"); 
            instream = this.requestStream;
        } else { 
            if (this.contentCache == null) {
                this.contentCache = generateRequestBody();
            }
            if (this.contentCache != null) { 
                LOG.debug("Using buffered request body"); 
                instream = new ByteArrayInputStream(this.contentCache);
            }
        }

        if (instream == null) {
            LOG.debug("Request body is empty");
            return true;
        }

        if ((this.repeatCount > 0) && (this.contentCache == null)) {
            throw new HttpException(
                "Unbuffered entity enclosing request can not be repeated.");
        }

        this.repeatCount++;

        OutputStream outstream = conn.getRequestOutputStream();

        if (contentLength == CONTENT_LENGTH_CHUNKED) {
            outstream = new ChunkedOutputStream(outstream);
        }
        if (contentLength >= 0) {
            // don't need a watcher here - we're reading from something local,
            // not server-side.
            instream = new ContentLengthInputStream(instream, contentLength);
        }

        byte[] tmp = new byte[4096];
        int total = 0;
        int i = 0;
        while ((i = instream.read(tmp)) >= 0) {
            outstream.write(tmp, 0, i);
            total += i;
        }
        // This is hardly the most elegant solution to closing chunked stream
        if (outstream instanceof ChunkedOutputStream) {
            ((ChunkedOutputStream) outstream).writeClosingChunk();
        }
        if ((contentLength > 0) && (total < contentLength)) {
            throw new IOException("Unexpected end of input stream after "
                + total + " bytes (expected " + contentLength + " bytes)");
        }
        LOG.debug("Request body sent");
        return true;
    }

    /**
     * Recycles the HTTP method so that it can be used again.
     * Note that all of the instance variables will be reset
     * once this method has been called. This method will also
     * release the connection being used by this HTTP method.
     * 
     * @see #releaseConnection()
     */
    public void recycle() {
        LOG.trace("enter EntityEnclosingMethod.recycle()");
        clearRequestBody();
        this.requestContentLength = CONTENT_LENGTH_AUTO;
        this.repeatCount = 0;
        super.recycle();
    }

    /**
     * Buffers request body input stream.
     */
    private void bufferContent() {
        LOG.trace("enter EntityEnclosingMethod.bufferContent()");

        if (this.buffer != null) {
            // Already been buffered
            return;
        }
        if (this.requestStream != null) {
            try {
                ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                byte[] data = new byte[4096];
                int l = 0;
                while ((l = this.requestStream.read(data)) >= 0) {
                    tmp.write(data, 0, l);
                }
                this.buffer = tmp.toByteArray();
                this.requestStream = null;
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                this.buffer = null;
                this.requestStream = null;
            }
        }
    }
}
