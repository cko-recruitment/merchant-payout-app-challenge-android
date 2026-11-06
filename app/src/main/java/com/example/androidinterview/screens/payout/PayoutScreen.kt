package com.example.androidinterview.screens.payout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidinterview.R
import com.example.androidinterview.core.biometric.rememberBiometricAuthenticator
import com.example.androidinterview.core.security.ScreenshotDetectedEffect
import com.example.androidinterview.core.security.SecureScreenEffect
import manoellribeiro.dev.design_system.components.button.PrimaryActionButton
import manoellribeiro.dev.design_system.components.dialog.PayoutConfirmationDialog
import manoellribeiro.dev.design_system.components.dialog.PayoutSummaryLine
import manoellribeiro.dev.design_system.components.dialog.SecurityReminderDialog
import manoellribeiro.dev.design_system.components.formField.LabeledDropdownField
import manoellribeiro.dev.design_system.components.formField.LabeledTextField
import manoellribeiro.dev.design_system.components.header.ScreenTitle
import manoellribeiro.dev.design_system.components.payoutOutcome.PayoutFailureContent
import manoellribeiro.dev.design_system.components.payoutOutcome.PayoutSuccessContent
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles
import manoellribeiro.dev.domain.model.Currency

private const val AMOUNT_FIELD_WEIGHT = 2.7f
private const val CURRENCY_FIELD_WEIGHT = 1f

private val CurrencyOptions = Currency.entries.map { it.name }

@Composable
fun PayoutDestination(
    viewModelFactory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier,
    viewModel: PayoutViewModel = viewModel(factory = viewModelFactory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSecurityReminderVisible by rememberSaveable { mutableStateOf(false) }
    SecureScreenEffect()
    ScreenshotDetectedEffect(onScreenshotDetected = { isSecurityReminderVisible = true })
    val authenticator = rememberBiometricAuthenticator(viewModel::onBiometricResult)
    val biometricTitle = stringResource(R.string.payout_biometric_title)
    val biometricSubtitle = stringResource(R.string.payout_biometric_subtitle)
    val biometricCancel = stringResource(R.string.payout_cancel)
    LaunchedEffect(Unit) {
        viewModel.startObservingConnectivity()
        viewModel.events.collect { event ->
            when (event) {
                PayoutEvent.RequestBiometric -> authenticator.authenticate(
                    title = biometricTitle,
                    subtitle = biometricSubtitle,
                    cancelLabel = biometricCancel,
                )
            }
        }
    }
    PayoutScreen(
        uiState = uiState,
        onAmountChange = viewModel::onAmountChange,
        onCurrencyChange = viewModel::onCurrencyChange,
        onIbanChange = viewModel::onIbanChange,
        onConfirmClick = viewModel::onConfirmClick,
        onCancelConfirmation = viewModel::onCancelConfirmation,
        onSubmit = viewModel::onSubmit,
        onStartAgain = viewModel::onStartAgain,
        modifier = modifier,
    )
    if (isSecurityReminderVisible) {
        SecurityReminderDialog(
            title = stringResource(R.string.payout_security_reminder_title),
            message = stringResource(R.string.payout_security_reminder_message),
            acknowledgeLabel = stringResource(R.string.payout_security_reminder_ok),
            onAcknowledgeClick = { isSecurityReminderVisible = false },
        )
    }
}

@Composable
private fun PayoutScreen(
    uiState: PayoutUiState,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onIbanChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onCancelConfirmation: () -> Unit,
    onSubmit: () -> Unit,
    onStartAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PayoutUiState.Form -> PayoutForm(
            form = uiState,
            onAmountChange = onAmountChange,
            onCurrencyChange = onCurrencyChange,
            onIbanChange = onIbanChange,
            onConfirmClick = onConfirmClick,
            onCancelConfirmation = onCancelConfirmation,
            onSubmit = onSubmit,
            modifier = modifier,
        )
        is PayoutUiState.Success -> PayoutOutcome(modifier) {
            PayoutSuccessContent(
                title = stringResource(R.string.payout_success_title),
                message = stringResource(R.string.payout_success_message, uiState.formattedAmount),
                actionLabel = stringResource(R.string.payout_create_another),
                onActionClick = onStartAgain,
            )
        }
        is PayoutUiState.Failure -> PayoutOutcome(modifier) {
            PayoutFailureContent(
                title = stringResource(R.string.payout_failure_title),
                message = stringResource(uiState.reason.messageRes()),
                actionLabel = stringResource(R.string.home_retry),
                onActionClick = onStartAgain,
            )
        }
    }
}

