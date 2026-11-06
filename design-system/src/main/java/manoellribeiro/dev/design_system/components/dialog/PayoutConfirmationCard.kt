package manoellribeiro.dev.design_system.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.components.bottomHairline
import manoellribeiro.dev.design_system.components.button.NeutralActionButton
import manoellribeiro.dev.design_system.components.button.PrimaryActionButton
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantShapes
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Content of [PayoutConfirmationDialog], kept separate so it renders in static previews. */
@Composable
fun PayoutConfirmationCard(
    title: String,
    summaryLines: List<PayoutSummaryLine>,
    cancelLabel: String,
    confirmLabel: String,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    isConfirmEnabled: Boolean = true,
    isCancelEnabled: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MerchantColors.Surface, MerchantShapes.Dialog)
            .padding(MerchantDimens.SpacingExtraLarge),
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingLarge),
    ) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
            color = MerchantColors.TextPrimary,
            textAlign = TextAlign.Center,
            style = MerchantTextStyles.ModalTitle,
        )
        // A payout summary only has a few lines, so a plain Column is cheaper than a lazy list.
        Column {
            summaryLines.forEach { line ->
                PayoutSummaryRow(label = line.label, value = line.value)
            }
        }
        message?.let { text ->
            Text(
                text = text,
                color = MerchantColors.Error,
                style = MerchantTextStyles.BodyMedium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingMedium)) {
            NeutralActionButton(
                label = cancelLabel,
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                enabled = isCancelEnabled,
            )
            PrimaryActionButton(
                label = confirmLabel,
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f),
                enabled = isConfirmEnabled,
            )
        }
    }
}

@Composable
private fun PayoutSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .bottomHairline(MerchantColors.DialogDivider)
            .padding(vertical = MerchantDimens.SpacingLarge),
        horizontalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MerchantColors.TextSecondary,
            style = MerchantTextStyles.BodyLarge,
        )
        // Wraps long values such as a masked IBAN instead of overflowing the card.
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = MerchantColors.TextPrimary,
            textAlign = TextAlign.End,
            style = MerchantTextStyles.BodyLargeBold,
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
private fun PayoutConfirmationCardOfflinePreview() {
    MerchantPayoutTheme {
        PayoutConfirmationCard(
            title = "Confirm Payout",
            summaryLines = PreviewSummaryLines,
            cancelLabel = "Cancel",
            confirmLabel = "Confirm",
            onCancelClick = {},
            onConfirmClick = {},
            message = "No internet connection",
            isConfirmEnabled = false,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF777777)
@Composable
private fun PayoutConfirmationCardPreview() {
    MerchantPayoutTheme {
        PayoutConfirmationCard(
            title = "Confirm Payout",
            summaryLines = PreviewSummaryLines,
            cancelLabel = "Cancel",
            confirmLabel = "Confirm",
            onCancelClick = {},
            onConfirmClick = {},
        )
    }
}
