package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Money

@Serializable
internal data class RemoteMerchantResponse(
    @SerialName("available_balance") val availableBalance: Long,
    @SerialName("pending_balance") val pendingBalance: Long,
    val currency: RemoteCurrency,
)

internal fun RemoteMerchantResponse.toDomain(): MerchantBalance {
    val domainCurrency = currency.toDomain()
    return MerchantBalance(
        available = Money(availableBalance, domainCurrency),
        pending = Money(pendingBalance, domainCurrency),
    )
}
