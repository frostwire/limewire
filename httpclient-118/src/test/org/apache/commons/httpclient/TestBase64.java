/*
 * $Header: /cvs/httpclient-118/src/test/org/apache/commons/httpclient/TestBase64.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:33:00 $
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import junit.framework.*;
import java.util.Random;
import org.apache.commons.httpclient.util.Base64;

/**
 * Simple tests of {@link Base64}.
 *
 * @deprecated The commons-codec Base64 class will be used in HttpClient 2.1
 * @author Rodney Waldhoff
 * @version $Id: TestBase64.java,v 1.1.1.1 2003/10/30 20:33:00 sberlin Exp $
 */
public class TestBase64 extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestBase64(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestBase64.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestBase64.class);
    }

    public void setUp() throws Exception {
    }

    private Random _rand = new Random();

    // encode/decode random arrays from size 0 to size 11
    public void testEncodeDecodeSmall() {
        for(int i=0;i<12;i++) {
            byte[] data = new byte[i];
            _rand.nextBytes(data);
            byte[] enc =  Base64.encode(data);
            assertTrue("\"" + (HttpConstants.getAsciiString(enc)) + "\" is Base64 data.",Base64.isBase64((HttpConstants.getAsciiString(enc))));
            byte[] data2 = Base64.decode(enc);
            assertTrue(toString(data) + " equals " + toString(data2),isEqual(data,data2));
        }
    }

    // encode/decode a large random array
    public void testEncodeDecodeRandom() {
        for(int i=1;i<5;i++) {
            byte[] data = new byte[_rand.nextInt(10000)+1];
            _rand.nextBytes(data);
            byte[] enc =  Base64.encode(data);
            assertTrue(Base64.isBase64(HttpConstants.getAsciiString(enc)));
            byte[] data2 = Base64.decode(enc);
            assertTrue(isEqual(data,data2));
        }
    }

    public void testSingletons() {
        assertEquals("AA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0 })));
        assertEquals("AQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)1 })));
        assertEquals("Ag==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)2 })));
        assertEquals("Aw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)3 })));
        assertEquals("BA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)4 })));
        assertEquals("BQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)5 })));
        assertEquals("Bg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)6 })));
        assertEquals("Bw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)7 })));
        assertEquals("CA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)8 })));
        assertEquals("CQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)9 })));
        assertEquals("Cg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)10 })));
        assertEquals("Cw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)11 })));
        assertEquals("DA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)12 })));
        assertEquals("DQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)13 })));
        assertEquals("Dg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)14 })));
        assertEquals("Dw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)15 })));
        assertEquals("EA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)16 })));
        assertEquals("EQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)17 })));
        assertEquals("Eg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)18 })));
        assertEquals("Ew==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)19 })));
        assertEquals("FA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)20 })));
        assertEquals("FQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)21 })));
        assertEquals("Fg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)22 })));
        assertEquals("Fw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)23 })));
        assertEquals("GA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)24 })));
        assertEquals("GQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)25 })));
        assertEquals("Gg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)26 })));
        assertEquals("Gw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)27 })));
        assertEquals("HA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)28 })));
        assertEquals("HQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)29 })));
        assertEquals("Hg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)30 })));
        assertEquals("Hw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)31 })));
        assertEquals("IA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)32 })));
        assertEquals("IQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)33 })));
        assertEquals("Ig==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)34 })));
        assertEquals("Iw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)35 })));
        assertEquals("JA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)36 })));
        assertEquals("JQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)37 })));
        assertEquals("Jg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)38 })));
        assertEquals("Jw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)39 })));
        assertEquals("KA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)40 })));
        assertEquals("KQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)41 })));
        assertEquals("Kg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)42 })));
        assertEquals("Kw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)43 })));
        assertEquals("LA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)44 })));
        assertEquals("LQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)45 })));
        assertEquals("Lg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)46 })));
        assertEquals("Lw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)47 })));
        assertEquals("MA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)48 })));
        assertEquals("MQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)49 })));
        assertEquals("Mg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)50 })));
        assertEquals("Mw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)51 })));
        assertEquals("NA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)52 })));
        assertEquals("NQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)53 })));
        assertEquals("Ng==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)54 })));
        assertEquals("Nw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)55 })));
        assertEquals("OA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)56 })));
        assertEquals("OQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)57 })));
        assertEquals("Og==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)58 })));
        assertEquals("Ow==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)59 })));
        assertEquals("PA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)60 })));
        assertEquals("PQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)61 })));
        assertEquals("Pg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)62 })));
        assertEquals("Pw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)63 })));
        assertEquals("QA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)64 })));
        assertEquals("QQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)65 })));
        assertEquals("Qg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)66 })));
        assertEquals("Qw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)67 })));
        assertEquals("RA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)68 })));
        assertEquals("RQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)69 })));
        assertEquals("Rg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)70 })));
        assertEquals("Rw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)71 })));
        assertEquals("SA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)72 })));
        assertEquals("SQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)73 })));
        assertEquals("Sg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)74 })));
        assertEquals("Sw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)75 })));
        assertEquals("TA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)76 })));
        assertEquals("TQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)77 })));
        assertEquals("Tg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)78 })));
        assertEquals("Tw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)79 })));
        assertEquals("UA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)80 })));
        assertEquals("UQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)81 })));
        assertEquals("Ug==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)82 })));
        assertEquals("Uw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)83 })));
        assertEquals("VA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)84 })));
        assertEquals("VQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)85 })));
        assertEquals("Vg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)86 })));
        assertEquals("Vw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)87 })));
        assertEquals("WA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)88 })));
        assertEquals("WQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)89 })));
        assertEquals("Wg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)90 })));
        assertEquals("Ww==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)91 })));
        assertEquals("XA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)92 })));
        assertEquals("XQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)93 })));
        assertEquals("Xg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)94 })));
        assertEquals("Xw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)95 })));
        assertEquals("YA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)96 })));
        assertEquals("YQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)97 })));
        assertEquals("Yg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)98 })));
        assertEquals("Yw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)99 })));
        assertEquals("ZA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)100 })));
        assertEquals("ZQ==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)101 })));
        assertEquals("Zg==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)102 })));
        assertEquals("Zw==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)103 })));
        assertEquals("aA==",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)104 })));
    }

    public void testTriplets() {
        assertEquals("AAAA",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)0 })));
        assertEquals("AAAB",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)1 })));
        assertEquals("AAAC",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)2 })));
        assertEquals("AAAD",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)3 })));
        assertEquals("AAAE",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)4 })));
        assertEquals("AAAF",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)5 })));
        assertEquals("AAAG",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)6 })));
        assertEquals("AAAH",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)7 })));
        assertEquals("AAAI",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)8 })));
        assertEquals("AAAJ",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)9 })));
        assertEquals("AAAK",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)10 })));
        assertEquals("AAAL",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)11 })));
        assertEquals("AAAM",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)12 })));
        assertEquals("AAAN",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)13 })));
        assertEquals("AAAO",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)14 })));
        assertEquals("AAAP",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)15 })));
        assertEquals("AAAQ",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)16 })));
        assertEquals("AAAR",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)17 })));
        assertEquals("AAAS",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)18 })));
        assertEquals("AAAT",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)19 })));
        assertEquals("AAAU",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)20 })));
        assertEquals("AAAV",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)21 })));
        assertEquals("AAAW",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)22 })));
        assertEquals("AAAX",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)23 })));
        assertEquals("AAAY",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)24 })));
        assertEquals("AAAZ",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)25 })));
        assertEquals("AAAa",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)26 })));
        assertEquals("AAAb",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)27 })));
        assertEquals("AAAc",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)28 })));
        assertEquals("AAAd",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)29 })));
        assertEquals("AAAe",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)30 })));
        assertEquals("AAAf",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)31 })));
        assertEquals("AAAg",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)32 })));
        assertEquals("AAAh",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)33 })));
        assertEquals("AAAi",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)34 })));
        assertEquals("AAAj",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)35 })));
        assertEquals("AAAk",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)36 })));
        assertEquals("AAAl",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)37 })));
        assertEquals("AAAm",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)38 })));
        assertEquals("AAAn",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)39 })));
        assertEquals("AAAo",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)40 })));
        assertEquals("AAAp",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)41 })));
        assertEquals("AAAq",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)42 })));
        assertEquals("AAAr",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)43 })));
        assertEquals("AAAs",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)44 })));
        assertEquals("AAAt",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)45 })));
        assertEquals("AAAu",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)46 })));
        assertEquals("AAAv",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)47 })));
        assertEquals("AAAw",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)48 })));
        assertEquals("AAAx",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)49 })));
        assertEquals("AAAy",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)50 })));
        assertEquals("AAAz",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)51 })));
        assertEquals("AAA0",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)52 })));
        assertEquals("AAA1",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)53 })));
        assertEquals("AAA2",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)54 })));
        assertEquals("AAA3",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)55 })));
        assertEquals("AAA4",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)56 })));
        assertEquals("AAA5",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)57 })));
        assertEquals("AAA6",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)58 })));
        assertEquals("AAA7",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)59 })));
        assertEquals("AAA8",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)60 })));
        assertEquals("AAA9",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)61 })));
        assertEquals("AAA+",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)62 })));
        assertEquals("AAA/",HttpConstants.getAsciiString(Base64.encode(new byte[] { (byte)0, (byte)0, (byte)63 })));
    }

    public void testKnownEncodings() {
        assertEquals("VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==",HttpConstants.getAsciiString(Base64.encode(HttpConstants.getAsciiBytes("The quick brown fox jumped over the lazy dogs."))));
        assertEquals("SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==",HttpConstants.getAsciiString(Base64.encode(HttpConstants.getAsciiBytes("It was the best of times, it was the worst of times."))));
        assertEquals("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==",HttpConstants.getAsciiString(Base64.encode(HttpConstants.getAsciiBytes("http://jakarta.apache.org/commmons"))));
        assertEquals("QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==",HttpConstants.getAsciiString(Base64.encode(HttpConstants.getAsciiBytes("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"))));
        assertEquals("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0=",HttpConstants.getAsciiString(Base64.encode(HttpConstants.getAsciiBytes("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }"))));
        assertEquals("eHl6enkh",HttpConstants.getAsciiString(Base64.encode(HttpConstants.getAsciiBytes("xyzzy!"))));
    }

    public void testKnownDecodings() {
        assertEquals("The quick brown fox jumped over the lazy dogs.",HttpConstants.getAsciiString(Base64.decode(HttpConstants.getAsciiBytes("VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg=="))));
        assertEquals("It was the best of times, it was the worst of times.",HttpConstants.getAsciiString(Base64.decode(HttpConstants.getAsciiBytes("SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg=="))));
        assertEquals("http://jakarta.apache.org/commmons",HttpConstants.getAsciiString(Base64.decode(HttpConstants.getAsciiBytes("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw=="))));
        assertEquals("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz",HttpConstants.getAsciiString(Base64.decode(HttpConstants.getAsciiBytes("QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg=="))));
        assertEquals("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }",HttpConstants.getAsciiString(Base64.decode(HttpConstants.getAsciiBytes("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0="))));
        assertEquals("xyzzy!",HttpConstants.getAsciiString(Base64.decode(HttpConstants.getAsciiBytes("eHl6enkh"))));
    }

    // -------------------------------------------------------- Private Methods
    private boolean isEqual(byte[] a, byte[] b) {
        if(a.length != b.length) { return false; }
        for(int i=0;i<a.length;i++) {
            if(a[i] != b[i]) { return false; }
        }
        return true;
    }

    private String toString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<data.length;i++) {
            buf.append(data[i]);
            if(i != data.length-1) {
                buf.append(",");
            }
        }
        return buf.toString();
    }
}
