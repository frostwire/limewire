/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/Cookie.java,v 1.3 2004/08/23 17:38:35 sberlin Exp $
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

package org.apache.commons.httpclient;

import java.io.Serializable;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>An HTTP "magic-cookie", as specified in RFC 2109.</p>
 * 
 * @author B.C. Holmes
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:dsale@us.britannica.com">Doug Sale</a>
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author Sean C. Sullivan
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @version $Revision: 1.3 $ $Date: 2004/08/23 17:38:35 $
 */

public class Cookie extends NameValuePair implements Serializable, Comparator {

    // ----------------------------------------------------------- Constructors

    /**
     * Create a cookie. Default constructor 
     * 
     * The new cookie is assigned 
     */

    public Cookie() {
        this(null, "noname", null, null, null, false);
    }

    /**
     * Create a cookie.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the host this cookie will be sent to
     */
    public Cookie(String domain, String name, String value) {
        this(domain, name, value, null, null, false);
    }

    /**
     * Create a cookie.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the host this cookie will be sent to
     * @param path    the path prefix for which this cookie will be sent
     * @param expires the {@link Date} at which this cookie expires,
     *                or <tt>null</tt> if the cookie expires at the end
     *                of the session
     * @param secure if true this cookie will only be sent over secure
     * connections
     */
    public Cookie(String domain, String name, String value, 
        String path, Date expires, boolean secure) {
            
        super(name, value);
        LOG.trace("enter Cookie(String, String, String, String, Date, boolean)");
        if (name == null) {
            throw new IllegalArgumentException("Cookie name may not be null");
        }
        if (name.equals("")) {
            throw new IllegalArgumentException("Cookie name may not be blank");
        }
        if (name.indexOf(' ') != -1) {
            throw new IllegalArgumentException("Cookie name may not contain blanks");
        }
        if (name.startsWith("$")) {
            throw new IllegalArgumentException("Cookie name may not start with $");
        }
        this.setPath(path);
        this.setDomain(domain);
        this.setExpiryDate(expires);
        this.setSecure(secure);
    }

