package com.example.androidinterview.screens.home

import com.example.androidinterview.core.formatting.toCurrencyText
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.MerchantBalance

sealed interface HomeUiState {

    /** The balance hasn't answered yet. */
    data object Loading : HomeUiState

    /** Both sections failed, so the screen shows one message and one retry. */
    data object Error : HomeUiState

    /** The balance has answered. Each section has its own state, so a failed section doesn't hide the other. */
    data class Content(
        val balance: BalanceSectionState,
        val activity: RecentActivitySectionState,
    ) : HomeUiState
}

fun mapToHomeUiState(balance: BalanceSectionState, activity: RecentActivitySectionState): HomeUiState =
    if (balance is BalanceSectionState.Error && activity is RecentActivitySectionState.Error) {
        HomeUiState.Error
    } else {
        HomeUiState.Content(balance, activity)
    }

sealed interface BalanceSectionState {
    data object Error : BalanceSectionState
    data class Loaded(val available: String, val pending: String) : BalanceSectionState
}

fun Result<MerchantBalance>.mapToSectionState(): BalanceSectionState = fold(
    onSuccess = { BalanceSectionState.Loaded(it.available.toCurrencyText(), it.pending.toCurrencyText()) },
    onFailure = { BalanceSectionState.Error },
)

sealed interface RecentActivitySectionState {
    data object Loading : RecentActivitySectionState
    data object Error : RecentActivitySectionState
    data object Empty : RecentActivitySectionState
    data class Loaded(val items: List<RecentActivityItem>) : RecentActivitySectionState
}

fun Result<ActivityPage>.mapToSectionState(): RecentActivitySectionState = fold(
    onSuccess = { page ->
        if (page.items.isEmpty()) {
            RecentActivitySectionState.Empty
        } else {
            RecentActivitySectionState.Loaded(
                page.items.map { item ->
                    RecentActivityItem(
                        id = item.id,
                        description = item.description,
                        formattedAmount = item.amount.toCurrencyText(),
                        isOutflow = item.amount.minorUnits < 0,
                    )
                },
            )
        }
    },
    onFailure = { RecentActivitySectionState.Error },
)

data class RecentActivityItem(
    val id: String,
    val description: String,
    val formattedAmount: String,
    val isOutflow: Boolean,
)
