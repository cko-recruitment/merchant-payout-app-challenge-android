package manoellribeiro.dev.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.MerchantApi
import manoellribeiro.dev.data.remote.model.RemoteActivityItem
import manoellribeiro.dev.data.remote.model.RemoteActivityPage
import manoellribeiro.dev.data.remote.model.RemoteCurrency
import manoellribeiro.dev.data.remote.model.RemoteMerchantResponse
import manoellribeiro.dev.data.remote.model.RemotePayoutRequest
import manoellribeiro.dev.data.remote.model.RemotePayoutResponse
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutRequest
import manoellribeiro.dev.domain.model.PayoutStatus
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.HttpException
import retrofit2.Response

@ExtendWith(MainDispatcherExtension::class)
class MerchantRepositoryImplTest {

    private lateinit var api: MerchantApi
    private lateinit var repository: MerchantRepositoryImpl

    @BeforeEach
    fun setup() {
        api = mockk()
        repository = MerchantRepositoryImpl(api)
    }

    @Test
    fun `if getMerchant succeeds should return the mapped balance`() = runTest {
        // Given
        coEvery { api.getMerchant() } returns RemoteMerchantResponse(
            availableBalance = 500_000,
            pendingBalance = 25_000,
            currency = RemoteCurrency.GBP,
        )

        // When
        val fakeResult = repository.getBalance()

        // Then
        val fakeExpected = MerchantBalance(
            available = Money(500_000, Currency.GBP),
            pending = Money(25_000, Currency.GBP),
        )
        assertEquals(fakeExpected, fakeResult.getOrNull())
    }