    /**
     * Create a cookie.
     *
     * @param name   the cookie name
     * @param value  the cookie value
     * @param domain the host this cookie will be sent to
     * @param path   the path prefix for which this cookie will be sent
     * @param maxAge the number of seconds for which this cookie is valid.
     *               maxAge is expected to be a non-negative number. 
     *               <tt>-1</tt> signifies that the cookie should never expire.
     * @param secure if <tt>true</tt> this cookie will only be sent over secure
     * connections
     */
    public Cookie(String domain, String name, String value, String path, 
        int maxAge, boolean secure) {
            
        this(domain, name, value, path, null, secure);
        if (maxAge < -1) {
            throw new IllegalArgumentException("Invalid max age:  " + Integer.toString(maxAge));
        }            
        if (maxAge >= 0) {
            setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
        }
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * <tt>null</tt> if no such comment has been defined.
     *
     * @see #setComment(String)
     */
    public String getComment() {
        return cookieComment;
    }

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     *
     * @see #getComment()
     */
    public void setComment(String comment) {
        cookieComment = comment;
    }

    /**
     * Returns my expiration {@link Date}, or <tt>null</tt>
     * if none exists.
     * <p><strong>Note:</strong> the object returned by this method is 
     * considered immutable. Changing it (e.g. using setTime()) could result
     * in undefined behaviour. Do so at your peril. </p>
     * @return my expiration {@link Date}, or <tt>null</tt>.
     *
     * @see #setExpiryDate(java.util.Date)
     *
     */
    public Date getExpiryDate() {
        return cookieExpiryDate;
    }

    /**
     * Expiration setter.
     * <p>
     * Netscape's original proposal defined an Expires header that took
     * a date value in a fixed-length variant format in place of Max-Age:
     * <br>
     * <tt>Wdy, DD-Mon-YY HH:MM:SS GMT</tt>
     * <br>
     * Note that the Expires date format contains embedded spaces, and that
     * "old" cookies did not have quotes around values.  Clients that
     * implement to this specification should be aware of "old" cookies and
     * Expires.
     * </p>
     * <p><strong>Note:</strong> the object returned by this method is considered
     * immutable. Changing it (e.g. using setTime()) could result in undefined 
     * behaviour. Do so at your peril.</p>
     *
     * @param expiryDate the {@link Date} after which this cookie is no longer valid.
     *
     * @see #getExpiryDate
     *
     */
    public void setExpiryDate (Date expiryDate) {
        cookieExpiryDate = expiryDate;
    }


    /**
     * Returns <tt>false</tt> if I should be discarded at the end
     * of the "session"; <tt>true</tt> otherwise.
     *
     * @return <tt>false</tt> if I should be discarded at the end
     *         of the "session"; <tt>true</tt> otherwise
     */
    public boolean isPersistent() {
        return (null != cookieExpiryDate);
    }


    /**
     * Returns my domain.
     *
     * @see #setDomain(java.lang.String)
     */
    public String getDomain() {
        return cookieDomain;
    }

    /**
     * Sets my domain.
     * <p>
     * I should be presented only to hosts satisfying this domain
     * name pattern.  Read RFC 2109 for specific details of the syntax.
     * Briefly, a domain name name begins with a dot (".foo.com") and means
     * that hosts in that DNS zone ("www.foo.com", but not "a.b.foo.com")
     * should see the cookie.  By default, cookies are only returned to
     * the host which saved them.
     *
     * @see #getDomain
     */
    public void setDomain(String domain) {
        if (domain != null) {
            int ndx = domain.indexOf(":");
            if (ndx != -1) {
              domain = domain.substring(0, ndx);
            }
            cookieDomain = domain.toLowerCase();
        }
    }


    /**
     * @return my path.
     * @see #setPath(java.lang.String)
     */
    public String getPath() {
        return cookiePath;
    }

    /**
     * Sets my path.
     * <p>
     * I should be presented only with requests beginning with this path.
     * See RFC 2109 for a specification of the default behaviour. Basically, URLs
     * in the same "directory" as the one which set the cookie, and in subdirectories,
     * can all see the cookie unless a different path is set.</p>
     *
     * @see #getPath
     *
     */
    public void setPath(String path) {
        cookiePath = path;
    }

    /**
     * @return <code>true</code> if this cookie should only be sent over secure connections.
     * @see #setSecure(boolean)
     */
    public boolean getSecure() {
        return isSecure;
    }

    /**
     * Set my secure flag.
     * <p>
     * When <tt>true</tt> the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @see #getSecure()
     */
    public void setSecure (boolean secure) {
        isSecure = secure;
    }

    /**
     *
     * @return the version of the HTTP cookie specification that I use.
     * 
     * @see #setVersion(int)
     *
     */
    public int getVersion() {
        return cookieVersion;
    }

    /**
     * Set the version of the HTTP cookie specification I report.
     * <p>
     * The current implementation only sends version 1 cookies.
     * (See RFC 2109 for details.)</p>
     *
     * @see #getVersion
     *
     */
    public void setVersion(int version) {
        cookieVersion = version;
    }

    /**
     * Return true if this cookie has expired.
     * @return <tt>true</tt> if I have expired.
     */
    public boolean isExpired() {
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= System.currentTimeMillis());
    }

