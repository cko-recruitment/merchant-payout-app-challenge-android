package com.example.androidinterview.ui.home

import androidx.compose.ui.graphics.Color

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: BusinessData) : HomeUiState() {
        data class BusinessData(
            val balanceAvailable: String,
            val balancePending: String,
            val recentActivity: List<Line>,
        ) {
            data class Line(
                val description: String,
                val value: String,
                val valueColor: Color,
            )
        }
    }
    data class Error(val message: String) : HomeUiState()
}