    @Test
    fun `if getMerchant throws an IOException should return a Network failure`() = runTest {
        // Given
        coEvery { api.getMerchant() } throws IOException("fake connection failure")

        // When
        val fakeResult = repository.getBalance()

        // Then
        assertInstanceOf(DataError.Network::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if getMerchant responds with a server error should return a ServiceUnavailable failure`() = runTest {
        // Given
        coEvery { api.getMerchant() } throws createFakeHttpException(500, "{}")

        // When
        val fakeResult = repository.getBalance()

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if getMerchant returns a malformed response should return an Unexpected failure`() = runTest {
        // Given
        coEvery { api.getMerchant() } throws SerializationException("fake malformed response")

        // When
        val fakeResult = repository.getBalance()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if getActivity succeeds should return the mapped page`() = runTest {
        // Given
        val fakeDate = Instant.parse("2026-01-23T10:00:00Z")
        coEvery { api.getActivity(any(), any()) } returns RemoteActivityPage(
            items = listOf(
                RemoteActivityItem(
                    id = "fake-activity-id",
                    type = "deposit",
                    amount = 75_000,
                    currency = RemoteCurrency.GBP,
                    date = fakeDate,
                    description = "Fake payment",
                    status = "completed",
                ),
            ),
            nextCursor = "fake-next-cursor",
            hasMore = true,
        )

        // When
        val fakeResult = repository.getActivity(cursor = null, limit = 3)

        // Then
        val fakeExpected = ActivityPage(
            items = listOf(
                ActivityItem(
                    id = "fake-activity-id",
                    type = ActivityType.Deposit,
                    amount = Money(75_000, Currency.GBP),
                    date = fakeDate,
                    description = "Fake payment",
                    status = ActivityStatus.Completed,
                ),
            ),
            nextCursor = "fake-next-cursor",
            hasMore = true,
        )
        assertEquals(fakeExpected, fakeResult.getOrNull())
    }

    @Test
    fun `if getActivity receives a cursor and a limit should pass them to the api`() = runTest {
        // Given
        val fakeCursor = "fake-cursor"
        val fakeLimit = 15
        coEvery { api.getActivity(any(), any()) } returns createFakeEmptyPage()

        // When
        repository.getActivity(fakeCursor, fakeLimit)

        // Then
        coVerify(exactly = 1) { api.getActivity(fakeCursor, fakeLimit) }
    }

    @Test
    fun `if getActivity receives a null cursor should pass null to the api`() = runTest {
        // Given
        val fakeLimit = 3
        coEvery { api.getActivity(any(), any()) } returns createFakeEmptyPage()

        // When
        repository.getActivity(null, fakeLimit)

        // Then
        coVerify(exactly = 1) { api.getActivity(null, fakeLimit) }
    }

    @Test
    fun `if the api returns an empty page should return a page with no items`() = runTest {
        // Given
        coEvery { api.getActivity(any(), any()) } returns createFakeEmptyPage()

        // When
        val fakeResult = repository.getActivity(null, 15)

        // Then
        assertEquals(ActivityPage(items = emptyList(), nextCursor = null, hasMore = false), fakeResult.getOrNull())
    }

    @Test
    fun `if getActivity throws an IOException should return a Network failure`() = runTest {
        // Given
        coEvery { api.getActivity(any(), any()) } throws IOException("fake connection failure")

        // When
        val fakeResult = repository.getActivity(null, 15)

        // Then
        assertInstanceOf(DataError.Network::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if createPayout succeeds should return the mapped payout`() = runTest {
        // Given
        val fakeCreatedAt = Instant.parse("2026-02-10T09:30:00Z")
        val fakeIban = "GB29NWBK60161331926819"
        coEvery { api.createPayout(any()) } returns RemotePayoutResponse(
            id = "fake-payout-id",
            status = "processing",
            amount = 40_000,
            currency = RemoteCurrency.GBP,
            iban = fakeIban,
            createdAt = fakeCreatedAt,
        )

        // When
        val fakeResult = repository.createPayout(createFakePayoutRequest())

        // Then
        val fakeExpected = Payout(
            id = "fake-payout-id",
            status = PayoutStatus.Processing,
            amount = Money(40_000, Currency.GBP),
            iban = fakeIban,
            createdAt = fakeCreatedAt,
        )
        assertEquals(fakeExpected, fakeResult.getOrNull())
    }

    @Test
    fun `if createPayout is called should send the mapped request to the api`() = runTest {
        // Given
        val fakeRequestSlot = slot<RemotePayoutRequest>()
        coEvery { api.createPayout(capture(fakeRequestSlot)) } throws IOException("fake connection failure")

        // When
        repository.createPayout(createFakePayoutRequest())

        // Then
        val fakeExpected = RemotePayoutRequest(
            amount = 40_000,
            currency = RemoteCurrency.GBP,
            iban = "GB29NWBK60161331926819",
            deviceId = "fake-device-id",
        )
        assertEquals(fakeExpected, fakeRequestSlot.captured)
        coVerify(exactly = 1) { api.createPayout(any()) }
    }

    @Test
    fun `if createPayout responds with insufficient funds should return an InsufficientFunds failure`() = runTest {
        // Given
        coEvery { api.createPayout(any()) } throws createFakeHttpException(400, """{"code":"INSUFFICIENT_FUNDS"}""")

        // When
        val fakeResult = repository.createPayout(createFakePayoutRequest())

        // Then
        assertInstanceOf(DataError.InsufficientFunds::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if createPayout responds with 503 should return a ServiceUnavailable failure`() = runTest {
        // Given
        coEvery { api.createPayout(any()) } throws createFakeHttpException(503, """{"code":"SERVICE_UNAVAILABLE"}""")

        // When
        val fakeResult = repository.createPayout(createFakePayoutRequest())

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if createPayout throws an IOException should return a Network failure`() = runTest {
        // Given
        coEvery { api.createPayout(any()) } throws IOException("fake connection failure")

        // When
        val fakeResult = repository.createPayout(createFakePayoutRequest())

        // Then
        assertInstanceOf(DataError.Network::class.java, fakeResult.exceptionOrNull())
    }

    private fun createFakeEmptyPage() = RemoteActivityPage(items = emptyList(), nextCursor = null, hasMore = false)

    private fun createFakePayoutRequest() = PayoutRequest(
        amount = Money(40_000, Currency.GBP),
        iban = "GB29NWBK60161331926819",
        deviceId = "fake-device-id",
    )

    private fun createFakeHttpException(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())),
    )
}
