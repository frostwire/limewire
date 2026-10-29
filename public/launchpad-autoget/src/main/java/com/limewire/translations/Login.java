package com.limewire.translations;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Page for logging in and retrieving the cookie we'll use.
 */
final class Login extends PageTransition {
	
	private final String username;
	private final String password;
	
	private Cookie cookie;

	public Login(String username, String password, String userAgent) {
		super(Pages.getInstance().loginPage(), userAgent);
		Util.note(this, "logging in with username:password:" + username + ":" + password);
		this.username = username;
		this.password = password;
	}
	
	public Cookie getCookie() {
		return cookie;
	}

	public InputStream submit(InputStream in) throws IOException {
		
		HttpClient client;
		
		// First set any cookie necessary
		note("getting the cookie");
		client = newClient(null);
		GetMethod get = new GetMethod(Pages.getInstance().mainURL());
		client.executeMethod(get);
		cookie = getCookie(get);
		
		note("trying to login");
		client = newClient(cookie);
		PostMethod post = new PostMethod(url());
		
		final NameValuePair[] data = {
				new NameValuePair("loginpage_email", 				username),
				new NameValuePair("loginpage_password", 		password),
				new NameValuePair("loginpage_submit_login", "Log In"),
		};
		post.addParameters(data);
		client.executeMethod(post);
		cookie = getCookie(post);
		return post.getResponseBodyAsStream();
	}
	
	public Cookie getCookie(HttpMethod method) {
		final Map<String,String> cookieNames2values = new HashMap<String,String>();
		String path = null;
		for (Header h : method.getResponseHeaders()) {
			String s = h.getName();
			if (!s.equalsIgnoreCase("set-cookie")) continue;
			String extForm = h.toExternalForm();
			int icolon = extForm.indexOf(':');
			String rest = icolon == -1 ? extForm : extForm.substring(icolon+1);
			for (StringTokenizer st = new StringTokenizer(rest, ";", false); st.hasMoreTokens();) {
				String pair = st.nextToken().trim();
				int ieq = pair.indexOf('=');
				String name, value;
				if (ieq == -1) {
					name = pair;
					value = null;
				} else {
					name = pair.substring(0,ieq);
					value = pair.substring(ieq+1);
				}
				if (name.equalsIgnoreCase("path")) path = value;
				cookieNames2values.put(name, value);
			}
		}
		if (!cookieNames2values.isEmpty()) {
			final StringBuffer cookieStr = new StringBuffer();
			String domain = null;
			for (String name : cookieNames2values.keySet()) {
				String value = cookieNames2values.get(name);
				if (cookieStr.length() > 0) cookieStr.append(";");
				cookieStr.append(name);
				if (value != null) cookieStr.append('=').append(value);
				note("cookie: " + name + "=" + value);
				if (name.equalsIgnoreCase("Domain")) domain = value;
			}    	
			Cookie c = new Cookie(domain, "lp", cookieStr.toString());
			if (path != null) c.setPath(path);
			note("cookie-path   : " + path);
			note("cookie-domain : " + domain);
			note("cookie: " + c);
			return c;
		}
		return null;
	}
}