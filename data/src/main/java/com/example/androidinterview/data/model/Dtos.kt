@file:SuppressLint("UnsafeOptInUsageError") // Bug in Kotlin showing incorrect errors
package com.example.androidinterview.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class CurrencyDto {
    @SerialName("GBP") GBP,
    @SerialName("EUR") EUR,
}

@Serializable
internal enum class ActivityTypeDto {
    @SerialName("payout") PAYOUT,
    @SerialName("deposit") DEPOSIT,
    @SerialName("refund") REFUND,
    @SerialName("fee") FEE,
}

@Serializable
internal enum class ActivityStatusDto {
    @SerialName("completed") COMPLETED,
    @SerialName("pending") PENDING,
    @SerialName("processing") PROCESSING,
    @SerialName("failed") FAILED,
}

@Serializable
internal data class ActivityItemDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: ActivityTypeDto,
    @SerialName("amount") val amount: Int,
    @SerialName("currency") val currency: CurrencyDto,
    @SerialName("date") val date: String,
    @SerialName("description") val description: String,
    @SerialName("status") val status: ActivityStatusDto,
)

@Serializable
internal enum class PayoutStatusDto {
    @SerialName("pending") PENDING,
    @SerialName("processing") PROCESSING,
    @SerialName("completed") COMPLETED,
    @SerialName("failed") FAILED,
}

@Serializable
internal data class PayoutRequestDto(
    @SerialName("amount") val amount: Int,
    @SerialName("currency") val currency: CurrencyDto,
    @SerialName("iban") val iban: String,
    @SerialName("device_id") val deviceId: String? = null,
)

@Serializable
internal data class PayoutResponseDto(
    @SerialName("id") val id: String,
    @SerialName("status") val status: PayoutStatusDto,
    @SerialName("amount") val amount: Int,
    @SerialName("currency") val currency: CurrencyDto,
    @SerialName("iban") val iban: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
internal data class DeviceResponseDto(
    @SerialName("device_id") val deviceId: String,
)

@Serializable
internal data class PaginatedActivityResponseDto(
    @SerialName("items") val items: List<ActivityItemDto>,
    @SerialName("next_cursor") val nextCursor: String?,
    @SerialName("has_more") val hasMore: Boolean,
)

@Serializable
internal data class MerchantDto(
    @SerialName("available_balance") val availableBalance: Int,
    @SerialName("pending_balance") val pendingBalance: Int,
    @SerialName("currency") val currency: CurrencyDto,
    @SerialName("activity") val activity: List<ActivityItemDto>,
)

@Serializable
internal data class ErrorResponseDto(
    @SerialName("error") val error: String,
    @SerialName("code") val code: String,
)
