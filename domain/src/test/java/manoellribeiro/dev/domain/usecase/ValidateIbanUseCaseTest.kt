package manoellribeiro.dev.domain.usecase

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateIbanUseCaseTest {

    private lateinit var validateIban: ValidateIbanUseCase

    @BeforeEach
    fun setup() {
        validateIban = ValidateIbanUseCase()
    }

    @Test
    fun `if the iban is the valid example from the README should return true`() {
        // Given
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the iban is the 39 character example from the README should return false`() {
        // Given
        val fakeIban = "FR1212345123451234567A12310131231231231"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban has 14 characters should return false`() {
        // Given
        val fakeIban = "GB" + "1".repeat(12)

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban has 15 characters should return true`() {
        // Given
        val fakeIban = "GB" + "1".repeat(13)

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the iban has 34 characters should return true`() {
        // Given
        val fakeIban = "GB" + "1".repeat(32)

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the iban has 35 characters should return false`() {
        // Given
        val fakeIban = "GB" + "1".repeat(33)

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban starts with lowercase letters should return false`() {
        // Given
        val fakeIban = "gb29NWBK60161331926819"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if only the second letter of the iban is lowercase should return false`() {
        // Given
        val fakeIban = "Gb29NWBK60161331926819"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban starts with digits should return false`() {
        // Given
        val fakeIban = "1229NWBK60161331926819"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the second character of the iban is a digit should return false`() {
        // Given
        val fakeIban = "G129NWBK60161331926819"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban starts with accented uppercase letters should return false`() {
        // Given
        val fakeIban = "ÉÉ29NWBK60161331926819"

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban is empty should return false`() {
        // Given
        val fakeIban = ""

        // When
        val fakeResult = validateIban(fakeIban)

        // Then
        assertFalse(fakeResult)
    }
}
