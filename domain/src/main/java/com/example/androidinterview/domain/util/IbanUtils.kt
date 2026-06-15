package com.example.androidinterview.domain.util

object IbanUtils {

    fun isValidIban(iban: String): Boolean =
        iban.matches(Regex("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$"))
}