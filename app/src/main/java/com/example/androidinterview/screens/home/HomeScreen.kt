package com.example.androidinterview.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidinterview.R
import manoellribeiro.dev.design_system.components.accountBalance.AccountBalanceSection
import manoellribeiro.dev.design_system.components.activityRow.LoadingMoreIndicator
import manoellribeiro.dev.design_system.components.activityRow.RecentActivityRow
import manoellribeiro.dev.design_system.components.button.TonalActionButton
import manoellribeiro.dev.design_system.components.feedback.SectionErrorMessage
import manoellribeiro.dev.design_system.components.header.ScreenTitle
import manoellribeiro.dev.design_system.components.header.SectionTitle
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

@Composable
fun HomeDestination(
    viewModelFactory: ViewModelProvider.Factory,
    onShowMore: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = viewModelFactory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Runs again whenever Home re-enters composition, so the balance is fresh after returning from another screen.
    LaunchedEffect(Unit) { viewModel.loadHome() }
    HomeScreen(
        uiState = uiState,
        onRetry = viewModel::loadHome,
        onShowMore = onShowMore,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onShowMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MerchantDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingExtraLarge),
    ) {
        ScreenTitle(text = stringResource(R.string.home_title))
        when (uiState) {
            HomeUiState.Loading -> LoadingMoreIndicator(label = stringResource(R.string.home_loading))
            HomeUiState.Error -> SectionErrorMessage(
                message = stringResource(R.string.home_error),
                retryLabel = stringResource(R.string.home_retry),
                onRetry = onRetry,
            )
            is HomeUiState.Content -> {
                BalanceContent(section = uiState.balance, onRetry = onRetry)
                RecentActivityContent(section = uiState.activity, onRetry = onRetry, onShowMore = onShowMore)
            }
        }
    }
}

@Composable
private fun BalanceContent(section: BalanceSectionState, onRetry: () -> Unit) {
    when (section) {
        is BalanceSectionState.Loaded -> AccountBalanceSection(
            title = stringResource(R.string.home_balance_title),
            availableLabel = stringResource(R.string.home_balance_available),
            availableAmount = section.available,
            pendingLabel = stringResource(R.string.home_balance_pending),
            pendingAmount = section.pending,
        )
        BalanceSectionState.Error -> TitledSection(R.string.home_balance_title) {
            SectionErrorMessage(
                message = stringResource(R.string.home_balance_error),
                retryLabel = stringResource(R.string.home_retry),
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun RecentActivityContent(
    section: RecentActivitySectionState,
    onRetry: () -> Unit,
    onShowMore: () -> Unit,
) {
    TitledSection(R.string.home_activity_title) {
        when (section) {
            RecentActivitySectionState.Loading -> LoadingMoreIndicator(label = stringResource(R.string.home_loading))
            RecentActivitySectionState.Error -> SectionErrorMessage(
                message = stringResource(R.string.home_activity_error),
                retryLabel = stringResource(R.string.home_retry),
                onRetry = onRetry,
            )
            RecentActivitySectionState.Empty -> Text(
                text = stringResource(R.string.home_activity_empty),
                color = MerchantColors.TextSecondary,
                style = MerchantTextStyles.BodyMedium,
            )
            is RecentActivitySectionState.Loaded -> {
                // At most 3 rows, so a plain Column is cheaper than a lazy list.
                Column {
                    section.items.forEach { item ->
                        RecentActivityRow(
                            description = item.description,
                            formattedAmount = item.formattedAmount,
                            isOutflow = item.isOutflow,
                        )
                    }
                }
                TonalActionButton(
                    label = stringResource(R.string.home_show_more),
                    onClick = onShowMore,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TitledSection(titleRes: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingMedium)) {
        SectionTitle(text = stringResource(titleRes))
        content()
    }
}

private val PreviewBalance = BalanceSectionState.Loaded(available = "£5,000.00", pending = "£250.00")
private val PreviewActivity = RecentActivitySectionState.Loaded(
    listOf(
        RecentActivityItem("1", "Payment from Customer QGW", "£686.22", isOutflow = false),
        RecentActivityItem("2", "Payout to Bank Account ****1234", "-£500.00", isOutflow = true),
        RecentActivityItem("3", "Payment from Customer ABC", "£1,500.00", isOutflow = false),
    ),
)

@Composable
private fun HomeScreenPreview(uiState: HomeUiState) {
    MerchantPayoutTheme { HomeScreen(uiState = uiState, onRetry = {}, onShowMore = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenLoadingPreview() {
    HomeScreenPreview(HomeUiState.Loading)
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenLoadedPreview() {
    HomeScreenPreview(HomeUiState.Content(PreviewBalance, PreviewActivity))
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenActivityStillLoadingPreview() {
    HomeScreenPreview(HomeUiState.Content(PreviewBalance, RecentActivitySectionState.Loading))
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenActivityEmptyPreview() {
    HomeScreenPreview(HomeUiState.Content(PreviewBalance, RecentActivitySectionState.Empty))
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenErrorPreview() {
    HomeScreenPreview(HomeUiState.Error)
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenBalanceFailedPreview() {
    HomeScreenPreview(HomeUiState.Content(BalanceSectionState.Error, PreviewActivity))
}
