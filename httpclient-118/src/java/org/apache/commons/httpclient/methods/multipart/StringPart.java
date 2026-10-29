/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/methods/multipart/StringPart.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
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

package org.apache.commons.httpclient.methods.multipart;

import java.io.OutputStream;
import java.io.IOException;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple string parameter for a multipart post
 *
 * @author <a href="mailto:mattalbright@yahoo.com">Matthew Albright</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * @since 2.0
 */
public class StringPart extends PartBase {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(StringPart.class);

    /** Default content encoding of string parameters. */
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    /** Default charset of string parameters*/
    public static final String DEFAULT_CHARSET = "US-ASCII";

    /** Default transfer encoding of string parameters*/
    public static final String DEFAULT_TRANSFER_ENCODING = "8bit";

    /** Contents of this StringPart. */
    private byte[] content;
    
    /** The String value of this part. */
    private String value;

    /**
     * Constructor.
     *
     * @param name The name of the part
     * @param value the string to post
     * @param charset the charset to be used to encode the string, if <code>null</code> 
     * the {@link #DEFAULT_CHARSET default} is used
     */
    public StringPart(String name, String value, String charset) {
        
        super(
            name,
            DEFAULT_CONTENT_TYPE,
            charset == null ? DEFAULT_CHARSET : charset,
            DEFAULT_TRANSFER_ENCODING
        );
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        }
        if (value.indexOf(0) != -1) {
            // See RFC 2048, 2.8. "8bit Data"
            throw new IllegalArgumentException("NULs may not be present in string parts");
        }
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param name The name of the part
     * @param value the string to post
     */
    public StringPart(String name, String value) {
        this(name, value, null);
    }
    
    /**
     * Gets the content in bytes.  Bytes are lazily created to allow the charset to be changed
     * after the part is created.
     * 
     * @return the content in bytes
     */
    private byte[] getContent() {
        if (content == null) {
            content = HttpConstants.getContentBytes(value, getCharSet());
        }
        return content;
    }
    
    /**
     * Writes the data to the given OutputStream.
     * @param out the OutputStream to write to
     * @throws IOException if there is a write error
     */
    protected void sendData(OutputStream out) throws IOException {
        LOG.trace("enter sendData(OutputStream)");
        out.write(getContent());
    }
    
    /**
     * Return the length of the data.
     * @return The length of the data.
     * @throws IOException If an IO problem occurs
     * @see org.apache.commons.httpclient.methods.multipart.Part#lengthOfData()
     */
    protected long lengthOfData() throws IOException {
        LOG.trace("enter lengthOfData()");
        return getContent().length;
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.multipart.BasePart#setCharSet(java.lang.String)
     */
    public void setCharSet(String charSet) {
        super.setCharSet(charSet);
        this.content = null;
    }

}
