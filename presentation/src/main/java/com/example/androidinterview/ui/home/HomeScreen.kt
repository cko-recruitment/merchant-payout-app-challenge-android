package com.example.androidinterview.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidinterview.presentation.R
@Composable
fun HomeScreen(
    onShowMore: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingContent(modifier = modifier)
        is HomeUiState.Error -> ErrorContent(modifier = modifier, message = state.message, onRetry = viewModel::loadData)
        is HomeUiState.Success -> MerchantContent(
            modifier = modifier,
            data = state.data,
            onShowMore = onShowMore,
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun MerchantContent(
    modifier: Modifier = Modifier,
    data: HomeUiState.Success.BusinessData,
    onShowMore: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        BalanceCard(data = data)
        Spacer(modifier = Modifier.height(24.dp))
        RecentActivitySection(
            activity = data.recentActivity,
            onShowMore = onShowMore,
        )
    }
}

@Composable
private fun BalanceCard(data: HomeUiState.Success.BusinessData) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_account_balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                BalanceColumn(
                    label = stringResource(R.string.home_balance_available),
                    amount = data.balanceAvailable,
                    modifier = Modifier.weight(1f),
                )
                BalanceColumn(
                    label = stringResource(R.string.home_balance_pending),
                    amount = data.balancePending,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BalanceColumn(label: String, amount: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(text = amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RecentActivitySection(activity: List<HomeUiState.Success.BusinessData.Line>, onShowMore: () -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.recent_activity),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        activity.forEach { item ->
            ActivityRow(item = item)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onShowMore,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBBDEFB),
                contentColor = Color(0xFF1565C0),
            ),
        ) {
            Text(stringResource(R.string.home_show_more))
        }
    }
}

@Composable
private fun ActivityRow(item: HomeUiState.Success.BusinessData.Line) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = item.valueColor,
        )
    }
}
