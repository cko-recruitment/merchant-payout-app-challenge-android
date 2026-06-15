package com.example.androidinterview.ui.home

import androidx.compose.ui.graphics.Color
import com.example.androidinterview.domain.model.Merchant
import com.example.androidinterview.domain.util.formatAmount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeUiModelMapper @Inject constructor() {

    operator fun invoke(merchant: Merchant): HomeUiState.Success.BusinessData {
        return HomeUiState.Success.BusinessData(
            balanceAvailable = formatAmount(merchant.currency, merchant.availableBalance),
            balancePending = formatAmount(merchant.currency, merchant.pendingBalance),
            recentActivity = merchant.activity.take(3).map {
                HomeUiState.Success.BusinessData.Line(
                    description = it.description,
                    value = formatAmount(it.currency, it.amount),
                    valueColor = if (it.amount < 0) Color.Red else Color(0xFF2E7D32),
                )
            },
        )
    }
}
