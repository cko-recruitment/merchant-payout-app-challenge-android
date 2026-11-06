package com.example.androidinterview.core.formatting

import java.text.NumberFormat
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.MoneyConverter

/** Formats for the device's locale, using the currency from the API: £5,000.00. */
fun Money.toCurrencyText(): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = java.util.Currency.getInstance(currency.name)
    return format.format(MoneyConverter.toMajorUnits(this))
}
