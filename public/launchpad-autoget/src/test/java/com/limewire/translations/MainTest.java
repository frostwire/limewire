package com.limewire.translations;

import junit.framework.TestCase;

public class MainTest extends TestCase {

	public void testMainArgumentsAreParsed() {
		Main.main(new String[] { "-passwordEmail", "password" });
	}

}
