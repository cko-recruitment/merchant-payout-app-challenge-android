package manoellribeiro.dev.design_system.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Content of [SecurityReminderDialog], kept separate so it renders in static previews. */
@Composable
fun SecurityReminderCard(
    title: String,
    message: String,
    acknowledgeLabel: String,
    onAcknowledgeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MerchantColors.Surface)
            .padding(MerchantDimens.SpacingExtraLarge),
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingSmall),
    ) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            color = MerchantColors.TextPrimary,
            style = MerchantTextStyles.DialogTitle,
        )
        Text(
            text = message,
            color = MerchantColors.TextPrimary,
            style = MerchantTextStyles.BodyLarge,
        )
        TextButton(
            onClick = onAcknowledgeClick,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = acknowledgeLabel,
                color = MerchantColors.Accent,
                style = MerchantTextStyles.Button,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
private fun SecurityReminderCardPreview() {
    MerchantPayoutTheme {
        SecurityReminderCard(
            title = "Security Reminder",
            message = "Please keep your financial data private. Screenshots may contain sensitive information.",
            acknowledgeLabel = "OK",
            onAcknowledgeClick = {},
        )
    }
}
