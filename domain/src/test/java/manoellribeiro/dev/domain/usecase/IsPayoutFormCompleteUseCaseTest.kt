package manoellribeiro.dev.domain.usecase

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsPayoutFormCompleteUseCaseTest {

    private lateinit var isPayoutFormComplete: IsPayoutFormCompleteUseCase

    @BeforeEach
    fun setup() {
        isPayoutFormComplete = IsPayoutFormCompleteUseCase()
    }

    @Test
    fun `if the amount is above zero and there is an iban should return true`() {
        // Given
        val fakeAmount = "400"
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the amount has surrounding whitespace should ignore it`() {
        // Given
        val fakeAmount = "  400  "
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the amount has too many decimals should return true so submit can show the inline error`() {
        // Given
        val fakeAmount = "10.999"
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the iban has an invalid format should return true so submit can show the inline error`() {
        // Given
        val fakeAmount = "400"
        val fakeIban = "fake-iban"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the amount is empty should return false`() {
        // Given
        val fakeAmount = ""
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is only whitespace should return false`() {
        // Given
        val fakeAmount = "   "
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is zero should return false`() {
        // Given
        val fakeAmount = "0.00"
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is negative should return false`() {
        // Given
        val fakeAmount = "-5"
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is not numeric should return false`() {
        // Given
        val fakeAmount = "fake-amount"
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the iban is empty should return false`() {
        // Given
        val fakeAmount = "400"
        val fakeIban = ""

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if both fields are empty should return false`() {
        // Given
        val fakeAmount = ""
        val fakeIban = ""

        // When
        val fakeResult = isPayoutFormComplete(fakeAmount, fakeIban)

        // Then
        assertFalse(fakeResult)
    }
}
