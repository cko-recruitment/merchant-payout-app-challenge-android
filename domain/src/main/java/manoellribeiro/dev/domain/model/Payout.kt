package manoellribeiro.dev.domain.model

import java.time.Instant

data class Payout(
    val id: String,
    val status: PayoutStatus,
    val amount: Money,
    val iban: String,
    val createdAt: Instant,
) {
    // A data class prints every field, so this stops the IBAN reaching logs or crash reports.
    override fun toString(): String =
        "Payout(id=$id, status=$status, amount=$amount, iban=***, createdAt=$createdAt)"
}
