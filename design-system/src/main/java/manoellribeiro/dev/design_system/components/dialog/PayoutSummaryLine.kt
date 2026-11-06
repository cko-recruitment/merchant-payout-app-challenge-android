package manoellribeiro.dev.design_system.components.dialog



/** One label and value line of the payout summary. The IBAN must already be masked. */
data class PayoutSummaryLine(
    val label: String,
    val value: String,
)
