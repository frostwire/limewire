/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/UsernamePasswordCredentials.java,v 1.1.1.1 2003/10/30 20:32:59 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:59 $
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
 * <p>Username and password {@link Credentials}.</p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Sean C. Sullivan
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @version $Revision: 1.1.1.1 $ $Date: 2003/10/30 20:32:59 $
 * 
 */
public class UsernamePasswordCredentials implements Credentials {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public UsernamePasswordCredentials() {
    }


    /**
     * The constructor with the username and password combined string argument.
     *
     * @param usernamePassword the username:password formed string
     * @see #toString
     */
    public UsernamePasswordCredentials(String usernamePassword) {
        int atColon = usernamePassword.indexOf(':');
        if (atColon >= 0) {
            this.userName = usernamePassword.substring(0, atColon);
            this.password = usernamePassword.substring(atColon + 1);
        } else {
            this.userName = usernamePassword;
        }
    }


    /**
     * The constructor with the username and password arguments.
     *
     * @param userName the user name
     * @param password the password
     */
    public UsernamePasswordCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * User name.
     */
    private String userName;


    /**
     * Password.
     */
    private String password;


    // ------------------------------------------------------------- Properties


    /**
     * User name property setter.
     *
     * @param userName
     * @see #getUserName()
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }


    /**
     * User name property getter.
     *
     * @return the userName
     * @see #setUserName(String)
     */
    public String getUserName() {
        return userName;
    }


    /**
     * Password property setter.
     *
     * @param password
     * @see #getPassword()
     */
    public void setPassword(String password) {
        this.password = password;
    }


    /**
     * Password property getter.
     *
     * @return the password
     * @see #setPassword(String)
     */
    public String getPassword() {
        return password;
    }

    
    /**
     * Get this object string.
     *
     * @return the username:password formed string
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append((this.userName == null) ? "null" : this.userName);
        result.append(":");
        result.append((this.password == null) ? "null" : this.password);
        return result.toString();
    }

}

