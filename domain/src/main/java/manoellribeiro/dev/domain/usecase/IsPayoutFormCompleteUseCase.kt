package manoellribeiro.dev.domain.usecase

class IsPayoutFormCompleteUseCase {

    /**
     * Decides when the merchant can press Confirm: an amount above zero and an IBAN. Whether they're valid is checked
     * on submit by [ParsePayoutAmountUseCase] and [ValidateIbanUseCase], so "10.999" gets an inline error.
     */
    operator fun invoke(amount: String, iban: String): Boolean =
        amount.trim().toBigDecimalOrNull()?.let { it.signum() > 0 } == true && iban.isNotEmpty()
}
