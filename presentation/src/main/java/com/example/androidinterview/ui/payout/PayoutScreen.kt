package com.example.androidinterview.ui.payout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.presentation.R

@Composable
fun PayoutScreen(
    modifier: Modifier = Modifier,
    viewModel: PayoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val amountInput by viewModel.amountInput.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val ibanInput by viewModel.ibanInput.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PayoutUiState.Idle, is PayoutUiState.Confirming, is PayoutUiState.AwaitingBiometric -> PayoutForm(
                amountInput = amountInput,
                currency = currency,
                ibanInput = ibanInput,
                isFormValid = isFormValid,
                onAmountChange = { viewModel.amountInput.value = it },
                onCurrencyChange = { viewModel.currency.value = it },
                onIbanChange = { viewModel.ibanInput.value = it.uppercase() },
                onRequestPayout = viewModel::onRequestPayout,
            )

            is PayoutUiState.Submitting -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is PayoutUiState.Success -> SuccessContent(
                state = state,
                onDone = viewModel::onReset,
            )

            is PayoutUiState.Error -> {
                val message = when (state) {
                    is PayoutUiState.Error.InsufficientFunds  -> stringResource(R.string.error_insufficient_funds)
                    is PayoutUiState.Error.ServiceUnavailable -> stringResource(R.string.error_service_unavailable)
                    is PayoutUiState.Error.Generic            -> state.message
                }
                val buttonLabel = stringResource(R.string.try_again)
                val onClick: () -> Unit = viewModel::onRetry
                ErrorContent(message = message, buttonLabel = buttonLabel, onClick = onClick)
            }
        }

        if (uiState is PayoutUiState.Confirming) {
            val state = uiState as PayoutUiState.Confirming
            AlertDialog(
                onDismissRequest = viewModel::onRetry,
                title = { Text(stringResource(R.string.payout_confirm_dialog_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow(label = stringResource(R.string.payout_field_amount), value = state.formattedAmount)
                        SummaryRow(label = stringResource(R.string.payout_field_currency), value = state.currency.name)
                        SummaryRow(label = stringResource(R.string.payout_field_iban), value = state.iban)
                    }
                },
                confirmButton = {
                    Button(onClick = viewModel::onConfirm) { Text(stringResource(R.string.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onRetry) { Text(stringResource(R.string.cancel)) }
                },
            )
        }

        if (uiState is PayoutUiState.AwaitingBiometric) {
            val state = uiState as PayoutUiState.AwaitingBiometric
            val context = LocalContext.current
            val title = stringResource(R.string.payout_confirm_dialog_title)
            val subtitle = stringResource(R.string.payout_biometric_subtitle, state.formattedAmount)
            val negativeButtonText = stringResource(R.string.cancel)
            LaunchedEffect(state) {
                val canAuth = BiometricManager.from(context)
                    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                    viewModel.onBiometricNotEnrolled()
                    return@LaunchedEffect
                }
                BiometricPrompt(
                    context as FragmentActivity,
                    ContextCompat.getMainExecutor(context),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            viewModel.onBiometricSuccess()
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            viewModel.onBiometricFailure()
                        }
                        override fun onAuthenticationFailed() {}
                    },
                ).authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .setNegativeButtonText(negativeButtonText)
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .build()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayoutForm(
    amountInput: String,
    currency: Currency,
    ibanInput: String,
    isFormValid: Boolean,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onIbanChange: (String) -> Unit,
    onRequestPayout: () -> Unit,
) {
    var currencyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.payout_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                label = { Text(stringResource(R.string.payout_field_amount)) },
                modifier = Modifier.weight(0.7f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )

            Spacer(Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = it },
                modifier = Modifier.weight(0.3f)
            ) {
                OutlinedTextField(
                    value = currency.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.payout_field_currency)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                ) {
                    Currency.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                onCurrencyChange(option)
                                currencyExpanded = false
                            },
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = ibanInput,
            onValueChange = onIbanChange,
            label = { Text(stringResource(R.string.payout_field_iban)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            singleLine = true,
        )
        Text(
            stringResource(R.string.payout_iban_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onRequestPayout,
            enabled = isFormValid,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.confirm))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SuccessContent(state: PayoutUiState.Success, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color.Green,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(40.dp),
        )
        Text(
            stringResource(R.string.payout_success_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.payout_success_message, state.formattedAmount),
            style = MaterialTheme.typography.titleSmall.copy(lineBreak = LineBreak.Heading),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.payout_create_another))
        }
    }
}

@Composable
private fun ErrorContent(message: String, buttonLabel: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Clear,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(40.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.error_payout_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClick, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(buttonLabel)
        }
    }
}

@Preview
@Composable
private fun ErrorContentPreview() {
    ErrorContent("Message", "Label") { }
}