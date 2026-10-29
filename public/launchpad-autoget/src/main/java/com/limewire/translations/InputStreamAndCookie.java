package com.limewire.translations;

import java.io.InputStream;

import org.apache.commons.httpclient.Cookie;

/**
 * For passing back the result of a look up with Cookie data.
 */
final class InputStreamAndCookie {

	private final InputStream inputStream;
	private final Cookie cookie;
	
	InputStreamAndCookie(InputStream in, Cookie cookie) {
		this.inputStream = in;
		this.cookie = cookie;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public Cookie getCookie() {
		return cookie;
	}
}