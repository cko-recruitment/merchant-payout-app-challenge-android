package manoellribeiro.dev.domain.model

data class MerchantBalance(
    val available: Money,
    val pending: Money,
)