@Composable
private fun PayoutForm(
    form: PayoutUiState.Form,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onIbanChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onCancelConfirmation: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(MerchantDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingExtraLarge),
    ) {
        ScreenTitle(text = stringResource(R.string.payouts_title))
        Row(horizontalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingMedium)) {
            LabeledTextField(
                label = stringResource(R.string.payout_amount_label),
                value = form.amount,
                onValueChange = onAmountChange,
                modifier = Modifier.weight(AMOUNT_FIELD_WEIGHT),
                placeholder = stringResource(R.string.payout_amount_placeholder),
                errorMessage = if (form.hasAmountError) stringResource(R.string.payout_amount_error) else null,
                keyboardType = KeyboardType.Decimal,
            )
            LabeledDropdownField(
                label = stringResource(R.string.payout_currency_label),
                selectedOption = form.currency.name,
                options = CurrencyOptions,
                onOptionSelected = { option -> onCurrencyChange(Currency.valueOf(option)) },
                modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT),
            )
        }
        LabeledTextField(
            label = stringResource(R.string.payout_iban_label),
            value = form.iban,
            onValueChange = onIbanChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.payout_iban_placeholder),
            helperText = stringResource(R.string.payout_iban_helper),
            errorMessage = if (form.hasIbanError) stringResource(R.string.payout_iban_error) else null,
            keyboardType = KeyboardType.Ascii,
        )
        Column(verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingSmall)) {
            PrimaryActionButton(
                label = stringResource(R.string.payout_confirm),
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = form.isConfirmEnabled,
            )
            if (!form.isOnline) NoInternetMessage()
        }
    }
    form.confirmation?.let { confirmation ->
        PayoutConfirmationDialog(
            title = stringResource(R.string.payout_confirm_title),
            summaryLines = listOf(
                PayoutSummaryLine(stringResource(R.string.payout_summary_amount), confirmation.formattedAmount),
                PayoutSummaryLine(stringResource(R.string.payout_summary_currency), confirmation.currency.name),
                PayoutSummaryLine(stringResource(R.string.payout_summary_iban), confirmation.maskedIban),
            ),
            cancelLabel = stringResource(R.string.payout_cancel),
            confirmLabel = stringResource(
                if (confirmation.isSubmitting) R.string.payout_submitting else R.string.payout_confirm,
            ),
            onCancelClick = onCancelConfirmation,
            onConfirmClick = onSubmit,
            message = if (form.isOnline) null else stringResource(R.string.payout_no_internet),
            isConfirmEnabled = form.isOnline && !confirmation.isSubmitting,
            isCancelEnabled = !confirmation.isSubmitting,
        )
    }
}

@Composable
private fun NoInternetMessage() {
    Text(
        text = stringResource(R.string.payout_no_internet),
        color = MerchantColors.Error,
        style = MerchantTextStyles.BodyMedium,
    )
}

@Composable
private fun PayoutOutcome(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenTitle(
            text = stringResource(R.string.payout_outcome_title),
            modifier = Modifier.padding(MerchantDimens.ScreenPadding),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

private fun PayoutFailureReason.messageRes(): Int = when (this) {
    PayoutFailureReason.ServiceUnavailable -> R.string.payout_error_service_unavailable
    PayoutFailureReason.InsufficientFunds -> R.string.payout_error_insufficient_funds
    PayoutFailureReason.UnknownOutcome -> R.string.payout_error_unknown_outcome
    PayoutFailureReason.Unexpected -> R.string.payout_error_unexpected
    PayoutFailureReason.BiometricCancelled -> R.string.payout_error_biometric_cancelled
    PayoutFailureReason.BiometricUnavailable -> R.string.payout_error_biometric_unavailable
}

private val PreviewEmptyForm = PayoutUiState.Form(
    amount = "",
    currency = Currency.GBP,
    iban = "",
    isOnline = true,
    isConfirmEnabled = false,
)

@Composable
private fun PayoutScreenPreview(uiState: PayoutUiState) {
    MerchantPayoutTheme {
        PayoutScreen(
            uiState = uiState,
            onAmountChange = {},
            onCurrencyChange = {},
            onIbanChange = {},
            onConfirmClick = {},
            onCancelConfirmation = {},
            onSubmit = {},
            onStartAgain = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutScreenEmptyFormPreview() {
    PayoutScreenPreview(PreviewEmptyForm)
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutScreenFormErrorsPreview() {
    PayoutScreenPreview(
        PreviewEmptyForm.copy(amount = "10.999", iban = "gb29", hasAmountError = true, hasIbanError = true),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutScreenOfflinePreview() {
    PayoutScreenPreview(PreviewEmptyForm.copy(amount = "400", iban = "GB29NWBK60161331926819", isOnline = false))
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutScreenSuccessPreview() {
    PayoutScreenPreview(PayoutUiState.Success("£400.00"))
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutScreenFailurePreview() {
    PayoutScreenPreview(PayoutUiState.Failure(PayoutFailureReason.InsufficientFunds))
}
