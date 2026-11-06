package manoellribeiro.dev.design_system.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.components.button.TonalActionButton
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Shown in place of a section's content when it fails to load, so the rest of the screen stays usable. */
@Composable
fun SectionErrorMessage(
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingMedium),
    ) {
        Text(
            text = message,
            color = MerchantColors.ErrorMessage,
            style = MerchantTextStyles.BodyMedium,
        )
        TonalActionButton(label = retryLabel, onClick = onRetry, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun SectionErrorMessagePreview() {
    MerchantPayoutTheme {
        SectionErrorMessage(
            message = "We couldn't load your balance.",
            retryLabel = "Try Again",
            onRetry = {},
        )
    }
}
