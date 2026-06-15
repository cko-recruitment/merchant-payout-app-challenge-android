package com.example.androidinterview.ui.activity

import androidx.compose.ui.graphics.Color

sealed class RecentActivityUiState {
    object Loading : RecentActivityUiState()
    data class Error(val message: String) : RecentActivityUiState()
    data class Success(
        val groups: List<ActivityGroup>,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
    ) : RecentActivityUiState() {
        data class ActivityGroup(val label: String, val items: List<Item>)
        data class Item(
            val description: String,
            val type: String,
            val value: String,
            val valueColor: Color,
            val date: String,
            val status: String,
        )
    }
}
