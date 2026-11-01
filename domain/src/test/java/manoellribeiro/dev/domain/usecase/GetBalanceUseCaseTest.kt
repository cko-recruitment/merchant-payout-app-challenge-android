package manoellribeiro.dev.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.repository.MerchantRepository
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class GetBalanceUseCaseTest {

    private lateinit var merchantRepository: MerchantRepository
    private lateinit var getBalance: GetBalanceUseCase

    @BeforeEach
    fun setup() {
        merchantRepository = mockk()

        getBalance = GetBalanceUseCase(merchantRepository = merchantRepository)
    }

    @Test
    fun `if the repository returns a balance should return it`() = runTest {
        // Given
        val fakeBalance = MerchantBalance(
            available = Money(minorUnits = 500_000, currency = Currency.GBP),
            pending = Money(minorUnits = 25_000, currency = Currency.GBP),
        )
        coEvery { merchantRepository.getBalance() } returns Result.success(fakeBalance)

        // When
        val fakeResult = getBalance()

        // Then
        assertEquals(fakeBalance, fakeResult.getOrNull())
        coVerify(exactly = 1) { merchantRepository.getBalance() }
    }

    @Test
    fun `if the repository fails should return its failure`() = runTest {
        // Given
        val fakeError = DataError.ServiceUnavailable()
        coEvery { merchantRepository.getBalance() } returns Result.failure(fakeError)

        // When
        val fakeResult = getBalance()

        // Then
        assertEquals(fakeError, fakeResult.exceptionOrNull())
    }
}
