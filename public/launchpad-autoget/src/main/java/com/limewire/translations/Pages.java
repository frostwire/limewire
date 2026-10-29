package com.limewire.translations;

/**
 * A place to keep URLs.
 */
final class Pages {
	
	public final static String DOMAIN = "translations.launchpad.net";
	private final String group;
	private final String base;
	private static Pages instance;
	
	public static Pages getInstance() {
		return instance;
	}
	
	/**
	 * This needs to be called to set everything up.
	 * 
	 * @param group group name, e.g. <code>limewire</code>
	 * @return the new instance
	 */
	static Pages newInstance(String group) {
		return instance = new Pages(group);
	}
	
	private Pages(String group) {
		this.group = group;
		this.base = "https://" + DOMAIN + "/" + group + "/";
	}

	public String loginPage() {
		return base + "+login";
	}

	public String downloadPage() {
    return base + "trunk/+pots/" + group + "/+export";
	}

	public String mainURL() {
		return base;
	}
		
}
