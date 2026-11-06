package com.example.androidinterview.screens.transactions

import androidx.annotation.StringRes
import com.example.androidinterview.R
import com.example.androidinterview.core.formatting.toCurrencyText
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType

sealed interface TransactionsUiState {
    data object Loading : TransactionsUiState
    data object Error : TransactionsUiState
    data object Empty : TransactionsUiState
    data class Content(
        val items: List<TransactionListItem>,
        val nextPageState: NextPageState,
    ) : TransactionsUiState
}

enum class NextPageState { Idle, Loading, Error }

/** A flat list of date headers and rows, which is what the LazyColumn renders. */
sealed interface TransactionListItem {
    val key: String

    data class DateHeader(
        override val key: String,
        @param:StringRes val relativeLabelRes: Int?,
        val formattedDate: String,
    ) : TransactionListItem

    data class Row(
        override val key: String,
        @param:StringRes val typeLabelRes: Int,
        val description: String,
        val formattedDate: String,
        val formattedAmount: String,
        @param:StringRes val statusLabelRes: Int,
        val isOutflow: Boolean,
    ) : TransactionListItem
}

/** Groups by the merchant's local day, so a late-evening UTC date lands on the day the merchant actually saw it. */
fun List<ActivityItem>.mapToTransactionListItems(
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
): List<TransactionListItem> {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    return groupBy { it.date.atZone(zone).toLocalDate() }.flatMap { (date, items) ->
        val formattedDate = date.format(dateFormatter)
        val header = TransactionListItem.DateHeader(
            key = "header-$date",
            relativeLabelRes = when (date) {
                today -> R.string.transactions_today
                today.minusDays(1) -> R.string.transactions_yesterday
                else -> null
            },
            formattedDate = formattedDate,
        )
        listOf(header) + items.map { item ->
            TransactionListItem.Row(
                key = item.id,
                typeLabelRes = item.type.labelRes(),
                description = item.description,
                formattedDate = formattedDate,
                formattedAmount = item.amount.toCurrencyText(),
                statusLabelRes = item.status.labelRes(),
                isOutflow = item.amount.minorUnits < 0,
            )
        }
    }
}

@StringRes
private fun ActivityType.labelRes(): Int = when (this) {
    ActivityType.Payout -> R.string.activity_type_payout
    ActivityType.Deposit -> R.string.activity_type_deposit
    ActivityType.Refund -> R.string.activity_type_refund
    ActivityType.Fee -> R.string.activity_type_fee
    ActivityType.Unknown -> R.string.activity_type_unknown
}

@StringRes
private fun ActivityStatus.labelRes(): Int = when (this) {
    ActivityStatus.Completed -> R.string.activity_status_completed
    ActivityStatus.Pending -> R.string.activity_status_pending
    ActivityStatus.Processing -> R.string.activity_status_processing
    ActivityStatus.Failed -> R.string.activity_status_failed
    ActivityStatus.Unknown -> R.string.activity_status_unknown
}
