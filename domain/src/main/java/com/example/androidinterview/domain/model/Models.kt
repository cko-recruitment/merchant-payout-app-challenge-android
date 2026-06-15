package com.example.androidinterview.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Merchant(
    val availableBalance: Int,
    val pendingBalance: Int,
    val currency: Currency,
    val activity: List<Activity>,
)

@Serializable
data class Activity(
    val id: String,
    val type: ActivityType,
    val amount: Int,
    val currency: Currency,
    val date: String,
    val description: String,
    val status: ActivityStatus,
)

@Serializable enum class Currency { GBP, EUR }
@Serializable enum class ActivityType { PAYOUT, DEPOSIT, REFUND, FEE }
@Serializable enum class ActivityStatus { COMPLETED, PENDING, PROCESSING, FAILED }

data class ActivityPage(
    val items: List<Activity>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

enum class PayoutStatus { PENDING, PROCESSING, COMPLETED, FAILED }

data class Payout(
    val id: String,
    val status: PayoutStatus,
    val amount: Int,
    val currency: Currency,
    val iban: String,
    val createdAt: String,
)
