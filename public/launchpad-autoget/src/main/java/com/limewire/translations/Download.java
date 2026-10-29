package com.limewire.translations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * The page that downloads things.
 */
final class Download extends PageTransition {
	
	private final InputStreamAndCookie inAndC;

	public Download(InputStreamAndCookie inAndC, String userAgent) {
		super(Pages.getInstance().downloadPage(), userAgent);
		this.inAndC = inAndC;
	}

	public InputStream submit(InputStream in) throws IOException {
		Util.note(this, "trying to download get");
		Cookie cookie = inAndC.getCookie();
		Util.note(this, "trying to download post");
		HttpClient client = newClient(cookie);
		PostMethod post = new PostMethod(url());
		final NameValuePair[] data = {
				new NameValuePair("what", "all"),
				new NameValuePair("format", "PO"),
				new NameValuePair("submit", "Request Download"),
		};
		post.addParameters(data);
		client.executeMethod(post);
		if (post.getStatusCode() != 303) {
			throw new IOException("303 expected");
		} else {
			return post.getResponseBodyAsStream();
		}
	}

}
