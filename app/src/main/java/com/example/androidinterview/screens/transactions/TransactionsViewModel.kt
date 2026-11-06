package com.example.androidinterview.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.usecase.GetActivityUseCase

class TransactionsViewModel(
    private val getActivity: GetActivityUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<TransactionsUiState> = MutableStateFlow(TransactionsUiState.Loading)
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private var loadedItems: List<ActivityItem> = emptyList()
    private var nextCursor: String? = null
    private var loadFirstPageJob: Job? = null

    private fun emitNewState(newState: TransactionsUiState) {
        _uiState.value = newState
    }

    /** Does nothing once pages are loaded, so a rotation keeps the list and its scroll position. */
    fun loadFirstPage() {
        if (_uiState.value is TransactionsUiState.Content || loadFirstPageJob?.isActive == true) return
        loadFirstPageJob = viewModelScope.launch {
            emitNewState(TransactionsUiState.Loading)
            getActivity(cursor = null, limit = PAGE_SIZE).fold(
                onSuccess = ::onPageLoaded,
                onFailure = { emitNewState(TransactionsUiState.Error) },
            )
        }
    }

    /** Called while scrolling near the end, so it only starts from Idle: this is what prevents duplicate fetches. */
    fun loadNextPage() {
        val content = _uiState.value as? TransactionsUiState.Content ?: return
        if (content.nextPageState == NextPageState.Idle) fetchNextPage(content)
    }

    /** Scrolling doesn't retry a failed page by itself, the user asks for it. */
    fun retryNextPage() {
        val content = _uiState.value as? TransactionsUiState.Content ?: return
        if (content.nextPageState == NextPageState.Error) fetchNextPage(content)
    }

    private fun fetchNextPage(content: TransactionsUiState.Content) {
        val cursor = nextCursor ?: return
        emitNewState(content.copy(nextPageState = NextPageState.Loading))
        viewModelScope.launch {
            getActivity(cursor = cursor, limit = PAGE_SIZE).fold(
                onSuccess = ::onPageLoaded,
                onFailure = { emitNewState(content.copy(nextPageState = NextPageState.Error)) },
            )
        }
    }

    private fun onPageLoaded(page: ActivityPage) {
        loadedItems = loadedItems + page.items
        nextCursor = page.nextCursor.takeIf { page.hasMore }
        emitNewState(
            if (loadedItems.isEmpty()) {
                TransactionsUiState.Empty
            } else {
                TransactionsUiState.Content(loadedItems.mapToTransactionListItems(), NextPageState.Idle)
            }
        )
    }

    private companion object {
        const val PAGE_SIZE = 15
    }
}
