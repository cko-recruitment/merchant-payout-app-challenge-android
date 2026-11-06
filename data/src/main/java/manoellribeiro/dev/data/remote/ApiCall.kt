package manoellribeiro.dev.data.remote

import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import manoellribeiro.dev.data.remote.model.RemoteApiError
import manoellribeiro.dev.domain.error.DataError
import retrofit2.HttpException

// Lets the API add fields without breaking app versions already released.
internal val apiJson = Json { ignoreUnknownKeys = true }

private const val INSUFFICIENT_FUNDS_CODE = "INSUFFICIENT_FUNDS"

/**
 * Runs an API call and turns every failure into a [DataError].
 * Not runCatching: that would also catch CancellationException and break coroutine cancellation.
 */
internal suspend fun <T> apiCall(call: suspend () -> T): Result<T> = try {
    Result.success(call())
} catch (e: HttpException) {
    Result.failure(e.toDataError())
} catch (e: IOException) {
    Result.failure(DataError.Network(e))
} catch (e: SerializationException) {
    Result.failure(DataError.Unexpected(e))
} catch (e: NullPointerException) {
    // Retrofit throws this when a successful response has no body.
    Result.failure(DataError.Unexpected(e))
}

private fun HttpException.toDataError(): DataError {
    val errorCode = try {
        response()?.errorBody()?.string()?.let { apiJson.decodeFromString<RemoteApiError>(it).code }
    } catch (_: SerializationException) {
        null
    } catch (_: IOException) {
        null
    }
    return when {
        errorCode == INSUFFICIENT_FUNDS_CODE -> DataError.InsufficientFunds()
        code() in 500..599 -> DataError.ServiceUnavailable()
        else -> DataError.Unexpected(this)
    }
}
