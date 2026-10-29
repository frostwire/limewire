/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/StatusLine.java,v 1.2 2004/05/17 17:41:21 sberlin Exp $
 * $Revision: 1.2 $
 * $Date: 2004/05/17 17:41:21 $
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

/**
 * Represents a Status-Line as returned from a HTTP server.
 *
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a> states
 * the following regarding the Status-Line:
 * <pre>
 * 6.1 Status-Line
 *
 *  The first line of a Response message is the Status-Line, consisting
 *  of the protocol version followed by a numeric status code and its
 *  associated textual phrase, with each element separated by SP
 *  characters. No CR or LF is allowed except in the final CRLF sequence.
 *
 *      Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
 * </pre>
 * <p>
 * This class is immutable and is inherently thread safe.
 *
 * @see HttpStatus
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @version $Id: StatusLine.java,v 1.2 2004/05/17 17:41:21 sberlin Exp $
 * @since 2.0
 */
public class StatusLine {

    // ----------------------------------------------------- Instance Variables

    /** The original Status-Line. */
    private final String statusLine;

    /** The HTTP-Version. */
    private final String httpVersion;

    /** The Status-Code. */
    private final int statusCode;

    /** The Reason-Phrase. */
    private final String reasonPhrase;


    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     *
     * @param statusLine the status line returned from the HTTP server
     * @throws HttpException if the status line is invalid
     */
    public StatusLine(String statusLine) 
    throws HttpException {

        //save the original Status-Line
        this.statusLine = new String(statusLine);
        int length = statusLine.length();


        //check validity of the Status-Line
        if (!statusLine.startsWith("HTTP")) {
            throw new HttpException("Status-Line '" + statusLine 
                + "' does not start with HTTP");
        }

        //handle the HTTP-Version
        int at = statusLine.indexOf(" ");
        if (at <= 0) {
            throw new HttpException(
                    "Unable to parse HTTP-Version from the status line: '"
                    + statusLine + "'");
        }
        this.httpVersion = (statusLine.substring(0, at)).toUpperCase();

        //advance through spaces
        while (statusLine.charAt(at) == ' ') {
            at++;
            if(at >= length)
                throw new HttpException(
                    "Unable to parse status code from status line: '"
                    + statusLine + "'");
        }

        //handle the Status-Code
        int to = statusLine.indexOf(" ", at);
        if (to < 0) {
            to = length;
        }
        try {
            this.statusCode = Integer.parseInt(statusLine.substring(at, to));
        } catch (NumberFormatException e) {
            throw new HttpException(
                "Unable to parse status code from status line: '" 
                + statusLine + "'");
        }

        //handle the Reason-Phrase
        at = to + 1;
        try {
            if (at < length) {
                this.reasonPhrase = statusLine.substring(at).trim();
            } else {
                this.reasonPhrase = "";
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new HttpException("Status text not specified: '" 
                    + statusLine + "'");
        }
    }


    // --------------------------------------------------------- Public Methods

    /**
     * @return the Status-Code
     */
    public final int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the HTTP-Version
     */
    public final String getHttpVersion() {
        return httpVersion;
    }

    /**
     * @return the Reason-Phrase
     */
    public final String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Return a string representation of this object.
     * @return a string represenation of this object.
     */
    public final String toString() {
        return statusLine;
    }
}
