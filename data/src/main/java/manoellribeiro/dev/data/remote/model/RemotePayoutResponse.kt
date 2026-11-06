package manoellribeiro.dev.data.remote.model

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutStatus

@Serializable
internal data class RemotePayoutResponse(
    val id: String,
    val status: String,
    val amount: Long,
    val currency: RemoteCurrency,
    val iban: String,
    @SerialName("created_at")
    @Serializable(with = IsoInstantSerializer::class)
    val createdAt: Instant,
)

internal fun RemotePayoutResponse.toDomain() = Payout(
    id = id,
    status = when (status) {
        "pending" -> PayoutStatus.Pending
        "processing" -> PayoutStatus.Processing
        "completed" -> PayoutStatus.Completed
        "failed" -> PayoutStatus.Failed
        else -> PayoutStatus.Unknown
    },
    amount = Money(amount, currency.toDomain()),
    iban = iban,
    createdAt = createdAt,
)
