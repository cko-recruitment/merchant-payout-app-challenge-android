package com.example.androidinterview.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import manoellribeiro.dev.domain.usecase.GetActivityUseCase
import manoellribeiro.dev.domain.usecase.GetBalanceUseCase

class HomeViewModel(
    private val getBalance: GetBalanceUseCase,
    private val getActivity: GetActivityUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadHomeJob: Job? = null

    private fun emitNewState(newState: HomeUiState) {
        _uiState.value = newState
    }

    fun loadHome() {
        loadHomeJob?.cancel()
        loadHomeJob = viewModelScope.launch {
            emitNewState(HomeUiState.Loading)
            coroutineScope {
                val balance = async { getBalance().mapToSectionState() }
                val activity = async { getActivity(cursor = null, limit = RECENT_ACTIVITY_LIMIT).mapToSectionState() }
                // The balance renders as soon as it arrives, so a slow activity fetch can't delay it.

                emitNewState(
                    HomeUiState.Content(
                        balance.await(),
                        RecentActivitySectionState.Loading
                    )
                )
                emitNewState(
                    mapToHomeUiState(
                        balance.await(),
                        activity.await()
                    )
                )
            }
        }
    }

    private companion object {
        const val RECENT_ACTIVITY_LIMIT = 3
    }
}
