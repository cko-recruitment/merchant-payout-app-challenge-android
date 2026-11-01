package manoellribeiro.dev.domain.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MaskIbanUseCaseTest {

    private lateinit var maskIban: MaskIbanUseCase

    @BeforeEach
    fun setup() {
        maskIban = MaskIbanUseCase()
    }

    @Test
    fun `if the iban is longer than 8 characters should keep only the first and last four`() {
        // Given
        val fakeIban = "GB29NWBK60161331926819"

        // When
        val fakeResult = maskIban(fakeIban)

        // Then
        assertEquals("GB29**************6819", fakeResult)
    }

    @Test
    fun `if the iban has 9 characters should hide only the middle character`() {
        // Given
        val fakeIban = "ABCD1EFGH"

        // When
        val fakeResult = maskIban(fakeIban)

        // Then
        assertEquals("ABCD*EFGH", fakeResult)
    }

    @Test
    fun `if the iban has 8 characters should hide all of them`() {
        // Given
        val fakeIban = "ABCDEFGH"

        // When
        val fakeResult = maskIban(fakeIban)

        // Then
        assertEquals("********", fakeResult)
    }

    @Test
    fun `if the iban has fewer than 8 characters should hide all of them`() {
        // Given
        val fakeIban = "ABC"

        // When
        val fakeResult = maskIban(fakeIban)

        // Then
        assertEquals("***", fakeResult)
    }

    @Test
    fun `if the iban is empty should return an empty text`() {
        // Given
        val fakeIban = ""

        // When
        val fakeResult = maskIban(fakeIban)

        // Then
        assertEquals("", fakeResult)
    }
}
