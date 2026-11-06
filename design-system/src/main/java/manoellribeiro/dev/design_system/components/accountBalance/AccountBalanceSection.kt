package manoellribeiro.dev.design_system.components.accountBalance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.components.header.SectionTitle
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** The balance block on Home. Amounts arrive already formatted, money logic stays in the domain layer. */
@Composable
fun AccountBalanceSection(
    title: String,
    availableLabel: String,
    availableAmount: String,
    pendingLabel: String,
    pendingAmount: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MerchantDimens.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingLarge),
    ) {
        SectionTitle(text = title)
        Row(modifier = Modifier.fillMaxWidth()) {
            LabeledBalanceAmount(
                label = availableLabel,
                amount = availableAmount,
                modifier = Modifier.weight(1f),
            )
            LabeledBalanceAmount(
                label = pendingLabel,
                amount = pendingAmount,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LabeledBalanceAmount(label: String, amount: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = label,
            color = MerchantColors.TextSecondary,
            style = MerchantTextStyles.BodyMedium,
        )
        Text(
            text = amount,
            color = MerchantColors.TextPrimary,
            style = MerchantTextStyles.BalanceAmount,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun AccountBalanceSectionPreview() {
    MerchantPayoutTheme {
        AccountBalanceSection(
            title = "Account Balance",
            availableLabel = "Available",
            availableAmount = "£5,000.00",
            pendingLabel = "Pending",
            pendingAmount = "£250.00",
        )
    }
}
