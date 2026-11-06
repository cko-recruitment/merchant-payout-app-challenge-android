package manoellribeiro.dev.design_system.components.payoutOutcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.R
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/** Result shown after a payout goes through. */
@Composable
fun PayoutSuccessContent(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PayoutOutcomeLayout(
        title = title,
        titleColor = MerchantColors.TextPrimary,
        message = message,
        messageColor = MerchantColors.TextBody,
        actionLabel = actionLabel,
        onActionClick = onActionClick,
        modifier = modifier,
    ) {
        // The title states the outcome, so the icon is decorative for TalkBack.
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier
                .size(MerchantDimens.OutcomeBadgeSize)
                .background(MerchantColors.Success, CircleShape)
                .padding(MerchantDimens.OutcomeBadgeIconPadding),
            tint = MerchantColors.OnSuccess,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutSuccessContentPreview() {
    MerchantPayoutTheme {
        PayoutSuccessContent(
            title = "Payout Completed",
            message = "Your payout of £400.00 has been processed successfully.",
            actionLabel = "Create Another Payout",
            onActionClick = {},
        )
    }
}
