package manoellribeiro.dev.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.Instant
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutRequest
import manoellribeiro.dev.domain.model.PayoutStatus
import manoellribeiro.dev.domain.repository.DeviceIdentityRepository
import manoellribeiro.dev.domain.repository.MerchantRepository
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class CreatePayoutUseCaseTest {

    private lateinit var merchantRepository: MerchantRepository
    private lateinit var deviceIdentityRepository: DeviceIdentityRepository
    private lateinit var createPayout: CreatePayoutUseCase

    @BeforeEach
    fun setup() {
        merchantRepository = mockk()
        deviceIdentityRepository = mockk()

        createPayout = CreatePayoutUseCase(
            merchantRepository = merchantRepository,
            deviceIdentityRepository = deviceIdentityRepository,
        )
    }

    @Test
    fun `if the device id is available should send a request with the amount iban and device id`() = runTest {
        // Given
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeIban = "GB29NWBK60161331926819"
        val fakeDeviceId = "fake-device-id"
        val fakePayoutRequestSlot = slot<PayoutRequest>()
        coEvery { deviceIdentityRepository.getDeviceId() } returns Result.success(fakeDeviceId)
        coEvery { merchantRepository.createPayout(capture(fakePayoutRequestSlot)) } returns
            Result.success(createFakePayout())

        // When
        createPayout(fakeAmount, fakeIban)

        // Then
        assertEquals(fakeAmount, fakePayoutRequestSlot.captured.amount)
        assertEquals(fakeIban, fakePayoutRequestSlot.captured.iban)
        assertEquals(fakeDeviceId, fakePayoutRequestSlot.captured.deviceId)
        coVerify(exactly = 1) { deviceIdentityRepository.getDeviceId() }
        coVerify(exactly = 1) { merchantRepository.createPayout(any()) }
    }

    @Test
    fun `if createPayout succeeds should return the payout`() = runTest {
        // Given
        val fakePayout = createFakePayout()
        coEvery { deviceIdentityRepository.getDeviceId() } returns Result.success("fake-device-id")
        coEvery { merchantRepository.createPayout(any()) } returns Result.success(fakePayout)

        // When
        val fakeResult = createPayout(fakePayout.amount, fakePayout.iban)

        // Then
        assertEquals(fakePayout, fakeResult.getOrNull())
    }

    @Test
    fun `if createPayout fails should return its failure`() = runTest {
        // Given
        val fakeError = DataError.InsufficientFunds()
        val fakeAmount = Money(minorUnits = 88_888, currency = Currency.GBP)
        coEvery { deviceIdentityRepository.getDeviceId() } returns Result.success("fake-device-id")
        coEvery { merchantRepository.createPayout(any()) } returns Result.failure(fakeError)

        // When
        val fakeResult = createPayout(fakeAmount, "GB29NWBK60161331926819")

        // Then
        assertEquals(fakeError, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if getDeviceId fails should return its failure without calling createPayout`() = runTest {
        // Given
        val fakeError = DataError.Unexpected()
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        coEvery { deviceIdentityRepository.getDeviceId() } returns Result.failure(fakeError)

        // When
        val fakeResult = createPayout(fakeAmount, "GB29NWBK60161331926819")

        // Then
        assertEquals(fakeError, fakeResult.exceptionOrNull())
        coVerify(exactly = 0) { merchantRepository.createPayout(any()) }
    }

    private fun createFakePayout(): Payout = Payout(
        id = "fake-payout-id",
        status = PayoutStatus.Pending,
        amount = Money(minorUnits = 40_000, currency = Currency.GBP),
        iban = "GB29NWBK60161331926819",
        createdAt = Instant.parse("2026-01-23T10:00:00Z"),
    )
}
