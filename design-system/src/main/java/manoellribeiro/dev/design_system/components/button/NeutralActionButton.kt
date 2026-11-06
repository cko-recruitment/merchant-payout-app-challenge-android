package manoellribeiro.dev.design_system.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/** The alternative to a primary action, e.g. "Cancel" in a dialog. */
@Composable
fun NeutralActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MerchantButton(
        label = label,
        onClick = onClick,
        containerColor = MerchantColors.Neutral,
        contentColor = MerchantColors.OnNeutral,
        minHeight = MerchantDimens.ButtonMinHeight,
        modifier = modifier,
        enabled = enabled,
    )
}

@Preview(showBackground = true)
@Composable
private fun NeutralActionButtonPreview() {
    MerchantPayoutTheme {
        NeutralActionButton(label = "Cancel", onClick = {})
    }
}
