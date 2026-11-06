package manoellribeiro.dev.design_system.components.activityRow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

/** Full transaction row for the paginated list. Built to stay flat because it is a LazyColumn item. */
@Composable
fun TransactionRow(
    type: String,
    description: String,
    formattedDate: String,
    formattedAmount: String,
    status: String,
    isOutflow: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .bottomHairline(MerchantColors.Divider)
            .padding(vertical = MerchantDimens.SpacingMedium),
        horizontalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingExtraSmall),
        ) {
            Text(
                text = type,
                color = MerchantColors.TextPrimary,
                style = MerchantTextStyles.BodyLargeBold,
            )
            Text(
                text = description,
                color = MerchantColors.TextPrimary,
                style = MerchantTextStyles.BodyMedium,
            )
            Text(
                text = formattedDate,
                color = MerchantColors.TextTertiary,
                style = MerchantTextStyles.Caption,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingExtraSmall),
        ) {
            Text(
                text = formattedAmount,
                color = amountColor(isOutflow),
                style = MerchantTextStyles.BodyLargeBold,
            )
            Text(
                text = status,
                color = MerchantColors.TextSecondary,
                style = MerchantTextStyles.Caption,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionRowInflowPreview() {
    MerchantPayoutTheme {
        TransactionRow(
            type = "Deposit",
            description = "Payment from Customer MYK",
            formattedDate = "23 Jan 2026",
            formattedAmount = "£727.38",
            status = "Completed",
            isOutflow = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionRowOutflowPreview() {
    MerchantPayoutTheme {
        TransactionRow(
            type = "Fee",
            description = "Transaction fee",
            formattedDate = "23 Jan 2026",
            formattedAmount = "-£979.74",
            status = "Completed",
            isOutflow = true,
        )
    }
}
