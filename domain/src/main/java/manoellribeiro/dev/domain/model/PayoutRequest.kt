package manoellribeiro.dev.domain.model

data class PayoutRequest(
    val amount: Money,
    val iban: String,
    val deviceId: String,
) {
    // A data class prints every field, so this stops the IBAN reaching logs or crash reports.
    override fun toString(): String = "PayoutRequest(amount=$amount, iban=***, deviceId=***)"
}
