package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import manoellribeiro.dev.domain.model.PayoutRequest

@Serializable
internal data class RemotePayoutRequest(
    val amount: Long,
    val currency: RemoteCurrency,
    val iban: String,
    @SerialName("device_id") val deviceId: String,
)

internal fun PayoutRequest.toRemote() = RemotePayoutRequest(
    amount = amount.minorUnits,
    currency = amount.currency.toRemote(),
    iban = iban,
    deviceId = deviceId,
)
