package manoellribeiro.dev.design_system.components.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/** Alert reminding the merchant to keep financial data private. */
@Composable
fun SecurityReminderDialog(
    title: String,
    message: String,
    acknowledgeLabel: String,
    onAcknowledgeClick: () -> Unit,
) {
    Dialog(onDismissRequest = onAcknowledgeClick) {
        SecurityReminderCard(
            title = title,
            message = message,
            acknowledgeLabel = acknowledgeLabel,
            onAcknowledgeClick = onAcknowledgeClick,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
private fun SecurityReminderDialogPreview() {
    MerchantPayoutTheme {
        SecurityReminderDialog(
            title = "Security Reminder",
            message = "Please keep your financial data private. Screenshots may contain sensitive information.",
            acknowledgeLabel = "OK",
            onAcknowledgeClick = {},
        )
    }
}
