package manoellribeiro.dev.design_system.components.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme

/** A low-emphasis action inside a screen's content, e.g. "Show More". */
@Composable
fun TonalActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MerchantButton(
        label = label,
        onClick = onClick,
        containerColor = MerchantColors.Tonal,
        contentColor = MerchantColors.OnTonal,
        minHeight = MerchantDimens.CompactButtonMinHeight,
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun TonalActionButtonPreview() {
    MerchantPayoutTheme {
        TonalActionButton(label = "Show More", onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}
