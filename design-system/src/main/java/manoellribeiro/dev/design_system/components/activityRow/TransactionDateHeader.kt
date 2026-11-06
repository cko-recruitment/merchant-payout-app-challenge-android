package manoellribeiro.dev.design_system.components.activityRow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Labels a group of transactions from the same day, e.g. "Today" or "18 May 2025". */
@Composable
fun TransactionDateHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(top = MerchantDimens.SpacingLarge, bottom = MerchantDimens.SpacingExtraSmall),
        color = MerchantColors.TextSecondary,
        style = MerchantTextStyles.BodyLargeBold,
    )
}

@Preview(showBackground = true)
@Composable
private fun TransactionDateHeaderPreview() {
    MerchantPayoutTheme { TransactionDateHeader(label = "Today") }
}
