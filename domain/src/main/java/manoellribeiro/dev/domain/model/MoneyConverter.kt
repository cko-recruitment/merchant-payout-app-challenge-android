package manoellribeiro.dev.domain.model

import java.math.BigDecimal

/** Reads the fraction digits from the currency (GBP has 2, JPY has 0), so a new currency needs no hard-coded 100. */
object MoneyConverter {

    /** Throws [ArithmeticException] if [major] has more decimals than the currency allows or doesn't fit a Long. */
    fun toMinorUnits(major: BigDecimal, currency: Currency): Long =
        major.movePointRight(fractionDigits(currency)).longValueExact()

    fun toMajorUnits(money: Money): BigDecimal =
        BigDecimal.valueOf(money.minorUnits, fractionDigits(money.currency))

    private fun fractionDigits(currency: Currency): Int =
        java.util.Currency.getInstance(currency.name).defaultFractionDigits
}
