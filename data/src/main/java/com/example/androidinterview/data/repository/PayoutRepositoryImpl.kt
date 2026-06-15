package com.example.androidinterview.data.repository

import com.example.androidinterview.data.device.DeviceIdProvider
import com.example.androidinterview.data.di.IoDispatcher
import com.example.androidinterview.data.model.ErrorResponseDto
import com.example.androidinterview.data.model.PayoutRequestDto
import com.example.androidinterview.data.model.toDomain
import com.example.androidinterview.data.model.toDto
import com.example.androidinterview.data.network.MerchantApi
import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.domain.model.Payout
import com.example.androidinterview.domain.model.PayoutException
import com.example.androidinterview.domain.repository.PayoutRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject

private val errorJson = Json { ignoreUnknownKeys = true }

internal class PayoutRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val deviceIdProvider: DeviceIdProvider,
    private val api: MerchantApi,
) : PayoutRepository {

    override suspend fun createPayout(amount: Int, currency: Currency, iban: String): Result<Payout> =
        withContext(ioDispatcher) {
            runCatching {
                val deviceId = deviceIdProvider.getDeviceId()
                api.createPayout(
                    PayoutRequestDto(
                        amount = amount,
                        currency = currency.toDto(),
                        iban = iban,
                        deviceId = deviceId,
                    )
                ).toDomain()
            }.recoverCatching { t ->
                throw if (t is HttpException) t.toPayoutException() else t
            }
        }

    private fun HttpException.toPayoutException(): PayoutException {
        val raw = response()?.errorBody()?.string()
        val dto = raw?.let { runCatching { errorJson.decodeFromString<ErrorResponseDto>(it) }.getOrNull() }
        val msg = dto?.error ?: message()
        return when (dto?.code) {
            "INSUFFICIENT_FUNDS"  -> PayoutException.InsufficientFunds(msg ?: "Insufficient funds")
            "SERVICE_UNAVAILABLE" -> PayoutException.ServiceUnavailable(msg ?: "Service unavailable")
            else                  -> PayoutException.Api(msg ?: "Unknown error")
        }
    }
}