    /**
     * Return true if this cookie has expired according to the time passed in.
     * @param now The current time.
     * @return <tt>true</tt> if I have expired.
     */
    public boolean isExpired(Date now) {
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= now.getTime());
    }


    /**
     * Indicates whether the cookie had a path specified in a 
     * Path attribute in the set-cookie header.  This value
     * is important for generating the cookie header because 
     * RFC 2109 sec. 4.3.4 says that the cookie header should only 
     * include a $Path attribute if the cookie's path was specified
     * in the set-cookie header.
     *
     * @see #isPathAttributeSpecified
     * @param value True if the cookie's path came from a Path attribute.
     */
    public void setPathAttributeSpecified(boolean value) {
        hasPathAttribute = value;
    }

    /**
     * Returns true if cookie's path was set via a Path attribute in the
     * set-cookie header.
     *
     * @see #setPathAttributeSpecified
     * @return True if cookie's path was specified in the set-cookie header.
     */
    public boolean isPathAttributeSpecified() {
        return hasPathAttribute;
    }

    /**
     * Indicates whether the cookie had a domain specified in a 
     * Domain attribute in the set-cookie header.  This value
     * is important for generating the cookie header because 
     * RFC 2109 sec. 4.3.4 says that the cookie header should only 
     * include a $Domain attribute if the cookie's domain was specified
     * in the set-cookie header.
     *
     * @see #isDomainAttributeSpecified
     * @param value True if the cookie's domain came from a Domain attribute.
     */
    public void setDomainAttributeSpecified(boolean value) {
        hasDomainAttribute = value;
    }

    /**
     * Returns true if cookie's domain was set via a Domain attribute in the
     * set-cookie header.
     *
     * @see #setDomainAttributeSpecified
     * @return True if cookie's domain was specified in the set-cookie header.
     */
    public boolean isDomainAttributeSpecified() {
        return hasDomainAttribute;
    }

    /**
     * Returns a hash code in keeping with the
     * {@link Object#hashCode} general hashCode contract.
     * @return A hash code
     */
    public int hashCode() {
        return super.hashCode()
            ^ (null == cookiePath ? 0 : cookiePath.hashCode())
            ^ (null == cookieDomain ? 0 : cookieDomain.hashCode());
    }


    /**
     * Two cookies are equal if the name, path and domain match.
     * @param obj The object to compare against.
     * @return true if the two objects are equal.
     */
    public boolean equals(Object obj) {
        LOG.trace("enter Cookie.equals(Object)");
        
        if ((obj != null) && (obj instanceof Cookie)) {
            Cookie that = (Cookie) obj;
            return 
                (null == this.getName() 
                    ? null == that.getName() 
                    : this.getName().equals(that.getName())) 
                && (null == this.getPath() 
                    ? null == that.getPath() 
                    : this.getPath().equals(that.getPath())) 
                && (null == this.getDomain() 
                    ? null == that.getDomain() 
                    : this.getDomain().equals(that.getDomain()));
        } else {
            return false;
        }
    }


    /**
     * Return a string suitable for sending in a Cookie header.
     * @return a string suitable for sending in a Cookie header.
     */
    public String toExternalForm() {
        return CookiePolicy.getSpecByVersion(
            getVersion()).formatCookie(this);
    }

    /**
     * Return <tt>true</tt> if I should be submitted with a request with given
     * attributes, <tt>false</tt> otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currently
     * ignored)
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using the HTTPS protocol
     * @param date the time at which the request is submitted
     * @return true if the cookie matches
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public boolean matches(
        String domain, int port, String path, boolean secure, Date date) {
            
        LOG.trace("enter Cookie.matches(Strinng, int, String, boolean, Date");
        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        return matcher.match(domain, port, path, secure, this);
    }

    /**
     * Return <tt>true</tt> if I should be submitted with a request with given
     * attributes, <tt>false</tt> otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currently
     * ignored)
     * @param path the path to which the request is being submitted
     * @param secure True if this cookie has the secure flag set
     * @return true if I should be submitted as above.
     * @deprecated use {@link CookieSpec} interface
     */
    public boolean matches(
        String domain, int port, String path, boolean secure) {
        LOG.trace("enter Cookie.matches(String, int, String, boolean");
        return matches(domain, port, path, secure, new Date());
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i> and
     * <i>path</i>, assuming the connection is not
     * secure.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param path The path
     * @param cookies The cookies to use
     * @return The new header.
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, String path, 
        Cookie[] cookies) {
            
        LOG.trace("enter Cookie.createCookieHeader(String,String,Cookie[])");
        return Cookie.createCookieHeader(domain, path, false, cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>path</i> and
     * <i>https</i> setting.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param cookies The cookies to use.
     * @return The new header
     * @exception IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, String path, 
        boolean secure, Cookie[] cookies)
        throws IllegalArgumentException {
            
        LOG.trace("enter Cookie.createCookieHeader("
            + "String, String, boolean, Cookie[])");

        // Make sure domain isn't null here.  Path will be validated in 
        // subsequent call to createCookieHeader
        if (domain == null) {
            throw new IllegalArgumentException("null domain in "
                + "createCookieHeader.");
        }
        // parse port from domain, if any
        int port = secure ? 443 : 80;
        int ndx = domain.indexOf(":");
        if (ndx != -1) {
            try {
                port = Integer.parseInt(domain.substring(ndx + 1, 
                    domain.length()));
            } catch (NumberFormatException e) {
                // ignore?, but at least LOG
                LOG.warn("Cookie.createCookieHeader():  "
                    + "Invalid port number in domain " + domain);
            }
        }
        return Cookie.createCookieHeader(domain, port, path, secure, cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>port</i>,
     * <i>path</i> and <i>https</i> setting.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param port The port
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param cookies The cookies to use.
     * @return The new header
     * @throws IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, int port, 
        String path, boolean secure, Cookie[] cookies) 
        throws IllegalArgumentException {
        LOG.trace("enter Cookie.createCookieHeader(String, int, String, boolean, Cookie[])");
        return Cookie.createCookieHeader(domain, port, path, secure, new Date(), cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing all cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>port</i>, <i>path</i> and
     * <i>https</i> setting, and which are not expired according to the given
     * <i>date</i>.
     * <p>
     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param port The port
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param now The date to check for expiry
     * @param cookies The cookies to use.
     * @return The new header
     * @throws IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */

    public static Header createCookieHeader(
        String domain, int port, String path, boolean secure, 
        Date now, Cookie[] cookies) 
        throws IllegalArgumentException {
            
        LOG.trace("enter Cookie.createCookieHeader(String, int, String, boolean, Date, Cookie[])");
        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        cookies = matcher.match(domain, port, path, secure, cookies);
        if ((cookies != null) && (cookies.length > 0)) {
            return matcher.formatCookieHeader(cookies);
        } else {
            return null;
        } 
    }

    /**
     * <p>Compares two cookies to determine order for cookie header.</p>
     * <p>Most specific should be first. </p>
     * <p>This method is implemented so a cookie can be used as a comparator for
     * a SortedSet of cookies. Specifically it's used above in the 
     * createCookieHeader method.</p>
     * <p>The compare only compares the path of the cookie, see section 4.3.4 
     * of RFC2109</p>
     * @param o1 The first object to be compared
     * @param o2 The second object to be compared
     * @return See {@link java.util.Comparator#compare(Object,Object)}
     */
    public int compare(Object o1, Object o2) {
        LOG.trace("enter Cookie.compare(Object, Object)");

        if (!(o1 instanceof Cookie)) {
            throw new ClassCastException(o1.getClass().getName());
        }
        if (!(o2 instanceof Cookie)) {
            throw new ClassCastException(o2.getClass().getName());
        }
        Cookie c1 = (Cookie) o1;
        Cookie c2 = (Cookie) o2;
        if (c1.getPath() == null && c2.getPath() == null) {
            return 0;
        } else if (c1.getPath() == null) {
            // null is assumed to be "/"
            if (c2.getPath().equals(CookieSpec.PATH_DELIM)) {
                return 0;
            } else {
                return -1;
            }
        } else if (c2.getPath() == null) {
            // null is assumed to be "/"
            if (c1.getPath().equals(CookieSpec.PATH_DELIM)) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return STRING_COLLATOR.compare(c1.getPath(), c2.getPath());
        }
    }

    /**
     * Return a {@link String} representation of me.
     * @see #toExternalForm
     */
    public String toString() {
        return toExternalForm();
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param port the port from which the {@link Header} was received
     * (currently ignored)
     * @param path the path from which the {@link Header} was received
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the
     * server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(
        String domain, int port, String path, Header setCookie) 
        throws HttpException, IllegalArgumentException {
            
        LOG.trace("enter Cookie.parse(String, int, String, Header)");
        return Cookie.parse(domain, port, path, false, setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the
     * server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(String domain, String path, Header setCookie) 
    throws HttpException, IllegalArgumentException {
        LOG.trace("enter Cookie.parse(String, String, Header)");
        return Cookie.parse (domain, 80, path, false, setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param secure <tt>true</tt> when the header was recieved over a secure
     * channel
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the
     * server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(String domain, String path, 
        boolean secure, Header setCookie) 
        throws HttpException, IllegalArgumentException {
            
        LOG.trace ("enter Cookie.parse(String, String, boolean, Header)");
        return Cookie.parse (
            domain, (secure ? 443 : 80), path, secure, setCookie);
    }

    /**
      * Parses the Set-Cookie {@link Header} into an array of
      * <tt>Cookie</tt>s.
      *
      * <P>The syntax for the Set-Cookie response header is:
      *
      * <PRE>
      * set-cookie      =    "Set-Cookie:" cookies
      * cookies         =    1#cookie
      * cookie          =    NAME "=" VALUE * (";" cookie-av)
      * NAME            =    attr
      * VALUE           =    value
      * cookie-av       =    "Comment" "=" value
      *                 |    "Domain" "=" value
      *                 |    "Max-Age" "=" value
      *                 |    "Path" "=" value
      *                 |    "Secure"
      *                 |    "Version" "=" 1*DIGIT
      * </PRE>
      *
      * @param domain the domain from which the {@link Header} was received
      * @param port The port from which the {@link Header} was received.
      * @param path the path from which the {@link Header} was received
      * @param secure <tt>true</tt> when the {@link Header} was received over
      * HTTPS
      * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from
      * the server
      * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link
      * Header}
      * @throws HttpException if an exception occurs during parsing
      * 
      * @deprecated use {@link CookieSpec} interface
      */
    public static Cookie[] parse(String domain, int port, String path, 
        boolean secure, Header setCookie) 
        throws HttpException {
            
        LOG.trace("enter Cookie.parse(String, int, String, boolean, Header)");

        CookieSpec parser = CookiePolicy.getDefaultSpec();
        Cookie[] cookies = parser.parse(domain, port, path, secure, setCookie);

        for (int i = 0; i < cookies.length; i++) {
            final Cookie cookie = cookies[i];
            final CookieSpec validator 
                = CookiePolicy.getSpecByVersion(cookie.getVersion());
            validator.validate(domain, port, path, secure, cookie);
        }
        return cookies;
    }

   // ----------------------------------------------------- Instance Variables

   /** My comment. */
   private String  cookieComment;

   /** My domain. */
   private String  cookieDomain;

   /** My expiration {@link Date}. */
   private Date    cookieExpiryDate;

   /** My path. */
   private String  cookiePath;

   /** My secure flag. */
   private boolean isSecure;

   /**
    * Specifies if the set-cookie header included a Path attribute for this
    * cookie
    */
   private boolean hasPathAttribute = false;

   /**
    * Specifies if the set-cookie header included a Domain attribute for this
    * cookie
    */
   private boolean hasDomainAttribute = false;

   /** The version of the cookie specification I was created from. */
   private int     cookieVersion = 0;

   // -------------------------------------------------------------- Constants

   /** 
    * Collator for Cookie comparisons.  Could be replaced with references to
    * specific Locales.
    */
   private static final RuleBasedCollator STRING_COLLATOR =
        (RuleBasedCollator) RuleBasedCollator.getInstance(
                                                new Locale("en", "US", ""));

   /** Log object for this class */
   private static final Log LOG = LogFactory.getLog(Cookie.class);

}

