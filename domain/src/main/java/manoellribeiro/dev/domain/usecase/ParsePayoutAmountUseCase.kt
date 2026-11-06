package manoellribeiro.dev.domain.usecase

import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.MoneyConverter

class ParsePayoutAmountUseCase {

    /** Returns null unless [input] is a positive amount the currency can represent, so "10.999" and "0" are rejected. */
    operator fun invoke(input: String, currency: Currency): Money? {
        val major = input.trim().toBigDecimalOrNull() ?: return null
        if (major.signum() <= 0) return null
        return try {
            Money(MoneyConverter.toMinorUnits(major, currency), currency)
        } catch (_: ArithmeticException) {
            null
        }
    }
}
