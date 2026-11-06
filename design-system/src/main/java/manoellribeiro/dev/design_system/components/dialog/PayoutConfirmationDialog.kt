package manoellribeiro.dev.design_system.components.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/**
 * Modal that asks the merchant to review a payout before it is submitted. Back and tapping outside only dismiss it
 * while [isCancelEnabled] is true.
 */
@Composable
fun PayoutConfirmationDialog(
    title: String,
    summaryLines: List<PayoutSummaryLine>,
    cancelLabel: String,
    confirmLabel: String,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    message: String? = null,
    isConfirmEnabled: Boolean = true,
    isCancelEnabled: Boolean = true,
) {
    Dialog(onDismissRequest = { if (isCancelEnabled) onCancelClick() }) {
        PayoutConfirmationCard(
            title = title,
            summaryLines = summaryLines,
            cancelLabel = cancelLabel,
            confirmLabel = confirmLabel,
            onCancelClick = onCancelClick,
            onConfirmClick = onConfirmClick,
            message = message,
            isConfirmEnabled = isConfirmEnabled,
            isCancelEnabled = isCancelEnabled,
        )
    }
}

private val PreviewSummaryLines = listOf(
    PayoutSummaryLine(label = "Amount:", value = "£400.00"),
    PayoutSummaryLine(label = "Currency:", value = "GBP"),
    PayoutSummaryLine(label = "IBAN:", value = "FR12**********************1231"),
)

@Preview(showBackground = true, backgroundColor = 0xFF777777)
@Composable
private fun PayoutConfirmationDialogPreview() {
    MerchantPayoutTheme {
        PayoutConfirmationDialog(
            title = "Confirm Payout",
            summaryLines = PreviewSummaryLines,
            cancelLabel = "Cancel",
            confirmLabel = "Confirm",
            onCancelClick = {},
            onConfirmClick = {},
        )
    }
}
