/*
 * Copyright 2012, 2014 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.crownj.uri;

import org.crownj.core.Address;
import org.crownj.core.LegacyAddress;
import org.crownj.params.MainNetParams;
import org.crownj.params.TestNet3Params;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.crownj.core.Coin.*;
import org.crownj.core.NetworkParameters;
import org.crownj.core.SegwitAddress;

import static org.junit.Assert.*;

import java.util.Locale;

public class crownURITest {
    private crownURI testObject = null;

    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final NetworkParameters TESTNET = TestNet3Params.get();
    private static final String MAINNET_GOOD_ADDRESS = "1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH";
    private static final String MAINNET_GOOD_SEGWIT_ADDRESS = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4";
    private static final String crown_SCHEME = MAINNET.getUriScheme();

    @Test
    public void testConvertTocrownURI() throws Exception {
        Address goodAddress = LegacyAddress.fromBase58(MAINNET, MAINNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", crownURI.convertTocrownURI(goodAddress, parseCoin("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", crownURI.convertTocrownURI(goodAddress, parseCoin("12.34"), "Hello World", "Mess & age + hope"));

        // no amount, label present, message present
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?label=Hello&message=glory", crownURI.convertTocrownURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", crownURI.convertTocrownURI(goodAddress, parseCoin("0.1"), null, "glory"));
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", crownURI.convertTocrownURI(goodAddress, parseCoin("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", crownURI.convertTocrownURI(goodAddress, parseCoin("12.34"), "Hello", null));
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", crownURI.convertTocrownURI(goodAddress, parseCoin("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=1000", crownURI.convertTocrownURI(goodAddress, parseCoin("1000"), null, null));
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?amount=1000", crownURI.convertTocrownURI(goodAddress, parseCoin("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?label=Hello", crownURI.convertTocrownURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", crownURI.convertTocrownURI(goodAddress, null, null, "Agatha"));
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", crownURI.convertTocrownURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS, crownURI.convertTocrownURI(goodAddress, null, null, null));
        assertEquals("crown:" + MAINNET_GOOD_ADDRESS, crownURI.convertTocrownURI(goodAddress, null, "", ""));

        // different scheme
        final NetworkParameters alternativeParameters = new MainNetParams() {
            @Override
            public String getUriScheme() {
                return "test";
            }
        };

        assertEquals("test:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage",
             crownURI.convertTocrownURI(LegacyAddress.fromBase58(alternativeParameters, MAINNET_GOOD_ADDRESS), parseCoin("12.34"), "Hello", "AMessage"));
    }

    @Test
    public void testConvertTocrownURI_segwit() throws Exception {
        assertEquals("crown:" + MAINNET_GOOD_SEGWIT_ADDRESS + "?message=segwit%20rules", crownURI.convertTocrownURI(
                SegwitAddress.fromBech32(MAINNET, MAINNET_GOOD_SEGWIT_ADDRESS), null, null, "segwit rules"));
    }

    @Test
    public void testGood_legacy() throws crownURIParseException {
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
        assertEquals(MAINNET_GOOD_ADDRESS, testObject.getAddress().toString());
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash().length);
    }

    @Test
    public void testGood_uppercaseScheme() throws crownURIParseException {
        testObject = new crownURI(MAINNET, crown_SCHEME.toUpperCase(Locale.US) + ":" + MAINNET_GOOD_ADDRESS);
        assertEquals(MAINNET_GOOD_ADDRESS, testObject.getAddress().toString());
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash().length);
    }

    @Test
    public void testGood_segwit() throws crownURIParseException {
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_SEGWIT_ADDRESS);
        assertEquals(MAINNET_GOOD_SEGWIT_ADDRESS, testObject.getAddress().toString());
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new crownURI(MAINNET, "blimpcoin:" + MAINNET_GOOD_ADDRESS);
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + "|" + MAINNET_GOOD_ADDRESS);
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "\\");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME);
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new crownURI(TESTNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws crownURIParseException {
        // Test the decimal parsing
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210.12345678");
        assertEquals("654321012345678", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=.12345678");
        assertEquals("12345678", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210");
        assertEquals("654321000000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws crownURIParseException {
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws crownURIParseException {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = crownURI.encodeURLString(testString);
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithRussian() throws crownURIParseException {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = crownURI.encodeURLString(moscowString); 
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws crownURIParseException {
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws crownURIParseException {
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "crownURI['amount'='654321000000000','label'='Hello World','message'='Be well','address'='1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws crownURIParseException {
        // Missing
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    @Test
    public void testEmpty_Label() throws crownURIParseException {
        assertNull(new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=").getLabel());
    }

    @Test
    public void testEmpty_Message() throws crownURIParseException {
        assertNull(new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=").getMessage());
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws crownURIParseException {
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    @Test
    public void testGood_ManyEquals() throws crownURIParseException {
        assertEquals("aardvark=zebra", new crownURI(MAINNET, crown_SCHEME + ":"
                + MAINNET_GOOD_ADDRESS + "?label=aardvark=zebra").getLabel());
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws crownURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws crownURIParseException {
        // Unknown not required field
        testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("crownURI['aardvark'='true','address'='1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH']", testObject.toString());

        assertEquals("true", testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("no separator"));
        }

        // Unknown and required field
        try {
            testObject = new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting crownURIParseException");
        } catch (crownURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws crownURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "crown://1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH?amount=0.01000000";
        crownURI uri = new crownURI(str);
        assertEquals("1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH", uri.getAddress().toString());
        assertEquals(CENT, uri.getAmount());
    }

    @Test(expected = crownURIParseException.class)
    public void testBad_AmountTooPrecise() throws crownURIParseException {
        new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=0.123456789");
    }

    @Test(expected = crownURIParseException.class)
    public void testBad_NegativeAmount() throws crownURIParseException {
        new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=-1");
    }

    @Test(expected = crownURIParseException.class)
    public void testBad_TooLargeAmount() throws crownURIParseException {
        new crownURI(MAINNET, crown_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=100000000");
    }

    @Test
    public void testPaymentProtocolReq() throws Exception {
        // Non-backwards compatible form ...
        crownURI uri = new crownURI(TESTNET, "crown:?r=https%3A%2F%2Fcrowncore.org%2F%7Egavin%2Ff.php%3Fh%3Db0f02e7cea67f168e25ec9b9f9d584f9");
        assertEquals("https://crowncore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://crowncore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9"),
                uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }

    @Test
    public void testMultiplePaymentProtocolReq() throws Exception {
        crownURI uri = new crownURI(MAINNET,
                "crown:?r=https%3A%2F%2Fcrowncore.org%2F%7Egavin&r1=bt:112233445566");
        assertEquals(ImmutableList.of("bt:112233445566", "https://crowncore.org/~gavin"), uri.getPaymentRequestUrls());
        assertEquals("https://crowncore.org/~gavin", uri.getPaymentRequestUrl());
    }

    @Test
    public void testNoPaymentProtocolReq() throws Exception {
        crownURI uri = new crownURI(MAINNET, "crown:" + MAINNET_GOOD_ADDRESS);
        assertNull(uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of(), uri.getPaymentRequestUrls());
        assertNotNull(uri.getAddress());
    }

    @Test
    public void testUnescapedPaymentProtocolReq() throws Exception {
        crownURI uri = new crownURI(TESTNET,
                "crown:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe");
        assertEquals("https://merchant.com/pay.php?h=2a8628fc2fbe", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://merchant.com/pay.php?h=2a8628fc2fbe"), uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }
}
