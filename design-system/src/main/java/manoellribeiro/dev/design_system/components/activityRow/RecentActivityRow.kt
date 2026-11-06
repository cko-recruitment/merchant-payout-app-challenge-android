package manoellribeiro.dev.design_system.components.activityRow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.components.bottomHairline
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Single-line activity row used on Home: description and amount only. */
@Composable
fun RecentActivityRow(
    description: String,
    formattedAmount: String,
    isOutflow: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .bottomHairline(MerchantColors.Divider)
            .padding(vertical = MerchantDimens.SpacingLarge),
        horizontalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = description,
            modifier = Modifier.weight(1f),
            color = MerchantColors.TextPrimary,
            style = MerchantTextStyles.BodyMedium,
        )
        Text(
            text = formattedAmount,
            color = amountColor(isOutflow),
            style = MerchantTextStyles.BodyLargeBold,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun RecentActivityRowInflowPreview() {
    MerchantPayoutTheme {
        RecentActivityRow(
            description = "Payment from Customer QGW",
            formattedAmount = "£686.22",
            isOutflow = false,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun RecentActivityRowOutflowPreview() {
    MerchantPayoutTheme {
        RecentActivityRow(
            description = "Payout to Bank Account ****1234",
            formattedAmount = "-£500.00",
            isOutflow = true,
        )
    }
}
