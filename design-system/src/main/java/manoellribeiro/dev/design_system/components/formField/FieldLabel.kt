package manoellribeiro.dev.design_system.components.formField

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        color = MerchantColors.TextPrimary,
        style = MerchantTextStyles.BodyLargeBold,
    )
}
