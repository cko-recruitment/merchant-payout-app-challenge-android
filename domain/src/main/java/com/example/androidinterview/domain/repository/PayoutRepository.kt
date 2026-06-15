package com.example.androidinterview.domain.repository

import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.domain.model.Payout

interface PayoutRepository {
    suspend fun createPayout(amount: Int, currency: Currency, iban: String): Result<Payout>
}
