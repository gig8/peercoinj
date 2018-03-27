/*
 * Copyright 2012, 2014 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.gig8.motacoinj.uri;

import com.gig8.motacoinj.core.Address;
import com.gig8.motacoinj.params.MainNetParams;
import com.gig8.motacoinj.uri.MotaCoinURI;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static com.gig8.motacoinj.core.Coin.*;
import static org.junit.Assert.*;

public class MotaCoinURITest {
    private MotaCoinURI testObject = null;

    private static final String MAINNET_GOOD_ADDRESS = "PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4";
    private static final String MAINNET_BAD_ADDRESS = "mranY19RYUjgJjXY4BJNYp88WXXAg7Pr9T";

    @Test
    public void testConvertToMotaCoinURI() throws Exception {
        Address goodAddress = new Address(MainNetParams.get(), MAINNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("12.34"), "Hello World", "Mess & age + hope"));

        // no amount, label present, message present
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?label=Hello&message=glory", MotaCoinURI.convertToMotaCoinURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("0.1"), null, "glory"));
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("12.34"), "Hello", null));
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=1000", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("1000"), null, null));
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?amount=1000", MotaCoinURI.convertToMotaCoinURI(goodAddress, parseCoin("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?label=Hello", MotaCoinURI.convertToMotaCoinURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", MotaCoinURI.convertToMotaCoinURI(goodAddress, null, null, "Agatha"));
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", MotaCoinURI.convertToMotaCoinURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS, MotaCoinURI.convertToMotaCoinURI(goodAddress, null, null, null));
        assertEquals("motaoin:" + MAINNET_GOOD_ADDRESS, MotaCoinURI.convertToMotaCoinURI(goodAddress, null, "", ""));
    }

    @Test
    public void testGood_Simple() throws MotaCoinURIParseException {
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
        assertNotNull(testObject);
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash160().length);
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), "blimpcoin:" + MAINNET_GOOD_ADDRESS);
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + "|" + MAINNET_GOOD_ADDRESS);
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "\\");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME);
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_BAD_ADDRESS);
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws MotaCoinURIParseException {
        // Test the decimal parsing
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210.123456");
        assertEquals("6543210123456", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=.123456");
        assertEquals("123456", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210");
        assertEquals("6543210000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws MotaCoinURIParseException {
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws Exception {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = MotaCoinURI.encodeURLString(testString);
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithRussian() throws Exception {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = MotaCoinURI.encodeURLString(moscowString); 
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws MotaCoinURIParseException {
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws MotaCoinURIParseException {
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "MotaCoinURI['amount'='6543210000000','label'='Hello World','message'='Be well','address'='PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws MotaCoinURIParseException {
        // Missing
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    @Test
    public void testEmpty_Label() throws MotaCoinURIParseException {
        assertNull(new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=").getLabel());
    }

    @Test
    public void testEmpty_Message() throws MotaCoinURIParseException {
        assertNull(new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=").getMessage());
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws MotaCoinURIParseException {
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    @Test
    public void testGood_ManyEquals() throws MotaCoinURIParseException {
        assertEquals("aardvark=zebra", new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":"
                + MAINNET_GOOD_ADDRESS + "?label=aardvark=zebra").getLabel());
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws MotaCoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws MotaCoinURIParseException {
        // Unknown not required field
        testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("MotaCoinURI['aardvark'='true','address'='PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4']", testObject.toString());

        assertEquals("true", (String) testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("no separator"));
        }

        // Unknown and required field
        try {
            testObject = new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting MotaCoinURIParseException");
        } catch (MotaCoinURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws MotaCoinURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "motaoin://PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4?amount=0.01000000";
        MotaCoinURI uri = new MotaCoinURI(str);
        assertEquals("PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4", uri.getAddress().toString());
        assertEquals(CENT, uri.getAmount());
    }

    @Test(expected = MotaCoinURIParseException.class)
    public void testBad_AmountTooPrecise() throws MotaCoinURIParseException {
        new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=0.123456789");
    }

    @Test(expected = MotaCoinURIParseException.class)
    public void testBad_NegativeAmount() throws MotaCoinURIParseException {
        new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=-1");
    }

    @Test(expected = MotaCoinURIParseException.class)
    public void testBad_TooLargeAmount() throws MotaCoinURIParseException {
        new MotaCoinURI(MainNetParams.get(), MotaCoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=2000000001");
    }

    @Test
    public void testPaymentProtocolReq() throws Exception {
        // Non-backwards compatible form ...
        MotaCoinURI uri = new MotaCoinURI(MainNetParams.get(), "motaoin:?r=https%3A%2F%2Fmotaoincore.org%2F%7Egavin%2Ff.php%3Fh%3Db0f02e7cea67f168e25ec9b9f9d584f9");
        assertEquals("https://motaoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://motaoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9"),
                uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }

    @Test
    public void testMultiplePaymentProtocolReq() throws Exception {
        MotaCoinURI uri = new MotaCoinURI(MainNetParams.get(),
                "motaoin:?r=https%3A%2F%2Fmotaoincore.org%2F%7Egavin&r1=bt:112233445566");
        assertEquals(ImmutableList.of("bt:112233445566", "https://motaoincore.org/~gavin"), uri.getPaymentRequestUrls());
        assertEquals("https://motaoincore.org/~gavin", uri.getPaymentRequestUrl());
    }

    @Test
    public void testNoPaymentProtocolReq() throws Exception {
        MotaCoinURI uri = new MotaCoinURI(MainNetParams.get(), "motaoin:" + MAINNET_GOOD_ADDRESS);
        assertNull(uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of(), uri.getPaymentRequestUrls());
        assertNotNull(uri.getAddress());
    }

    @Test
    public void testUnescapedPaymentProtocolReq() throws Exception {
        MotaCoinURI uri = new MotaCoinURI(MainNetParams.get(),
                "motaoin:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe");
        assertEquals("https://merchant.com/pay.php?h=2a8628fc2fbe", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://merchant.com/pay.php?h=2a8628fc2fbe"), uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }
}

