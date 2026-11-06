package manoellribeiro.dev.design_system.components.payoutOutcome

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.R
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/** Result shown when a payout fails: service errors, insufficient funds or a cancelled biometric prompt. */
@Composable
fun PayoutFailureContent(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PayoutOutcomeLayout(
        title = title,
        titleColor = MerchantColors.Error,
        message = message,
        messageColor = MerchantColors.ErrorMessage,
        actionLabel = actionLabel,
        onActionClick = onActionClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = null,
            modifier = Modifier.size(MerchantDimens.OutcomeIconSize),
            tint = MerchantColors.Error,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutFailureServiceUnavailablePreview() {
    MerchantPayoutTheme {
        PayoutFailureContent(
            title = "Unable to Process Payout",
            message = "Service temporarily unavailable. Please try again later.",
            actionLabel = "Try Again",
            onActionClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutFailureInsufficientFundsPreview() {
    MerchantPayoutTheme {
        PayoutFailureContent(
            title = "Unable to Process Payout",
            message = "Insufficient funds.",
            actionLabel = "Try Again",
            onActionClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PayoutFailureBiometricCancelledPreview() {
    MerchantPayoutTheme {
        PayoutFailureContent(
            title = "Unable to Process Payout",
            message = "Biometric authentication was cancelled.",
            actionLabel = "Try Again",
            onActionClick = {},
        )
    }
}
