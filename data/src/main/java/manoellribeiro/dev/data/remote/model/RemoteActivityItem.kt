package manoellribeiro.dev.data.remote.model

import java.time.Instant
import kotlinx.serialization.Serializable
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType
import manoellribeiro.dev.domain.model.Money

@Serializable
internal data class RemoteActivityItem(
    val id: String,
    val type: String,
    val amount: Long,
    val currency: RemoteCurrency,
    @Serializable(with = IsoInstantSerializer::class) val date: Instant,
    val description: String,
    val status: String,
)

internal fun RemoteActivityItem.toDomain() = ActivityItem(
    id = id,
    type = when (type) {
        "payout" -> ActivityType.Payout
        "deposit" -> ActivityType.Deposit
        "refund" -> ActivityType.Refund
        "fee" -> ActivityType.Fee
        else -> ActivityType.Unknown
    },
    amount = Money(amount, currency.toDomain()),
    date = date,
    description = description,
    status = when (status) {
        "completed" -> ActivityStatus.Completed
        "pending" -> ActivityStatus.Pending
        "processing" -> ActivityStatus.Processing
        "failed" -> ActivityStatus.Failed
        else -> ActivityStatus.Unknown
    },
)
