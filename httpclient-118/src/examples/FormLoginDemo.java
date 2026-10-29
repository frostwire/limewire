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

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.*;

/**
 * <p>
 * A example that demonstrates how HttpClient APIs can be used to perform 
 * form-based logon.
 * </p>
 *
 * @author Oleg Kalnichevski
 *
 */
public class FormLoginDemo
{
    static final String LOGON_SITE = "developer.java.sun.com";
    static final int    LOGON_PORT = 80;

    public FormLoginDemo() {
        super();
    }

    public static void main(String[] args) throws Exception {

        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(LOGON_SITE, LOGON_PORT, "http");
        client.getState().setCookiePolicy(CookiePolicy.COMPATIBILITY);
     	// 'developer.java.sun.com' has cookie compliance problems
        // Their session cookie's domain attribute is in violation of the RFC2109
        // We have to resort to using compatibility cookie policy

        GetMethod authget = new GetMethod("/servlet/SessionServlet");

        client.executeMethod(authget);
        System.out.println("Login form get: " + authget.getStatusLine().toString()); 
        // release any connection resources used by the method
        authget.releaseConnection();
        // See if we got any cookies
        Cookie[] initcookies = 
          client.getState().getCookies(LOGON_SITE, LOGON_PORT, "/", false);
        System.out.println("Initial set of cookies:");    
        if (initcookies.length == 0) {
            System.out.println("None");    
        } else {
            for (int i = 0; i < initcookies.length; i++) {
                System.out.println("- " + initcookies[i].toString());    
            }
        }
        
        PostMethod authpost = new PostMethod("/servlet/SessionServlet");
        // Prepare login parameters
        NameValuePair action   = new NameValuePair("action", "login");
        NameValuePair url      = new NameValuePair("url", "/index.html");
        NameValuePair userid   = new NameValuePair("UserId", "userid");
        NameValuePair password = new NameValuePair("Password", "password");
        authpost.setRequestBody( 
          new NameValuePair[] {action, url, userid, password});
        
        client.executeMethod(authpost);
        System.out.println("Login form post: " + authpost.getStatusLine().toString()); 
        // release any connection resources used by the method
        authpost.releaseConnection();
        // See if we got any cookies
        // The only way of telling whether logon succeeded is 
        // by finding a session cookie
        Cookie[] logoncookies = 
          client.getState().getCookies(LOGON_SITE, LOGON_PORT, "/", false);
        System.out.println("Logon cookies:");    
        if (logoncookies.length == 0) {
            System.out.println("None");    
        } else {
            for (int i = 0; i < logoncookies.length; i++) {
                System.out.println("- " + logoncookies[i].toString());    
            }
        }
        // Usually a successful form-based login results in a redicrect to 
        // another url
        int statuscode = authpost.getStatusCode();
        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
            (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
            (statuscode == HttpStatus.SC_SEE_OTHER) ||
            (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            Header header = authpost.getResponseHeader("location");
            if (header != null) {
                String newuri = header.getValue();
                if ((newuri == null) || (newuri.equals(""))) {
                    newuri = "/";
                }
                System.out.println("Redirect target: " + newuri); 
                GetMethod redirect = new GetMethod(newuri);

                client.executeMethod(redirect);
                System.out.println("Redirect: " + redirect.getStatusLine().toString()); 
                // release any connection resources used by the method
                redirect.releaseConnection();
            } else {
                System.out.println("Invalid redirect");
                System.exit(1);
            }
        }
    }
}
