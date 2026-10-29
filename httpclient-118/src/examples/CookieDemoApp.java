/*
 * $Header: /cvs/httpclient-118/src/examples/CookieDemoApp.java,v 1.1.1.1 2003/10/30 20:32:58 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:58 $
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
 * 4. The names "The Jakarta Project", "HttpClient", and "Apache Software
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * This is a sample application that demonstrates
 * how to use the Jakarta HttpClient API.
 *
 * This application sets an HTTP cookie and
 * updates the cookie's value across multiple
 * HTTP GET requests.
 *
 * @author Sean C. Sullivan
 * @author Oleg Kalnichevski
 *
 */
public class CookieDemoApp {

    /**
     *
     * Usage:
     *          java CookieDemoApp http://mywebserver:80/
     *
     *  @param args command line arguments
     *                 Argument 0 is a URL to a web server
     *
     *
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java CookieDemoApp <url>");
            System.err.println("<url> The url of a webpage");
            System.exit(1);
        }
        // Get target URL
        String strURL = args[0];
        System.out.println("Target URL: " + strURL);

        // Get initial state object
        HttpState initialState = new HttpState();
        // Initial set of cookies can be retrieved from persistent storage and 
        // re-created, using a persistence mechanism of choice,
        Cookie mycookie = new Cookie(".foobar.com", "mycookie", "stuff", "/", null, false);
        // and then added to your HTTP state instance
        initialState.addCookie(mycookie);
        // RFC 2101 cookie management spec is used per default
        // to parse, validate, format & match cookies
        initialState.setCookiePolicy(CookiePolicy.RFC2109);
        // A different cookie management spec can be selected
        // when desired

//      initialState.setCookiePolicy(CookiePolicy.NETSCAPE_DRAFT);
        // Netscape Cookie Draft spec is provided for completeness
        // You would hardly want to use this spec in real life situations
//      initialState.setCookiePolicy(CookiePolicy.COMPATIBILITY);
        // Compatibility policy is provided in order to mimic cookie
        // management of popular web browsers that is in some areas 
        // not 100% standards compliant

        // Get HTTP client instance
        HttpClient httpclient = new HttpClient();
        httpclient.setConnectionTimeout(30000);
        httpclient.setState(initialState);
        // Get HTTP GET method
        GetMethod httpget = new GetMethod(strURL);
        // Execute HTTP GET
        int result = httpclient.executeMethod(httpget);
        // Display status code
        System.out.println("Response status code: " + result);
        // Get all the cookies
        Cookie[] cookies = httpclient.getState().getCookies();
        // Display the cookies
        System.out.println("Present cookies: ");
        for (int i = 0; i < cookies.length; i++) {
            System.out.println(" - " + cookies[i].toExternalForm());
        }
        // Release current connection to the connection pool once you are done
        httpget.releaseConnection();
    }
}
