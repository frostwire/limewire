package com.limewire.translations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;

/**
 * This represents on page load. It has a URL and a User-Agent, so we can keep
 * the same User-Agent over the lifetime of the program, but change it for every
 * run.
 */
abstract class PageTransition {
	
	private final String url;
	private final String userAgent;

	PageTransition(String url, String userAgent) {
		this.url = url;
		this.userAgent = userAgent;
		Util.note(this, "URL:" + url);
	}
	
	public abstract InputStream submit(InputStream in) throws IOException;
	
	public final InputStream submit() throws IOException {
		return submit(null);
	}
	
	final String url() {
		return url;
	}
		
	protected final HttpClient newClient(Cookie cookie) {
		final HttpClient res = new HttpClient();
		final HttpClientParams params = new HttpClientParams();
		params.setParameter("User-agent", userAgent);
		res.setParams(params);
		if (cookie != null) {
			res.getState().addCookie(cookie);
		}
		return res;
	}
	
	protected final void note(String msg) {
		Util.note(this, msg);
	}
}
