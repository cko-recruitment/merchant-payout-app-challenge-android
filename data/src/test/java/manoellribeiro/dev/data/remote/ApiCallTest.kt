package manoellribeiro.dev.data.remote

import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.HttpException
import retrofit2.Response

@ExtendWith(MainDispatcherExtension::class)
class ApiCallTest {

    private lateinit var retrofitResponse: Response<String>
    private lateinit var errorBody: ResponseBody

    @BeforeEach
    fun setup() {
        retrofitResponse = mockk()
        errorBody = mockk()
    }

    @Test
    fun `if the call succeeds should return its value`() = runTest {
        // Given
        val fakeValue = "fake-value"

        // When
        val fakeResult = apiCall { fakeValue }

        // Then
        assertEquals(fakeValue, fakeResult.getOrNull())
    }

    @Test
    fun `if the call succeeds with null should return a success holding null`() = runTest {
        // Given
        val fakeValue: String? = null

        // When
        val fakeResult = apiCall { fakeValue }

        // Then
        assertTrue(fakeResult.isSuccess)
        assertNull(fakeResult.getOrNull())
    }

    @Test
    fun `if the error code is INSUFFICIENT_FUNDS should return an InsufficientFunds failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(400, """{"code":"INSUFFICIENT_FUNDS"}""")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.InsufficientFunds::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the error body has unknown fields should still read the error code`() = runTest {
        // Given
        val fakeException = createFakeHttpException(
            400,
            """{"error":"Insufficient funds","code":"INSUFFICIENT_FUNDS"}""",
        )

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.InsufficientFunds::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 503 should return a ServiceUnavailable failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(503, """{"code":"SERVICE_UNAVAILABLE"}""")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 500 should return a ServiceUnavailable failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(500, "{}")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 599 should return a ServiceUnavailable failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(599, "{}")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 499 should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(499, "{}")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 600 should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(600, "{}")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 400 with another error code should return an Unexpected failure holding the exception`() = runTest {
        // Given
        val fakeException = createFakeHttpException(400, """{"code":"FAKE_OTHER_CODE"}""")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
    }

    @Test
    fun `if the status is 404 with an empty body should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(404, "")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 503 with a malformed body should return a ServiceUnavailable failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(503, "<html>fake error page</html>")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the status is 400 with a malformed body should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(400, "<html>fake error page</html>")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the error body is a json array should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(400, "[]")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the error code is not a string should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(400, """{"code":400}""")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the error code is null should return an Unexpected failure`() = runTest {
        // Given
        val fakeException = createFakeHttpException(400, """{"code":null}""")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the error body is missing should map the failure from the status`() = runTest {
        // Given
        every { retrofitResponse.code() } returns 503
        every { retrofitResponse.message() } returns "fake message"
        every { retrofitResponse.errorBody() } returns null
        val fakeException = HttpException(retrofitResponse)

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if reading the error body throws an IOException should map the failure from the status`() = runTest {
        // Given
        every { retrofitResponse.code() } returns 503
        every { retrofitResponse.message() } returns "fake message"
        every { retrofitResponse.errorBody() } returns errorBody
        every { errorBody.string() } throws IOException("fake read failure")
        val fakeException = HttpException(retrofitResponse)

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the call throws an IOException should return a Network failure holding it`() = runTest {
        // Given
        val fakeException = IOException("fake connection failure")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Network::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
    }

    @Test
    fun `if the call times out should return a Network failure`() = runTest {
        // Given
        val fakeException = SocketTimeoutException("fake timeout")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        assertInstanceOf(DataError.Network::class.java, fakeResult.exceptionOrNull())
    }

    @Test
    fun `if the call throws a SerializationException should return an Unexpected failure holding it`() = runTest {
        // Given
        val fakeException = SerializationException("fake malformed response")

        // When
        val fakeResult = apiCall<String> { throw fakeException }

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
    }

    @Test
    fun `if the call is cancelled should rethrow the CancellationException`() = runTest {
        // Given
        val fakeException = CancellationException("fake cancellation")

        // When
        val fakeThrown = assertThrows<CancellationException> {
            apiCall<String> { throw fakeException }
        }

        // Then
        assertSame(fakeException, fakeThrown)
    }

    private fun createFakeHttpException(code: Int, body: String): HttpException = HttpException(
        Response.error<String>(code, body.toResponseBody("application/json".toMediaType())),
    )
}
