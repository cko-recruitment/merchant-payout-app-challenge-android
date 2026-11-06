package com.example.androidinterview.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidinterview.R
import manoellribeiro.dev.design_system.components.activityRow.LoadingMoreIndicator
import manoellribeiro.dev.design_system.components.activityRow.TransactionDateHeader
import manoellribeiro.dev.design_system.components.activityRow.TransactionRow
import manoellribeiro.dev.design_system.components.feedback.SectionErrorMessage
import manoellribeiro.dev.design_system.components.header.ModalTitleBarWithDismissAction
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

private const val NEXT_PAGE_FOOTER_KEY = "next-page-footer"
private const val LOAD_MORE_THRESHOLD = 5

private enum class ContentType { Header, Row, Footer }

@Composable
fun TransactionsDestination(
    viewModelFactory: ViewModelProvider.Factory,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel(factory = viewModelFactory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadFirstPage() }
    TransactionsScreen(
        uiState = uiState,
        onDone = onDone,
        onRetryFirstPage = viewModel::loadFirstPage,
        onLoadNextPage = viewModel::loadNextPage,
        onRetryNextPage = viewModel::retryNextPage,
        modifier = modifier,
    )
}

@Composable
private fun TransactionsScreen(
    uiState: TransactionsUiState,
    onDone: () -> Unit,
    onRetryFirstPage: () -> Unit,
    onLoadNextPage: () -> Unit,
    onRetryNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MerchantColors.Surface),
    ) {
        ModalTitleBarWithDismissAction(
            title = stringResource(R.string.transactions_title),
            dismissLabel = stringResource(R.string.transactions_done),
            onDismissClick = onDone,
        )
        val messageModifier = Modifier.padding(MerchantDimens.ScreenPadding)
        when (uiState) {
            TransactionsUiState.Loading -> LoadingMoreIndicator(
                label = stringResource(R.string.home_loading),
                modifier = messageModifier,
            )
            TransactionsUiState.Error -> SectionErrorMessage(
                message = stringResource(R.string.transactions_error),
                retryLabel = stringResource(R.string.home_retry),
                onRetry = onRetryFirstPage,
                modifier = messageModifier,
            )
            TransactionsUiState.Empty -> Text(
                text = stringResource(R.string.transactions_empty),
                modifier = messageModifier,
                color = MerchantColors.TextSecondary,
                style = MerchantTextStyles.BodyMedium,
            )
            is TransactionsUiState.Content -> TransactionsList(
                content = uiState,
                onLoadNextPage = onLoadNextPage,
                onRetryNextPage = onRetryNextPage,
            )
        }
    }
}

@Composable
private fun TransactionsList(
    content: TransactionsUiState.Content,
    onLoadNextPage: () -> Unit,
    onRetryNextPage: () -> Unit,
) {
    val listState = rememberLazyListState()
    val isNearEnd by remember(listState) { derivedStateOf { listState.isNearEnd() } }
    // items.size is a key because a short page can leave the user near the end, which needs another fetch.
    LaunchedEffect(isNearEnd, content.items.size) {
        if (isNearEnd) onLoadNextPage()
    }
    // Resolved once here, so the lazy items don't each read a CompositionLocal.
    val resources = LocalResources.current
    val loadingMoreLabel = stringResource(R.string.transactions_loading_more)
    val nextPageError = stringResource(R.string.transactions_next_page_error)
    val retryLabel = stringResource(R.string.home_retry)
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = MerchantDimens.ScreenPadding),
    ) {
        items(
            items = content.items,
            key = { it.key },
            contentType = { item ->
                when (item) {
                    is TransactionListItem.DateHeader -> ContentType.Header
                    is TransactionListItem.Row -> ContentType.Row
                }
            },
        ) { item ->
            when (item) {
                is TransactionListItem.DateHeader -> TransactionDateHeader(
                    label = item.relativeLabelRes?.let(resources::getString) ?: item.formattedDate,
                )
                is TransactionListItem.Row -> TransactionRow(
                    type = resources.getString(item.typeLabelRes),
                    description = item.description,
                    formattedDate = item.formattedDate,
                    formattedAmount = item.formattedAmount,
                    status = resources.getString(item.statusLabelRes),
                    isOutflow = item.isOutflow,
                )
            }
        }
        when (content.nextPageState) {
            NextPageState.Idle -> Unit
            NextPageState.Loading -> item(key = NEXT_PAGE_FOOTER_KEY, contentType = ContentType.Footer) {
                LoadingMoreIndicator(label = loadingMoreLabel)
            }
            NextPageState.Error -> item(key = NEXT_PAGE_FOOTER_KEY, contentType = ContentType.Footer) {
                SectionErrorMessage(
                    message = nextPageError,
                    retryLabel = retryLabel,
                    onRetry = onRetryNextPage,
                    modifier = Modifier.padding(vertical = MerchantDimens.SpacingLarge),
                )
            }
        }
    }
}

private fun LazyListState.isNearEnd(): Boolean {
    val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisibleIndex >= layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
}

private val PreviewItems = listOf(
    TransactionListItem.DateHeader("header-1", R.string.transactions_today, "23 Jan 2026"),
    TransactionListItem.Row(
        "1", R.string.activity_type_deposit, "Payment from Customer MYK", "23 Jan 2026", "£727.38",
        R.string.activity_status_completed, isOutflow = false,
    ),
    TransactionListItem.DateHeader("header-2", null, "22 Jan 2026"),
    TransactionListItem.Row(
        "2", R.string.activity_type_fee, "Transaction fee", "22 Jan 2026", "-£979.74",
        R.string.activity_status_completed, isOutflow = true,
    ),
)

@Composable
private fun TransactionsScreenPreview(uiState: TransactionsUiState) {
    MerchantPayoutTheme {
        TransactionsScreen(
            uiState = uiState,
            onDone = {},
            onRetryFirstPage = {},
            onLoadNextPage = {},
            onRetryNextPage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenLoadingPreview() {
    TransactionsScreenPreview(TransactionsUiState.Loading)
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenErrorPreview() {
    TransactionsScreenPreview(TransactionsUiState.Error)
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenEmptyPreview() {
    TransactionsScreenPreview(TransactionsUiState.Empty)
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenContentPreview() {
    TransactionsScreenPreview(TransactionsUiState.Content(PreviewItems, NextPageState.Idle))
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenLoadingNextPagePreview() {
    TransactionsScreenPreview(TransactionsUiState.Content(PreviewItems, NextPageState.Loading))
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenNextPageErrorPreview() {
    TransactionsScreenPreview(TransactionsUiState.Content(PreviewItems, NextPageState.Error))
}
