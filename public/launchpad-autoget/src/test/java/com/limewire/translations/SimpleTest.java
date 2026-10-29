package com.limewire.translations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import junit.framework.TestCase;

public class SimpleTest extends TestCase {
	
	private final File outDir = new File("rosetta-limewire");
	private final File tarFile = new File("rosetta-limewire.tar");
	private final File outFile = new File(tarFile.getName() + ".gz");
	
	protected void setUp() throws Exception {
		tearDown();
		if (!outDir.exists()) outDir.mkdirs();
	}
	
	protected void tearDown() throws Exception {
		if (outDir.exists())  outDir.delete();
		if (tarFile.exists()) tarFile.delete();
		if (outFile.exists()) {
			File[] fs = outFile.listFiles();
			if (fs != null) {
				for (File f : fs) {
					if (!f.isDirectory()) f.delete();
				}
			}
			outFile.delete();
		}
	}
	
	public void testSimple() throws FileNotFoundException, IOException {
		Properties ps = new Properties();
		final File f = new File("src/main/xml/user.properties");
		if (!f.exists()) fail(f + " must exist");
		
		ps.load(new FileInputStream(f));
		Main m = new Main();
		m.setEmail(ps.getProperty("email"));
		m.setPasswordEmail(ps.getProperty("passwordEmail"));
		m.setUsername(ps.getProperty("username"));
		m.setPassword(ps.getProperty("password"));
		m.go();		
				
		assertTrue(outFile.getAbsolutePath() + " doesn't exist", outFile.exists());
		assertTrue("length is only " + outFile.length(), outFile.length() > 2000000);
		
		GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(outFile));
		BufferedOutputStream tarOut = new BufferedOutputStream(new FileOutputStream(tarFile));
		byte[] buf = new byte[8 * 1028];
		int len;
		while ((len = gzip.read(buf)) != -1) {
			tarOut.write(buf, 0, len);
		}
//		TarInputStream tarIn = new TarInputStream(new FileInputStream(tarFile));
//		int count = 0;
//		while (true) {
//			TarEntry te = tarIn.getNextEntry();
//			if (te == null) break;
//			if (te.getName().equals(outDir.getName() + "/") ||
//					te.getName().equals(outDir.getName() + "\\")) {
//				continue;
//			}
//			assertTrue(te.getSize() > 50);
//			count++;
//		}
//		assertTrue("count is only " + count, count > 30);
		
	}

}
