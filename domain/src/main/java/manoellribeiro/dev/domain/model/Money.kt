package manoellribeiro.dev.domain.model

/** An amount in the currency's lowest denomination (pence, cents). Negative means an outflow. */
data class Money(
    val minorUnits: Long,
    val currency: Currency,
)
