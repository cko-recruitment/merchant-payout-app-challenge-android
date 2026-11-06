package manoellribeiro.dev.domain.usecase

import java.math.BigDecimal
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.MoneyConverter

class ShouldRequestBiometricUseCase(
    private val thresholdsInMajorUnits: Map<Currency, BigDecimal> = mapOf(
        Currency.GBP to BigDecimal(1_000),
        Currency.EUR to BigDecimal(1_000),
    ),
) {
    operator fun invoke(amount: Money): Boolean {
        // A currency with no threshold takes the safe path.
        val threshold = thresholdsInMajorUnits[amount.currency] ?: return true
        return amount.minorUnits > MoneyConverter.toMinorUnits(threshold, amount.currency)
    }
}
