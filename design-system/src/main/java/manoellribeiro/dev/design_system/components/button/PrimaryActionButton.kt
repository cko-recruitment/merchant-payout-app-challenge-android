package manoellribeiro.dev.design_system.components.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/** The main action of a screen, e.g. "Confirm" or "Try Again". */
@Composable
fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MerchantButton(
        label = label,
        onClick = onClick,
        containerColor = MerchantColors.Primary,
        contentColor = MerchantColors.OnPrimary,
        minHeight = MerchantDimens.ButtonMinHeight,
        modifier = modifier,
        enabled = enabled,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PrimaryActionButtonEnabledPreview() {
    MerchantPayoutTheme {
        PrimaryActionButton(label = "Confirm", onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun PrimaryActionButtonDisabledPreview() {
    MerchantPayoutTheme {
        PrimaryActionButton(
            label = "Confirm",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
        )
    }
}
