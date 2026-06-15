package com.example.androidinterview.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IbanUtilsTest {

    // region valid IBANs

    @Test
    fun `valid UK IBAN is accepted`() {
        assertTrue(IbanUtils.isValidIban("GB29NWBK60161331926819"))
    }

    @Test
    fun `valid German IBAN is accepted`() {
        assertTrue(IbanUtils.isValidIban("DE89370400440532013000"))
    }

    @Test
    fun `valid French IBAN is accepted`() {
        assertTrue(IbanUtils.isValidIban("FR7614508069002013678700000"))
    }

    @Test
    fun `minimum length IBAN (15 chars) is accepted`() {
        // 2 country + 2 check + 11 BBAN = 15
        assertTrue(IbanUtils.isValidIban("GB00NWBK00A0000"))
    }

    @Test
    fun `maximum length IBAN (34 chars) is accepted`() {
        // 2 country + 2 check + 30 BBAN = 34
        assertTrue(IbanUtils.isValidIban("GB00" + "A".repeat(30)))
    }

    // endregion

    // region structural violations

    @Test
    fun `empty string is rejected`() {
        assertFalse(IbanUtils.isValidIban(""))
    }

    @Test
    fun `IBAN with 14 chars is too short`() {
        // BBAN only 10 chars (minimum is 11)
        assertFalse(IbanUtils.isValidIban("GB00NWBK00000"))
    }

    @Test
    fun `IBAN with 35 chars is too long`() {
        // BBAN 31 chars (maximum is 30)
        assertFalse(IbanUtils.isValidIban("GB00" + "A".repeat(31)))
    }

    // endregion

    // region country code violations

    @Test
    fun `lowercase country code is rejected`() {
        assertFalse(IbanUtils.isValidIban("gb29NWBK60161331926819"))
    }

    @Test
    fun `mixed-case country code is rejected`() {
        assertFalse(IbanUtils.isValidIban("Gb29NWBK60161331926819"))
    }

    @Test
    fun `digits in country code position are rejected`() {
        assertFalse(IbanUtils.isValidIban("1229NWBK60161331926819"))
    }

    // endregion

    // region check digit violations

    @Test
    fun `letters in check digit position are rejected`() {
        assertFalse(IbanUtils.isValidIban("GBXXNWBK60161331926819"))
    }

    // endregion

    // region BBAN violations

    @Test
    fun `lowercase letters in BBAN are rejected`() {
        assertFalse(IbanUtils.isValidIban("GB29nwbk60161331926819"))
    }

    @Test
    fun `spaces in IBAN are rejected`() {
        assertFalse(IbanUtils.isValidIban("GB29 NWBK 6016 1331 9268 19"))
    }

    @Test
    fun `special characters in IBAN are rejected`() {
        assertFalse(IbanUtils.isValidIban("GB29NWBK6016133192681!"))
    }

    // endregion
}
