package com.example.androidinterview.domain.util

import com.example.androidinterview.domain.model.Currency
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue

fun Currency.symbol(): String = when (this) {
    Currency.GBP -> "£"
    Currency.EUR -> "€"
}

private val formatter = NumberFormat.getNumberInstance(Locale.UK).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

fun formatAmount(currency: Currency, amountPence: Int): String {
    val value = amountPence / 100.0
    val negativeSymbol = if (value < 0.0) "-" else ""
    return "$negativeSymbol${currency.symbol()}${formatter.format(value.absoluteValue)}"
}
