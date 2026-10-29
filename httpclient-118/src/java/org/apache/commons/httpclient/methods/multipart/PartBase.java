/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/methods/multipart/PartBase.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
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


/**
 * Provides setters and getters for the basic Part properties.
 * 
 * @author Michael Becke
 */
public abstract class PartBase extends Part {

    /** Name of the file part. */
    private String name;
        
    /** Content type of the file part. */
    private String contentType;

    /** Content encoding of the file part. */
    private String charSet;
    
    /** The transfer encoding. */
    private String transferEncoding;

    /**
     * Constructor.
     * 
     * @param name The name of the part
     * @param contentType The content type, or <code>null</code>
     * @param charset The character encoding, or <code>null</code> 
     * @param transferEncoding The transfer encoding, or <code>null</code>
     */
    public PartBase(String name, String contentType, String charSet, String transferEncoding) {

        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
        this.contentType = contentType;
        this.charSet = charSet;
        this.transferEncoding = transferEncoding;
    }

    /**
     * Returns the name.
     * @return The name.
     * @see org.apache.commons.httpclient.methods.multipart.Part#getName()
     */
    public String getName() { 
        return this.name; 
    }

    /**
     * Returns the content type of this part.
     * @return String The name.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Return the character encoding of this part.
     * @return String The name.
     */
    public String getCharSet() {
        return this.charSet;
    }

    /**
     * Returns the transfer encoding of this part.
     * @return String The name.
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * Sets the character encoding.
     * 
     * @param charSet the character encoding, or <code>null</code> to exclude the character 
     * encoding header
     */
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    /**
     * Sets the content type.
     * 
     * @param contentType the content type, or <code>null</code> to exclude the content type header
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the part name.
     * 
     * @param name
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
    }

    /**
     * Sets the transfer encoding.
     * 
     * @param transferEncoding the transfer encoding, or <code>null</code> to exclude the 
     * transfer encoding header
     */
    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

}
