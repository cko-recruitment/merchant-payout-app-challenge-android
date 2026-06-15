package com.example.androidinterview.ui.activity

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.androidinterview.domain.model.Activity
import com.example.androidinterview.domain.model.ActivityStatus
import com.example.androidinterview.domain.model.ActivityType
import com.example.androidinterview.domain.util.formatAmount
import com.example.androidinterview.presentation.R
import com.example.androidinterview.ui.activity.RecentActivityUiState.Success.ActivityGroup
import com.example.androidinterview.ui.activity.RecentActivityUiState.Success.Item
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentActivityUiModelMapper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Spec is unclear, written says "MM" but the reference screenshots are "MMM"
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    operator fun invoke(activities: List<Activity>): List<ActivityGroup> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        return activities
            .groupBy { ZonedDateTime.parse(it.date).toLocalDate() }
            .entries
            .sortedByDescending { it.key }
            .map { (date, items) ->
                ActivityGroup(
                    label = when (date) {
                        today -> context.getString(R.string.date_today)
                        yesterday -> context.getString(R.string.date_yesterday)
                        else -> date.format(dateFormatter)
                    },
                    items = items.map { activity ->
                        Item(
                            description = activity.description,
                            type = activity.type.toDisplayString(),
                            value = formatAmount(activity.currency, activity.amount),
                            valueColor = if (activity.amount < 0) Color.Red else Color(0xFF2E7D32),
                            date = ZonedDateTime.parse(activity.date)
                                .toLocalDate()
                                .format(dateFormatter),
                            status = activity.status.toDisplayString(),
                        )
                    },
                )
            }
    }

    private fun ActivityType.toDisplayString() = when (this) {
        ActivityType.PAYOUT  -> context.getString(R.string.activity_type_payout)
        ActivityType.DEPOSIT -> context.getString(R.string.activity_type_deposit)
        ActivityType.REFUND  -> context.getString(R.string.activity_type_refund)
        ActivityType.FEE     -> context.getString(R.string.activity_type_fee)
    }

    private fun ActivityStatus.toDisplayString() = when (this) {
        ActivityStatus.COMPLETED  -> context.getString(R.string.activity_status_completed)
        ActivityStatus.PENDING    -> context.getString(R.string.activity_status_pending)
        ActivityStatus.PROCESSING -> context.getString(R.string.activity_status_processing)
        ActivityStatus.FAILED     -> context.getString(R.string.activity_status_failed)
    }
}
