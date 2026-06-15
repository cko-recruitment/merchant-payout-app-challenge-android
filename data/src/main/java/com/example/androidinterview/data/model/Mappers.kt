package com.example.androidinterview.data.model

import com.example.androidinterview.domain.model.Activity
import com.example.androidinterview.domain.model.ActivityPage
import com.example.androidinterview.domain.model.ActivityStatus
import com.example.androidinterview.domain.model.ActivityType
import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.domain.model.Merchant
import com.example.androidinterview.domain.model.Payout
import com.example.androidinterview.domain.model.PayoutStatus

internal fun PaginatedActivityResponseDto.toDomain() = ActivityPage(
    items = items.map { it.toDomain() },
    nextCursor = nextCursor,
    hasMore = hasMore,
)

internal fun MerchantDto.toDomain() = Merchant(
    availableBalance = availableBalance,
    pendingBalance = pendingBalance,
    currency = currency.toDomain(),
    activity = activity.map { it.toDomain() },
)

internal fun ActivityItemDto.toDomain() = Activity(
    id = id,
    type = type.toDomain(),
    amount = amount,
    currency = currency.toDomain(),
    date = date,
    description = description,
    status = status.toDomain(),
)

internal fun CurrencyDto.toDomain() = when (this) {
    CurrencyDto.GBP -> Currency.GBP
    CurrencyDto.EUR -> Currency.EUR
}

internal fun ActivityTypeDto.toDomain() = when (this) {
    ActivityTypeDto.PAYOUT  -> ActivityType.PAYOUT
    ActivityTypeDto.DEPOSIT -> ActivityType.DEPOSIT
    ActivityTypeDto.REFUND  -> ActivityType.REFUND
    ActivityTypeDto.FEE     -> ActivityType.FEE
}

internal fun ActivityStatusDto.toDomain() = when (this) {
    ActivityStatusDto.COMPLETED  -> ActivityStatus.COMPLETED
    ActivityStatusDto.PENDING    -> ActivityStatus.PENDING
    ActivityStatusDto.PROCESSING -> ActivityStatus.PROCESSING
    ActivityStatusDto.FAILED     -> ActivityStatus.FAILED
}

internal fun Currency.toDto() = when (this) {
    Currency.GBP -> CurrencyDto.GBP
    Currency.EUR -> CurrencyDto.EUR
}

internal fun PayoutResponseDto.toDomain() = Payout(
    id = id,
    status = status.toDomain(),
    amount = amount,
    currency = currency.toDomain(),
    iban = iban,
    createdAt = createdAt,
)

internal fun PayoutStatusDto.toDomain() = when (this) {
    PayoutStatusDto.PENDING    -> PayoutStatus.PENDING
    PayoutStatusDto.PROCESSING -> PayoutStatus.PROCESSING
    PayoutStatusDto.COMPLETED  -> PayoutStatus.COMPLETED
    PayoutStatusDto.FAILED     -> PayoutStatus.FAILED
}
