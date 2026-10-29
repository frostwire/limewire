/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/util/DateParser.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
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

package org.apache.commons.httpclient.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A utility class for parsing HTTP dates as used in cookies and other headers.  
 * This class handles dates as defined by RFC 2616 section 3.3.1 as well as 
 * some other common non-standard formats.
 * 
 * @author Christopher Brown
 * @author Michael Becke
 */
public class DateParser {

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C 
     * <code>asctime()</code> format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    /** The patterns used for parsing dates */
    private static final String[] DATE_PATTERNS = {
        PATTERN_RFC1123,
        PATTERN_RFC1036,
        PATTERN_ASCTIME,
        "EEE, dd-MMM-yyyy HH:mm:ss z",
        "EEE, dd-MMM-yyyy HH-mm-ss z",
        "EEE, dd MMM yy HH:mm:ss z",
        "EEE dd-MMM-yyyy HH:mm:ss z",
        "EEE dd MMM yyyy HH:mm:ss z",
        "EEE dd-MMM-yyyy HH-mm-ss z",
        "EEE dd-MMM-yy HH:mm:ss z",
        "EEE dd MMM yy HH:mm:ss z",
        "EEE,dd-MMM-yy HH:mm:ss z",
        "EEE,dd-MMM-yyyy HH:mm:ss z",
        "EEE, dd-MM-yyyy HH:mm:ss z",
    };

    /**
     * Parses a date value.
     *
     * @param dateValue the date value to parse
     * 
     * @return the parsed date
     *
     * @throws DateParseException if the value could not be parsed using any of the 
     * supported date formats
     */
    public static Date parseDate(String dateValue) throws DateParseException {
        return parseDate(dateValue, DATE_PATTERNS);
    }
    
    /**
     * Parses the date value using the array of date formats.
     * 
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     * 
     * @return the parsed date
     * 
     * @throws DateParseException if none of the dataFormats could parse the dateValue
     */
    private static Date parseDate(
        String dateValue, 
        String[] dateFormats
    ) throws DateParseException {
        
        if (dateValue == null) {
            throw new IllegalArgumentException("dateValue is null");
        }
        
        SimpleDateFormat dateParser = null;
        
        for (int i = 0; i < dateFormats.length; i++) {
            if (dateParser == null) {
                dateParser = new SimpleDateFormat(dateFormats[i], Locale.US);
                dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
            } else {
                dateParser.applyPattern(dateFormats[i]);                    
            }
            try {
                return dateParser.parse(dateValue);
            } catch (ParseException pe) {
                // ignore this exception, we will try the next format
            }                
        }
        
        // we were unable to parse the date
        throw new DateParseException("Unable to parse the date " + dateValue);        
    }

    /** This class should not be instantiated. */    
    private DateParser() { }
    
}
