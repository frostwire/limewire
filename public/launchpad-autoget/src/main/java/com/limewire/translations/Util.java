package com.limewire.translations;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.httpclient.Cookie;


final class Util {

  private Util() {}
  
	static InputStreamAndCookie login(Object who, String username, String password, String userAgent) throws IOException {
		note(who,"logging in with username: " + username + " and password: " + password);
		note(who, "User-agent: " + userAgent);
		Login login = new Login(username,password,userAgent);
		InputStream in = login.submit();
		Cookie cookie = login.getCookie();
		return new InputStreamAndCookie(in,cookie);
	}

  public static boolean isProperty(String s) {
    return !isEmpty(System.getProperty(s));
  }

  public static boolean isEmpty(String s) {
    return s == null || s.equals("");
  }

  public static void handle(Object who, Throwable e, ErrorCode ec) {
    System.err.println("EXCEPTION: " + who + ":" + e.getMessage());
    e.printStackTrace();
    System.err.println("REASON: " + ec.getMsg());
    System.exit(ec.getCode());
  }

  public static File wget(String url, File f, Progress p) throws Exception {
  	p.setTitle("downloading " + url);
  	final int TOTAL = 2500000; // about 2.5 MBs
  	p.setMax(TOTAL); 
    final byte[] buf = new byte[8 * 1028];
    InputStream in = new BufferedInputStream(new URL(url).openStream());
    OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
    for (int len = 0, total = 0; (len = in.read(buf)) != -1;) {
      out.write(buf,0,len);
      p.update((total += len) + " of about " + TOTAL + " bytes", buf.length);
    }
    out.close();
    in.close();
    return f;
  }

  static void note(Object who, Object msg) {
    System.out.println("[" + who.getClass().getName() + "] " + msg);
  }

	static ProgressPanel open(Object msg) {
	  return ProgressPanel.open(String.valueOf(msg));
	}

	public static String cardinal2ordinal(int n) {
		int r = n % 10;
		String end;
		switch(r) {
		case 1:  end = "st"; break;
		case 2:  end = "nd"; break;
		case 3:  end = "rd"; break;
		default: end = "th";
		}
		return n + end;
	}

}
