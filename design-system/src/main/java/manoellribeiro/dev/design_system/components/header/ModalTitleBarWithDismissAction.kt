package manoellribeiro.dev.design_system.components.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.components.bottomHairline
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Top bar of a full-screen modal: a title, a dismiss action and a divider underneath. */
@Composable
fun ModalTitleBarWithDismissAction(
    title: String,
    dismissLabel: String,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bottomHairline(MerchantColors.BarDivider)
            .padding(
                start = MerchantDimens.ScreenPadding,
                end = MerchantDimens.SpacingSmall,
                top = MerchantDimens.SpacingSmall,
                bottom = MerchantDimens.SpacingSmall,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            color = MerchantColors.TextPrimary,
            style = MerchantTextStyles.ModalTitle,
        )
        TextButton(onClick = onDismissClick) {
            Text(
                text = dismissLabel,
                color = MerchantColors.OnTonal,
                style = MerchantTextStyles.Button,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModalTitleBarWithDismissActionPreview() {
    MerchantPayoutTheme {
        ModalTitleBarWithDismissAction(
            title = "Recent Activity",
            dismissLabel = "Done",
            onDismissClick = {},
        )
    }
}
