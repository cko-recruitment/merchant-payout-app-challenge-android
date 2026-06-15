package com.example.androidinterview.ui.activity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterview.domain.model.Activity
import com.example.androidinterview.domain.repository.MerchantRepository
import com.example.androidinterview.presentation.R
import com.example.androidinterview.ui.activity.RecentActivityUiState.Error
import com.example.androidinterview.ui.activity.RecentActivityUiState.Loading
import com.example.androidinterview.ui.activity.RecentActivityUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentActivityViewModel @Inject constructor(
    private val repository: MerchantRepository,
    private val mapper: RecentActivityUiModelMapper,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val loadedItems = MutableStateFlow<List<Activity>>(emptyList())
    private val networkError = MutableStateFlow<String?>(null)
    private val isInitialLoading = MutableStateFlow(true)
    private val isLoadingMore = MutableStateFlow(false)

    private var nextCursor: String? = null
    private var hasMore = true

    val uiState: StateFlow<RecentActivityUiState> = combine(
        loadedItems, networkError, isInitialLoading, isLoadingMore,
    ) { items, error, initialLoading, loadingMore ->
        when {
            initialLoading -> Loading
            error != null  -> Error(error)
            else           -> Success(mapper(items), loadingMore, hasMore)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Loading)

    init {
        loadPage(cursor = null, isInitial = true)
    }

    fun loadMore() {
        if (hasMore && !isLoadingMore.value) loadPage(cursor = nextCursor, isInitial = false)
    }

    fun retry() {
        loadedItems.value = emptyList()
        networkError.value = null
        nextCursor = null
        hasMore = true
        loadPage(cursor = null, isInitial = true)
    }

    private fun loadPage(cursor: String?, isInitial: Boolean) {
        if (isInitial) isInitialLoading.value = true else isLoadingMore.value = true
        viewModelScope.launch {
            repository.getActivity(cursor = cursor)
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    hasMore = page.hasMore
                    loadedItems.value = loadedItems.value + page.items
                    isInitialLoading.value = false
                    isLoadingMore.value = false
                }
                .onFailure {
                    if (loadedItems.value.isEmpty()) {
                        networkError.value = it.message ?: context.getString(R.string.error_something_went_wrong)
                        isInitialLoading.value = false
                    } else {
                        isLoadingMore.value = false
                    }
                }
        }
    }
}
